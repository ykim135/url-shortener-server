package irobo.api.dao

import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
import com.github.mauricio.async.db.mysql.exceptions.MySQLException
import irobo.api.model._
import irobo.api.model.dao._
import irobo.api.service._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.mock.MockitoSugar
import utest._

import irobo.common.crypto.{ AwsKmsDecryption, CypherText }
import org.joda.time._

import scala.concurrent.{ Future, ExecutionContext, Await } 
import scala.concurrent.duration._
import scala.util._

object UrlDaoTest extends TestSuite with Matchers with MockitoSugar {
  val tests = this {
    "Url Dao Test" - {
      val pool = TestDatabase.pool

      val urlDao = new UrlDao(pool)

      def insertUrls() = {
        val sql = 
          s"""
          INSERT INTO urls (id, full_url, short_url, full_url_hash) 
          VALUES 
          (1, "abcdfd", "adfasfsd", "abcdef"), 
          (2, "sdfdsf", "adfasfsd", "bcdfdd")
          """

        pool.sendQuery(sql)
      }

      def truncate() = {
        val tables = Seq("urls")

        Future.sequence(tables.map { table =>
          for {
            _ <- pool.sendQuery(s"TRUNCATE TABLE $table")
          } yield ()
        })
      }

      "get url" - {
        for {
          _      <- truncate()
          _      <- insertUrls()
          output <- urlDao.getUrl("abcdef")
        } yield {
          assert(output == Seq.empty)
        }
      }

      "insert url" - {
        val url = Url(
          Some(1), 
          "fdasfsda", 
          Some("sadfdsafdas"), 
          "dafdsaf"
        )

        for {
          _      <- truncate()
          output <- urlDao.insertUrl(url)
        } yield {
          assert(output == 1)
        }
      }

      "update short url" - {
        for {
          _      <- truncate()
          _      <- insertUrls()
          output <- urlDao.updateShortUrl(Some(1), Some("test"))
        } yield assert(output == None)
      }
    }
  }
}
