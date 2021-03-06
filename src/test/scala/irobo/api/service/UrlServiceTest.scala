package irobo.api.service

import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
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

import scala.concurrent.{ Future, ExecutionContext } 

object UrlServiceTest extends TestSuite with Matchers with MockitoSugar {
  val tests = this {
    "Url Service Test" - {
      implicit val pool = TestDatabase.pool

      val monixScheduler = monix.execution.Scheduler.Implicits.global

      val urlDao     = new UrlDao(pool)
      val urlService = new UrlService(urlDao)

      def truncate() = {
        val tables = Seq("urls")

        Future.sequence(tables.map { table =>
          for {
            _ <- pool.sendQuery(s"TRUNCATE TABLE $table")
          } yield ()
        })
      }

      "prettify full url" - {
        val fullUrl = "https://google.com"

        val output = urlService.prettifyFullUrl(fullUrl)

        assert(output == "google.com")
      }

      "parse short url" - {
        val url = "my.kr/test"

        val output = urlService.parseShortUrl(url)

        assert(output == "test")
      }

      "wrap short url" - {
        val shortUrl = "a"

        val output = urlService.wrapShortUrl(shortUrl)

        assert(output == None)
      }

      "get hashed full url" - {
        val output = urlService.getFullUrlHash("test")

        assert(output == None)
      }

      "to base 62" - {
        val output = urlService.toBase62(Some(213890L))

        assert(output == None)
      }

      "to base 10" - {
        val output = urlService.toBase10("cb")

        assert(output == None)
      }

      "get short url" - {
        for {
          _       <- truncate()
          output1 <- urlService.getShortUrl("www.naver.com")
          output2 <- urlService.getShortUrl("www.naver.com")
        } yield {
          println(output1)
          println(output2)

          assert(output1 == output2)
        }
      }

      "get full url" - {
        for {
          output <- urlService.getFullUrl("d")
        } yield assert(output == None)
      }
    }
  }
}
