package model

import model.Template._

import play.api.templates.Html
import play.api._



import com.codeminders.scalaws.s3.AWSS3
import com.codeminders.scalaws.s3.api.Keys
import com.codeminders.scalaws.s3.model.S3ObjectSummary
import com.codeminders.scalaws.utils.DateUtils

// Play 2.3.9 Migration forward to twirl
import play.twirl.api._
// import play.templates.ScalaTemplateCompiler
// import Template._
import FilesListFormat._
import org.apache.commons.io.FilenameUtils
import scala.util.matching.Regex
import java.net.URL
import scala.collection.mutable.ArrayBuffer

class IndexGenerator(s3client: AWSS3, backReference: String, maxKeys: Int) {

  val footer = Html("""Generated by <a href="%s">S3Index</a>""".format(backReference))

  def generate(properties: Properties, prefix: String = "", marker: Int = 1): Html = {
    val printableIndexRange = (marker - 5 until marker + 5).map((1 - (marker - 5)) + _)
    val filter = keysFilter(properties.includedPaths.foldLeft(List[Regex]())((l, p) => Utils.globe2Regexp(p) :: l),
      properties.excludedPaths.foldLeft(List[Regex]())((l, p) => Utils.globe2Regexp(p) :: l))(_)
    val list = s3client(properties.bucketName).list(prefix = prefix, delimiter = "/")
    val filteredList = list.take(maxKeys).filter(e => filter(e)).sortBy(e => e.isLeft).toArray
    val printableList = filteredList.drop(properties.maxKeys * (marker - 1)).take(properties.maxKeys)
    val keys = printableList.filter(_.isLeft).map(_.left.get)
    val commonPrefexes = printableList.filter(_.isRight).map(_.right.get.prefix)
    val index = filteredList.zipWithIndex.filter(e => printableIndexRange.contains((e._2 / properties.maxKeys) + 1)).foldLeft(ArrayBuffer.empty[Int]) {
      (a, e) =>
        if ((e._2 % properties.maxKeys) == 0) a += (((e._2 / properties.maxKeys) + 1))
        else a
    }.toArray
    html(properties.bucketName, prefix, keys, commonPrefexes, properties.template, properties.filesListFormat, (marker, index))
  }

  def html(bucketName: String, prefix: String, keys: Seq[S3ObjectSummary], prefexes: Seq[String], template: Template, filesFormat: FilesListFormat, index: (Int, Seq[Int])): Html = {

    def url(k: String): String = "http://%s.s3.amazonaws.com/%s".format(bucketName, k)

    val parentPrefix = if (prefix.endsWith("/")) FilenameUtils.getPath(prefix.dropRight(1)) else FilenameUtils.getPath(prefix)

    val headers = Seq(headerLine(filesFormat))

    val parentLink = if (!prefix.isEmpty()) {
      Seq(Seq(Html("""<div class="back"><a href="#%s">..</a></div>""".format(parentPrefix))))
    } else Seq.empty

    val directories = for (g <- prefexes) yield {
      Seq(Html("""<div class="dir"><a href="#%s">%s</a></div>""".format(g, g.substring(prefix.length()))))
    }

    val files = for (o <- keys) yield {
      fileLine(filesFormat, prefix, o, url)
    }

    val data = headers ++ parentLink ++ directories ++ files

    val indexPrev = if (index._1 - 1 > 0) {
      Seq(Html("""<a href="#%s&%s" class="prev" >Previous</a>""".format(prefix, index._1 - 1)))
    } else Seq.empty[Html]
    val indexNext = if (index._1 + 1 <= index._2.size) {
      Seq(Html("""<a href="#%s&%s" class="prev" >Next</a>""".format(prefix, index._1 + 1)))
    } else Seq.empty[Html]
    val indexHtml = indexPrev ++ (for (idx <- index._2) yield {
      if (index._1 == idx) {
        Html("""<div class="current">%s</div>""".format(idx))
      } else {
        Html("""<div class="marker"><a href="#%s&%s" class="fl">%s</a></div>""".format(prefix, idx, idx))
      }
    }) ++ indexNext

    template match {
      case Simple =>
        views.html.templates.simple(if (prefix.isEmpty()) "/" else "/" + prefix, data, indexHtml, footer, backReference)
      case Slim =>
        views.html.templates.slim(if (prefix.isEmpty()) "/" else "/" + prefix, data, indexHtml, footer, backReference)
    }
  }

  def keysFilter(includedKeys: Seq[Regex], excludedKeys: Seq[Regex])(e: Either[com.codeminders.scalaws.s3.model.S3ObjectSummary, com.codeminders.scalaws.s3.api.Keys]): Boolean = {
    val name = e match {
      case Left(obj) => obj.key
      case Right(p) => p.prefix
    }
    if (!includedKeys.isEmpty) {
      includedKeys.exists(i => i.pattern.matcher(name).matches())
    } else true && !excludedKeys.exists(e => e.pattern.matcher(name).matches())
  }

  private def headerLine(f: FilesListFormat): Seq[Html] = {
    f match {
      case Standard => Seq(Html("Name"), Html("Last Modified"), Html("Size"))
      case Brief => Seq(Html("Name"), Html("Size"))
      case Full => Seq(Html("Name"),
        Html("Last Modified"),
        Html("MD5"),
        Html("Size"),
        Html("Storage Class"),
        Html("Owner"))
    }
  }

  private def fileLine(f: FilesListFormat, p: String, o: S3ObjectSummary, url: (String) => String): Seq[Html] = {
    f match {
      case Standard => Seq(Html("""<div class="file"><a href="%s">%s</a></div>""".format(url(o.key), o.key.substring(p.length()))),
        Html(DateUtils.formatRfc822Date(o.lastModified)),
        Html(o.size.toString))
      case Brief => Seq(Html("""<div class="file"><a href="%s">%s</a></div>""".format(url(o.key), o.key.substring(p.length()))), Html(o.size.toString))
      case Full => Seq(Html("""<div class="file"><a href="%s">%s</a></div>""".format(url(o.key), o.key.substring(p.length()))),
        Html(DateUtils.formatRfc822Date(o.lastModified)),
        Html(o.etag),
        Html(o.size.toString),
        Html(o.storageClass.toString()),
        Html(o.owner.displayName))
    }
  }

}