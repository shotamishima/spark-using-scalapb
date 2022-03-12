// scalapbと必要なプラグインは同じ。Sparkに合わせてバージョンを変更

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.1")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.10.11"
