val scala3Version = "3.1.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Sésame",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.typelevel" %% "cats-core" % "2.7.0",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "3.3.14",
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6.1",
    libraryDependencies += "org.rocksdb" % "rocksdbjni" % "7.3.1"
  )
