name := "spark-streaming-sample"

version := "0.1.0"

scalaVersion := "2.12.13"

val sparkVersion = "3.1.1"

// provided はjar fileに含めないライブラリ、ユーザー側で適切なバージョンを利用
libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
    "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
    "com.thesamet.scalapb" %% "sparksql-scalapb" % "0.11.0-RC1"
)

/* assembly comfigurations */

assemblyJarName := s"${name.value}-${version.value}.jar"

mainClass in assembly := Some("example.spark_streaming_kafka.AppMain")

test in assembly := {}

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "build-info.properties") => MergeStrategy.discard
  case PathList(ps @ _*) if ps.last == "module-info.class" => MergeStrategy.discard
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

/* scalapb configurations */

// sparkは古いprotobuf runtimeを使っているので、互換性のない
// shadeで隠す
assemblyShadeRules in assembly := Seq(
    ShadeRule.rename("com.google.protobuf.**" -> "shadeproto.@1").inAll,
    ShadeRule.rename("scala.collection.compat.**" -> "scalacompat.@1").inAll
)

PB.targets in Compile := Seq(
    scalapb.gen() -> (sourceManaged in Compile).value
)
