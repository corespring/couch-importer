## couch-importer

This project uploads a previously exported CouchDB database JSON file to another CouchDB database. You'd use the
`_all_docs` endpoint of your database to obtain this file, typically like:

    curl -X GET http://my_couch_db.hostname.com/db_name/_all_docs\?include_docs\=true > exported_db.json


### Usage

The main SBT task is run with the input JSON file as the sole argument:

    sbt "run exported_db.json"


You will first need to [install sbt](http://www.scala-sbt.org/0.13/tutorial/Setup.html).

A few environment variables are also required:

| Environment Variable  | Purpose |
| --------------------- | ------- |
| ENV_COUCHDB_URI       | Target URI to which to import |
| ENV_CLOUDANT_USERNAME | If importing to Cloudant, the username of the user with `_writer` permission |
| ENV_CLOUDANT_PASSWORD | If importing to Cloudant, the password of the user with `_writer` permission |