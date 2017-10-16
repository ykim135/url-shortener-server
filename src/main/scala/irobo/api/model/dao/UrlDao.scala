package irobo.api.model.dao

import scala.concurrent.{ExecutionContext, Future}

import com.github.mauricio.async.db.mysql.exceptions.MySQLException
import com.github.mauricio.async.db.mysql.MySQLQueryResult
import com.github.mauricio.async.db.{Connection, QueryResult, RowData}

import irobo.common.{Logger, UUIDGenerator}
import irobo.api.model._

import org.joda.time._

class UrlDao(connection: Connection)(implicit ec: ExecutionContext) extends RowDao[Url] with Logger {
  val _pool = connection

  def getInstance(row: RowData): Url = {
    Url (
      id          = Option(row("id")).map(_.asInstanceOf[Long]),
      fullUrl     = row("full_url").asInstanceOf[String],
      shortUrl    = Option(row("short_url")).map(_.asInstanceOf[String]),
      fullUrlHash = row("full_url_hash").asInstanceOf[String]
    )
  }

  def getUrl(fullUrlHash: String) : Future[Option[Url]] = {
    val sql = 
      s"""
      SELECT * FROM urls WHERE full_url_hash = "$fullUrlHash"
      """

    get(sql)
  }

  def getUrlById(id: Option[Long]) : Future[Option[Url]] = {
    if (id.isDefined) {
      val sql = 
        s"""
        SELECT * FROM urls WHERE id = "${id.get}"
        """

        get(sql)
    }
    else Future.successful(None)
  }

  def insertUrl(url: Url): Future[Long] = {
    val sql = 
      s"""
      INSERT INTO urls (full_url, short_url, full_url_hash) 
      VALUES
      (?, ?, ?)
      """

    val input = Array(
      url.fullUrl,
      url.shortUrl.getOrElse(null),
      url.fullUrlHash
    )

    connection.sendPreparedStatement(sql, input).map {
      _ match {
        case result: MySQLQueryResult => result.rowsAffected
        case _                        => throw new Exception(s"url insert failed!")
      }
    }
  }

  def updateShortUrl(
    id       : Option[Long],
    shortUrl : Option[String]
  ): Future[Long] = {
    id match {
      case Some(id) =>
        val sql = 
          s"""
          UPDATE urls 
          SET short_url = ?
          WHERE id = ? 
          """

        connection.sendPreparedStatement(sql, Array(shortUrl.get, id)).map {
          _ match {
            case result: MySQLQueryResult => result.rowsAffected
            case _                        => throw new Exception(s"short url update failed!")
          }
        }
      case _ => Future.successful(0L)
    }
  }
}
