name := "MachineLearning"

version := "1.0"

sbtVersion := "0.13.11"
scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.6.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.6.0"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.6.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.6.0"

// https://mvnrepository.com/artifact/org.lionsoul/jcseg-core
libraryDependencies += "org.lionsoul" % "jcseg-core" % "2.0.0"
// libraryDependencies += "io.github.zacker330.es" % "ik-analysis-core" % "1.0.0"
libraryDependencies += "cn.bestwu" % "ik-analyzers" % "5.1.0"
