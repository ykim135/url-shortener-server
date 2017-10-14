package irobo.api.model.dao

import com.github.mauricio.async.db.mysql.exceptions.MySQLException
import com.github.mauricio.async.db.mysql.MySQLQueryResult
import com.github.mauricio.async.db.{Connection, QueryResult, RowData}

import irobo.common.{Logger, UUIDGenerator}
import irobo.api.model._

import scala.concurrent.{ExecutionContext, Future}

import org.joda.time._

trait RowDao[T] extends Logger {
  def _pool : Connection

  def getInstance(row: RowData): T 

  def get(sql: String)(implicit ec: ExecutionContext): Future[Option[T]] = {
    _pool.sendQuery(sql).map { result =>
      result.rows match {
        case Some(rows) => rows.map { getInstance }.headOption
        case None       => None
      }
    }
  }

  def list(sql: String)(implicit ec: ExecutionContext): Future[Seq[T]] = {
    _pool.sendQuery(sql).map { result =>
      result.rows match {
        case Some(rows) => rows.map { getInstance }
        case None       => throw new Exception(s"get op failed")
      }
    }
  }
}
