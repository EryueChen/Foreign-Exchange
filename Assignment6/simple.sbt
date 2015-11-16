name := "Simple Project"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
"org.apache.spark" %% "spark-core" % "1.5.2",
"org.apache.spark" %% "spark-mllib" % "1.5.2",
"com.datastax.spark" %% "spark-cassandra-connector" % "1.4.0-M3"
)
