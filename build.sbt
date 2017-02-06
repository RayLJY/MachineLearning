name := "MachineLearning"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.6.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.6.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.6.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.6.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka" % "1.6.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-assembly" % "1.6.0" % "provided"

libraryDependencies += "org.apache.hbase" % "hbase" % "1.0.3" % "provided"
libraryDependencies += "org.apache.hbase" % "hbase-common" % "1.0.3" % "provided"
libraryDependencies += "org.apache.hbase" % "hbase-client" % "1.0.3" % "provided"
libraryDependencies += "org.apache.hbase" % "hbase-server" % "1.0.3" % "provided"

// https://mvnrepository.com/artifact/org.lionsoul/jcseg-core
libraryDependencies += "org.lionsoul" % "jcseg-core" % "2.0.0"

assemblyMergeStrategy in assembly := {
  case PathList(ps@_*) if ps.last endsWith "*.xml" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith "*.class" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith "*.jar" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith "*.txt" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith "*.xsd" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith "*.dtd" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith "*.properties" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
