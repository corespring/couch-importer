name := "couch-importer"

version := "0.0.1"

scalaVersion := "2.10.4"

resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases" at "http://oss.sonatype.org/content/repositories/releases",
  "typesafe releases" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core" % "2.4.3",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.3",
  "com.typesafe.play" %% "play-json" % "2.3.6",
  "com.typesafe.play" %% "play-ws" % "2.3.6"
)