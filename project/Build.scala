import sbt._
import Keys._
import play.PlayImport.PlayKeys._
//import sbtassembly.Plugin._
//import AssemblyKeys._

object S3IndexBuild extends Build {

  val appName = "S3Index"
  val appVersion = "1.1"
  val scVersion = "2.11.7"

  val playDependencies = Seq(
    "com.typesafe.play" %% "play-cache" % "2.3.9",
    "org.scala-lang.modules" %% "scala-async" % "0.9.2"
  )

  val appDependencies = Seq(
    // Upgraded https://github.com/ntbrock/scala-aws
    "com.codeminders.scalaws" %% "scala-aws-core" % "1.0.0-SNAPSHOT",
    "com.codeminders.scalaws" %% "scala-aws-s3" % "1.0.0-SNAPSHOT",

    // The AWS client is not hte Atlassian one
    //    "io.atlassian.aws-scala" %% "aws-scala-core"  % "4.0.2",
    //    "io.atlassian.aws-scala" %% "aws-scala-s3"  % "4.0.2",
    //    "io.atlassian.aws-scala" %% "aws-scala-core"  % "4.0.2"  % "test" classifier "tests",
    //    "io.atlassian.aws-scala" %% "aws-scala-s3"  % "4.0.2"  % "test" classifier "tests",

    "commons-io" % "commons-io" % "2.4",
    "commons-codec" % "commons-codec" % "1.7",
    "org.apache.httpcomponents" % "httpclient" % "4.2.1",
    "org.scalatest" % "scalatest_2.9.2" % "1.8" % "test",
    "commons-lang" % "commons-lang" % "2.6",
    "com.googlecode.htmlcompressor" % "htmlcompressor" % "1.5.2",
    "rhino" % "js" % "1.7R2",
    "com.yahoo.platform.yui" % "yuicompressor" % "2.4.7",
    "com.google.javascript" % "closure-compiler" % "r2388"
  )

  val root = Project(appName, file("."))
    .enablePlugins(play.PlayScala)
    .settings(
    scalaVersion := scVersion,
      version := appVersion,
      libraryDependencies ++= ( playDependencies ++ appDependencies )
  )
   
}
