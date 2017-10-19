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

class UrlService(urlDao : UrlDao, globalParamsDao : GlobalParamsDao)(implicit ec : ExecutionContext) extends Logger {
	private[service] def parseFullUrl(fullUrl: String) = {
		fullUrl.replace("https://", "").replace("http://", "")
	}

	// gets the last bit of url which is the key to full url value
	private[service] def parseShortUrl(url: String): Future[Option[String]] = {
		globalParamsDao.getGlobalParams().map { globalParamsOpt =>
			globalParamsOpt.map { globalParams =>
				url.split(globalParams.shortDomain).last
			}
		}
	}

	private[service] def wrapShortUrl(shortUrl: Option[String]): Future[Option[String]] = {
		globalParamsDao.getGlobalParams().map { globalParamsOpt =>
			globalParamsOpt.map { globalParams =>
				globalParams.shortDomain + shortUrl.getOrElse("")
			}
		}
	}

	// hash full url in MD5
	private[service] def getFullUrlHash(fullUrl: String): String = {
		MessageDigest
			.getInstance("MD5")
			.digest(fullUrl.getBytes)
			.map("%02X".format(_))
			.mkString
	}

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

	private[service] def toBase10(str: Option[String]): Future[Option[Long]] = {
		str match {
			case Some(str) =>
				globalParamsDao.getGlobalParams().map { globalParamsOpt =>
					globalParamsOpt.map { globalParams =>
						val digits = str.map { eachChar => globalParams.charset.indexOf(eachChar) }.reverse
						digits.map { digit => digit * scala.math.pow (62, digits.indexOf(digit)) }.sum.toInt
					}
				}
			case _ =>
				Future.successful(None)
		}
	}

	private[service] def toBase62(num: Option[Long]): Future[Option[String]] = {
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

				globalParamsDao.getGlobalParams().map { globalParamsOpt =>
					globalParamsOpt.map { globalParams =>
						digits.map { digit => globalParams.charset.charAt(digit) }.mkString
					}
				}
			case _ =>
				Future.successful(None)
		}
	}

	// 1. get the full url and its hash value
	// 2. see if the hash value exists in the database.
	// 3. if anything is there, return the corresponding short_url
	// 4. else, insert the full url and its hash value. then, get the newly inserted id. make sure full url is in the format www.test.co.kr
	// convert it to base64 to get the short url. Insert the short url into DB.
	def getShortUrl(fullUrl: String): Future[Option[String]] = {
		val parsedFullUrl = parseFullUrl(fullUrl)
		val fullUrlHash   = getFullUrlHash(parsedFullUrl)

		urlDao.getUrl(fullUrlHash).flatMap { urlRow =>
			if (urlRow.isDefined)
				wrapShortUrl(urlRow.flatMap(_.shortUrl))
			else {
				for {
					_        <- insertFullUrlHash(parsedFullUrl, fullUrlHash)
					url      <- urlDao.getUrl(fullUrlHash)
					id        = url.flatMap(_.id)
					shortUrl <- toBase62(id)
					_        <- urlDao.updateShortUrl(id, shortUrl)
					output   <- wrapShortUrl(shortUrl)
				} yield output
			}
		}
	}

	def getFullUrl(shortUrl: String): Future[Option[String]] = {
		for {
			parsedShortUrl <- parseShortUrl(shortUrl)
			id             <- toBase10(parsedShortUrl)
			urlOpt         <- urlDao.getUrlById(id)
		} yield urlOpt.map(_.fullUrl)
	}
}
