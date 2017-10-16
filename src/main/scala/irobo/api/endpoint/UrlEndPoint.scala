package irobo.api.endpoint

import scala.concurrent.{ ExecutionContext, Future => SFuture, Promise => SPromise }
import scala.util.{ Failure, Success }

import com.twitter.finagle.Http
import com.twitter.finagle.http._
import com.twitter.util.{ Return, Throw, Future => TFuture, Promise => TPromise }

import io.circe.generic.auto._
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import io.finch._
import io.finch.circe._

import irobo.api.service._
import irobo.api.model._
import irobo.api.JsonDecoder._
import irobo.api.JsonEncoder._

import scala.util.matching.Regex
import scala.concurrent.Future

import cats.syntax.either._

class UrlEndPoint(urlService: UrlService)(implicit executionContext: ExecutionContext) {
  def getShortUrl(): Endpoint[ShortUrl] = {
    post(
      "api"   :: 
      "short" :: 
      body.as[Json]
    ) { json : Json =>
			val doc: Json = parse(json.toString).getOrElse(Json.Null)

			val fullUrl: Option[String] = doc.hcursor.get[String]("full_url").toOption

			val pattern: Regex = """^((https?|ftp|smtp):\/\/)?(www.)?[a-z0-9]+\.[a-z]+(\/[a-zA-Z0-9#]+\/?)*$""".r

			val validatedUrl = fullUrl.flatMap { url => pattern.findFirstIn(url) }

      val output = 
        validatedUrl match {
          case Some(fullUrl) =>
            urlService.getShortUrl(fullUrl).map { shortUrl =>
              Ok(ShortUrl("2000", "Success", shortUrl)).withHeader("Access-Control-Allow-Origin" -> "*")
            }
          case None =>
            Future.successful(Ok(ShortUrl("4000", "Invalid url: please check your url", None)).withHeader("Access-Control-Allow-Origin" -> "*"))
        }

      output.asTwitter
    }
  }

  def getFullUrl(): Endpoint[FullUrl] = {
    post(
      "api"  :: 
      "full" :: 
      body.as[Json]
    ) { json : Json =>
			val doc: Json = parse(json.toString).getOrElse(Json.Null)

			val shortUrl: Option[String] = doc.hcursor.get[String]("short_url").toOption

      val output = 
        shortUrl match {
          case Some(shortUrl) =>
            urlService.getFullUrl(shortUrl).map { fullUrl =>
              val status =  
                if (fullUrl.isDefined) FullUrl("2000", "Success", fullUrl)
                else FullUrl("4002", "No matching full url", fullUrl)

              Ok(status).withHeader("Access-Control-Allow-Origin" -> "*")
            }
          case None =>
            Future.successful(Ok(FullUrl("4001", "Cannot interpret shortened url", None)).withHeader("Access-Control-Allow-Origin" -> "*"))
        }

      output.asTwitter
    }
  }
}
