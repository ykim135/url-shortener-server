package irobo.api

import java.time.Instant
import scala.util.{ Failure, Success, Try }

import com.github.mauricio.async.db.mysql.MySQLConnection
import com.github.mauricio.async.db.mysql.pool.MySQLConnectionFactory
import com.github.mauricio.async.db.pool.{ ConnectionPool, PoolConfiguration }
import com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext
import com.github.mauricio.async.db.{ Configuration, Connection, QueryResult, RowData }
import com.twitter.finagle.Http
import com.twitter.finagle.http._
import com.twitter.util.Await
import com.typesafe.config._
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import pdi.jwt.{ Jwt, JwtCirce, JwtAlgorithm, JwtClaim }

import irobo.api.endpoint._
import irobo.api.model._
import irobo.api.model.dao._
import irobo.api.service._
import irobo.common.crypto.{AwsKmsDecryption, AwsKmsEncryption, RSA}
import JsonEncoder._

import scala.concurrent._

object Server extends App with Logger {

  /**
   * ********************************************************************************
   * **  Configurations
   * ********************************************************************************
   */
  logger.info("Loading configuration settings")

  val config = ConfigFactory.load()
  val port   = config.getInt("port")

  val authUrl     = config.getString("authUrl")
  val authPort    = config.getInt("authPort")

  val pool = {
    val mySqlConfig = config.getConfig("mySqlConfig")
    val dbUserName  = mySqlConfig.getString("username")
    val dbHost      = mySqlConfig.getString("host")
    val dbPort      = Option(mySqlConfig.getInt("port")).getOrElse(3306)
    val dbPassword  = Option(mySqlConfig.getString("password")).filterNot(_.isEmpty)
    val database    = Option(mySqlConfig.getString("database"))
    val conf        = new Configuration(
      username = dbUserName,
      host     = dbHost,
      port     = dbPort,
      password = dbPassword,
      database = database
    )

    val factory    = new MySQLConnectionFactory(conf)
    val poolConfig = PoolConfiguration(
			maxObjects   = 10,
			maxIdle      = 4,
			maxQueueSize = 100000
		)

    new ConnectionPool(factory, poolConfig)
  }

  val monixScheduler              = 
    monix.execution.Scheduler.Implicits.global
  val transactionExecutionContext = 
    com.github.mauricio.async.db.util.ExecutorServiceUtils.CachedExecutionContext

  /******************************************************************/
  /** DAO Setups
   ******************************************************************/
  
  val urlDao = new UrlDao(pool)

  /**
   * ********************************************************************************
   * **  Services
   * ********************************************************************************
   */

  val urlService = new UrlService(urlDao)

  /**
   * ********************************************************************************
   * **  Endpoints
   * ********************************************************************************
   */

  logger.info("Creating end point instances")

  val urlEndPoint = new UrlEndPoint(urlService)

  val CORSHandler: Endpoint[Unit] = options(*) {
    NoContent[Unit]
      .withHeader("Access-Control-Allow-Origin" -> "*")
      .withHeader("Access-Control-Allow-Methods" -> "PUT, POST, GET, OPTIONS")
      .withHeader("Access-Control-Max-Age" -> "10000000000")
      .withHeader("Access-Control-Allow-Headers" -> "jwt-token, origin, x-csrftoken, content-type, accept")
  }

  val routes = 
    urlEndPoint.getShortUrl() :+:
    urlEndPoint.getFullUrl() :+:
    CORSHandler

  /**
   * ********************************************************************************
   * **  Graceful Shutdown Hook
   * ********************************************************************************
   */
  logger.info("Adding a shutdown hook")

  val shutDownHook = new Runnable {
    def run() {
      logger.info("gracefully shutting down the server")
      pool.close
    }
  }

  Runtime.getRuntime.addShutdownHook(new Thread(shutDownHook))

  /**
   * ********************************************************************************
   * **  Deploy the server
   * ********************************************************************************
   */
  logger.info("Starting the server listening at the port {}", port)

  val server = Http.serve(":" + port, routes.toService)
  Await.ready(server)
}

