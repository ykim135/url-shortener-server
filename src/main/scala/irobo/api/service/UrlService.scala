package irobo.api.service

import irobo.api.Format
import irobo.api.model._
import irobo.api.model.dao._
import irobo.api.service._
import irobo.common.Logger
import scala.concurrent.{ Future, ExecutionContext }

import com.twitter.finagle._
import com.twitter.finagle.http._

import java.security.MessageDigest

class UrlService(urlDao: UrlDao)(implicit ec : ExecutionContext) extends Logger {
	val charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
  
  def wrapShortUrl(shortUrl: String) = 
    s"my.kr/$shortUrl"

  def parseShortUrl(url: String) = 
    url.split("my.kr/").tail

	// hash full url in MD5
	private[service] def getFullUrlHash(fullUrl: String): String = 
		MessageDigest
			.getInstance("MD5")
			.digest(fullUrl.getBytes)
			.map("%02X".format(_))
			.mkString

	// hash full url and insert it with the original full url
	private[service] def insertFullUrlHash(
		fullUrl     : String,
		fullUrlHash : String
	): Future[Long] = {
		val urlValue = Url(
			id          = None,
			fullUrl     = fullUrl,
			shortUrl    = None,
			fullUrlHash = fullUrlHash
		)
		
		urlDao.insertUrl(urlValue)
	}
  
  def toBase10(str: String): Int = {
    val digits = str.map { eachChar => charset.indexOf(eachChar) }.reverse

    digits.map { digit => digit * scala.math.pow (62, digits.indexOf(digit)) }.sum.toInt
  }

  def toBase62(num: Option[Long]): Option[String] = {
    @scala.annotation.tailrec 
    def loop(
      num    : Long,
      digits : Seq[Int] = Seq.empty[Int]
    ): Seq[Int] = {
      if (num <= 0) digits.reverse
      else {
        val newDigits: Seq[Int] = digits :+ (num % 62).toInt
        val newNum: Long        = num / 62

        loop(newNum, newDigits)
      }
    }

    num match {
      case Some(num) =>
        val digits = loop(num) 
        val output = digits.map { digit => charset.charAt(digit) }.mkString

        Some(output)
      case _ =>
        None
    }
  }

	// 1. get the full url and its hash value
	// 2. see if the hash value exists in the database. 
	// 3. if anything is there, return the corresponding short_url
	// 4. else, insert the full url and its hash value. then, get the newly inserted id.
	// convert it to base64 to get the short url. Insert the short url into DB. 
	def getShortUrl(fullUrl: String): Future[Option[String]] = {
    val fullUrlHash = getFullUrlHash(fullUrl)

    urlDao.getUrl(fullUrlHash).flatMap { urlRow =>
      if (urlRow.isDefined) 
        Future.successful(urlRow.flatMap(_.shortUrl))
      else {
        for {
          _        <- insertFullUrlHash(fullUrl, fullUrlHash)
          url      <- urlDao.getUrl(fullUrlHash)
          id        = url.flatMap(_.id)
          shortUrl  = toBase62(id)
          _        <- urlDao.updateShortUrl(id, shortUrl)
        } yield shortUrl
      }
    }
	}

  def getFullUrl(shortUrl: String): Future[Option[String]] = {
    val id = toBase10(shortUrl) 

    urlDao.getUrlById(id).map { urlOpt => urlOpt.map(_.fullUrl) }
  }
}
