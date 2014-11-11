package org.corespring.couchdb

import java.net.URL

import com.fasterxml.jackson.core._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ning.http.client.AsyncHttpClientConfig
import org.apache.commons.codec.binary.Base64
import play.api.libs.json._
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration.Duration

object Importer {

  import ExecutionContext.Implicits.global

  /** Target DB info **/
  val uri = System.getenv("ENV_COUCHDB_URI")
  val username = System.getenv("ENV_CLOUDANT_USERNAME")
  val password = System.getenv("ENV_CLOUDANT_PASSWORD")
  val db = "v2_item_sessions_2"

  /** HTTP client **/
  val builder = new AsyncHttpClientConfig.Builder()
  val client = new NingWSClient(builder.build())

  /** Need to buffer writes because CouchDB maxes out POSTs to _bulk_docs at 64mb. **/
  val MaxBufferSize = 5000000
  val stringBuffer = new StringBuffer()

  /**
   * Takes a URL pointing to a JSON file obtained from _all_docs, and pushes its contents to CouchDB.
   */
  def doImport(url: URL) {
    val factory = new JsonFactory()
    factory.setCodec(new ObjectMapper(factory))
    val parser = factory.createParser(url)

    parser.nextToken() match {
      case JsonToken.START_OBJECT => {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
          val currentToken: JsonToken = parser.nextToken()
          parser.getCurrentName() match {
            case "rows" => currentToken match {
              case JsonToken.START_ARRAY => {
                while (parser.nextToken() != JsonToken.END_ARRAY) {
                  val node = parser.readValueAsTree().asInstanceOf[ObjectNode].toString
                  push(node)
                }
              }
              case _ => throw new IllegalArgumentException("Error: rows should be an array")
            }
            case _ => {}
          }
        }
      }
      case _ => throw new IllegalArgumentException("Error: root should be object")
    }
  }

  /**
   * Pushes a JSON string to CouchDB
   */
  private def push(node: String) {
    val value = Json.parse(node) \ "doc"
    val jsonTransformer = (__ \ '_rev).json.prune
    value.validate(jsonTransformer) match {
      case JsSuccess(result, _) => {
        if (stringBuffer.length != 0) {
          stringBuffer.append(",")
        }
        stringBuffer.append(value)
        val bufferSize = stringBuffer.toString.getBytes.length
        if (bufferSize >= MaxBufferSize) {
          doPost(stringBuffer.toString)
          stringBuffer.delete(0, stringBuffer.length)
        }
      }
      case _ => throw new IllegalArgumentException("Bad things")
    }
  }

  /**
   * POSTs a data string to _bulk_docs
   */
  private def doPost(data: String) = {
    val body = s"""{"docs": [$data]}"""
    val future = client
      .url(s"$uri/$db/_bulk_docs")
      .withHeaders(
        "Content-Type" -> "application/json",
        "Authorization" -> ("Basic " + new String(Base64.encodeBase64(s"$username:$password".getBytes)))
      )
      .post(body).map(_.body)
    val response = Await.result(future, Duration.Inf)
    println(response)
  }

}
