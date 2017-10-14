package irobo.api.model

case class ShortUrl (
	status : String,
	msg    : String,
	url    : Option[String]
)

case class FullUrl (
	status : String,
	msg    : String,
	url    : Option[String]
)
