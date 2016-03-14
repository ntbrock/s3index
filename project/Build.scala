import sbt._
import Keys._
import play.PlayImport.PlayKeys._
//import sbtassembly.Plugin._
//import AssemblyKeys._

object S3IndexBuild extends Build {

  val appName = "S3Index"
  val appVersion = "1.1"
  val scVersion = "2.11.7"

  val appDependencies = Seq(
    //	"com.codeminders.scalaws" % "scala-aws-s3_2.9.2" % "1.0.0-SNAPSHOT",
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
      libraryDependencies ++= appDependencies
  )
   
}
