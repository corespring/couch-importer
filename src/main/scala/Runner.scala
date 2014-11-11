package scala

import java.net.URL

import org.corespring.couchdb.Importer

object Runner extends App {

  override def main(args: Array[String]) = args.headOption match {
    case Some(filename) => {
      var url = new URL(s"file://$filename")
      Importer.doImport(url)
    }
    case _ => println(s"""Usage: sbt "run exported_db.json"""")
  }

}
