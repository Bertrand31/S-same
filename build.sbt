name := "Sésame"

version := "0.1"

scalaVersion := "3.2.2"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.7.0",
  "org.typelevel" %% "cats-effect" % "3.3.14",
  "org.apache.commons" % "commons-math3" % "3.6.1",
  "com.aerospike" % "aerospike-client" % "latest.integration",
  "io.circe" %% "circe-core" % "0.15.0-M1",
  "io.circe" %% "circe-parser" % "0.15.0-M1",

  // Tests
  "org.scalactic" %% "scalactic" % "3.2.13",
  "org.scalatest" %% "scalatest" % "3.2.13" % "test",
)

scalacOptions ++= Seq(
  "-deprecation", // Warn about deprecated features
  "-encoding", "UTF-8", // Specify character encoding used by source files
  "-feature", // Emit warning and location for usages of features that should be imported explicitly
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:higherKinds", // Allow higher-kinded types
  "-unchecked", // Enable additional warnings where generated code depends on assumptions
)

javaOptions ++= Seq(
  "-XX:+CMSClassUnloadingEnabled", // Enable class unloading under the CMS GC
  "-Xms10g",
  "-Xmx14g",
)

// Test suite settings
Test / fork := true
