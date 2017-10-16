package irobo.api.model.dao

import scala.concurrent.{ExecutionContext, Future}

import com.github.mauricio.async.db.mysql.exceptions.MySQLException
import com.github.mauricio.async.db.mysql.MySQLQueryResult
import com.github.mauricio.async.db.{Connection, QueryResult, RowData}

import irobo.common.{Logger, UUIDGenerator}
import irobo.api.model._

import org.joda.time._

class GlobalParamsDao(connection: Connection)(implicit ec: ExecutionContext) extends RowDao[GlobalParams] with Logger {
  val _pool = connection

  def getInstance(row: RowData): GlobalParams = {
    GlobalParams (
      charset     = row("charset").asInstanceOf[String],
      shortDomain = row("short_domain").asInstanceOf[String]
    )
  }

  def getGlobalParams() : Future[Option[GlobalParams]] = {
    val sql = 
      s"""
      SELECT * FROM global_params
      """

    get(sql)
  }
}
