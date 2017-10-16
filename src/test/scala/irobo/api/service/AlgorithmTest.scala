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

object AlgorithmTest extends TestSuite with Matchers with MockitoSugar {
  val tests = this {
    "Algorithm Test" - {
      val algorithm = new Algorithm()

      "get first monday date" - {
        val jan = algorithm.getFirstMondayDate(1)
        val feb = algorithm.getFirstMondayDate(2)

        assert(jan == new LocalDate(2017, 1, 2))
        assert(feb == new LocalDate(2017, 2, 6))
      }
    }
  }
}
