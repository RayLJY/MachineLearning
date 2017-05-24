name := "MachineLearning"

version := "1.0"

sbtVersion := "0.13.15"
scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.1.1"
libraryDependencies += "org.apache.spark" % "spark-mllib_2.11" % "2.1.1"
libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "2.1.1"
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.1.1"
libraryDependencies += "org.apache.spark" % "spark-graphx_2.11" % "2.1.1"

// https://mvnrepository.com/artifact/org.lionsoul/jcseg-core
libraryDependencies += "org.lionsoul" % "jcseg-core" % "2.0.0"
// libraryDependencies += "io.github.zacker330.es" % "ik-analysis-core" % "1.0.0"
libraryDependencies += "cn.bestwu" % "ik-analyzers" % "5.1.0"
