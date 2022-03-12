scalaVersion := "2.12.10"

// provided はjar fileに含めないライブラリ、ユーザー側で適切なバージョンを利用
libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "3.0.1" % "provided",
    "org.apache.spark" %% "spark-sql" % "3.0.1" % "provided",
    "com.thesamet.scalapb" %% "sparksql-scalapb" % "0.11.0-RC1"
)

// sparkは古いprotobuf runtimeを使っているので、互換性のない
// shadeで隠す
assemblyShadeRules in assembly := Seq(
    ShadeRule.rename("com.google.protobuf.**" -> "shadeproto.@1").inAll,
    ShadeRule.rename("scala.collection.compat.**" -> "scalacompat.@1").inAll
)

PB.targets in Compile := Seq(
    scalapb.gen() -> (sourceManaged in Compile).value
)
