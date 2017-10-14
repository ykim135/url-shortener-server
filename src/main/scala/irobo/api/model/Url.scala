package irobo.api.model

case class Url (
  id          : Option[Long],
  fullUrl     : String,
  shortUrl    : Option[String],
  fullUrlHash : String
)
