package irobo.api

import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Encoder
import io.circe.Json

import irobo.api.model._

object JsonEncoder {
  implicit val EncodeShortUrl: Encoder[ShortUrl] = new Encoder[ShortUrl] {
    override def apply(a: ShortUrl) = 
    Json.obj (
			"status" -> Json.fromString(a.status),
			"msg"    -> Json.fromString(a.msg),
			"url"    -> Json.fromString(a.url.getOrElse(""))
    )
  }

  implicit val EncodeFullUrl: Encoder[FullUrl] = new Encoder[FullUrl] {
    override def apply(a: FullUrl) = 
    Json.obj (
			"status" -> Json.fromString(a.status),
			"msg"    -> Json.fromString(a.msg),
			"url"    -> Json.fromString(a.url.getOrElse(""))
    )
  }
}
