package irobo.api.service

import irobo.common.Logger
import irobo.api.service.FutureConvert._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import java.nio.charset.StandardCharsets
import java.net._

import com.twitter.finagle.builder.{ClientBuilder, Cluster}
import com.twitter.finagle.builder.Cluster.Change
import com.twitter.util
import com.twitter.concurrent.{SpoolSource, Spool}
import com.twitter.finagle.http.{Request, Response, RequestBuilder}
import com.twitter.conversions.time._
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.service.{Backoff, RetryBudget}

/**
 *  * Companion object for HttpInvoker mainly used to get a instanace of HttpInvoker class
 *   *
 *    * @author Eun Song
 *     */
object HttpInvoker {
  def apply(
    host       : String,
    port       : Int,
    maxSize    : Int = 2,
    maxWaiters : Int = 1000
  )(implicit ctx: ExecutionContext) = new HttpInvoker(host, port, maxSize, maxWaiters)
}

class HttpInvoker private (
  host       : String,
  port       : Int,
  maxSize    : Int,
  maxWaiters : Int
)(implicit ctx: ExecutionContext) {
  private val logger   = Logger.getLogger(this.getClass)
  private val protocol = if(port == 443) "https" else "http"
  private val client   = ClientBuilder()
    .codec(com.twitter.finagle.http.Http())
    .cluster(new DnsResolvingCluster(host, port))
    .hosts(s"""$host:$port""")
    .tls(s"""$host""")
    .hostConnectionLimit(maxSize)
    .hostConnectionCoresize(maxSize / 2)
    .hostConnectionMaxWaiters(maxWaiters)
    .keepAlive(true)
    .failFast(false)
    .build()

  def sendPostJson(uri: String, json: String): Future[Response] = {
    logger.info(s"Sending a POST request to $uri")

    val request = RequestBuilder()
      .url(new java.net.URL(protocol, host, port, s"/${uri}"))
      .setHeader("Content-Type", "application/json")
      .buildPost(com.twitter.io.Buf.Utf8(json))
      val f = client(request).asScala

    f onComplete {
      case Success(response) =>
				logger.info(s"==============================================")
        logger.debug(s"Succeed to get response: ${response.getStatusCode()}}")
				logger.info(s"==============================================")
				logger.debug(s"Response : ${response.getContentString()}")
				logger.info(s"==============================================")
      case Failure(t) =>
        logger.debug(s"Fail to get response: $t")
        t.printStackTrace()
    }

    f
  }

  def sendGet(uri: String, queryParams: Map[String, String] = Map.empty[String, String]): Future[Response] = {
    val queryParamsStr     = s"""${queryParams.map(v => s"${v._1}=${URLEncoder.encode(v._2, "UTF-8")}").mkString("&")}"""
    val uriWithQueryParams = s"""$uri?$queryParamsStr"""
    val request            = RequestBuilder()
      .url(new java.net.URL(protocol, host, port, uriWithQueryParams))
      .buildGet()

    logger.info(s"Sending a GET request to $uri")

    val f = client(request).asScala

    f onComplete {
      case Success(response) =>
        logger.debug(s"Succeed to get response: ${response.getStatusCode()}\n${response.getContentString()}")
      case Failure(t) =>
        logger.debug(s"Fail to get response: $t")
        t.printStackTrace()
    }

    f
  }
}

/**
 *  * Twitter cluster implementation which refreshes DNS entries.
 *   */
object DnsResolvingCluster extends Logger {

  val DnsTtl = 1000 * 60 * 5

  class HostToResolve(val host: String, val port: Int, var current: Seq[SocketAddress], val outgoing: SpoolSource[Change[SocketAddress]])

  @volatile private var started = false
  private val resolvers = new java.util.concurrent.ConcurrentHashMap[String, HostToResolve]()

  def addResolver(host: String, port: Int, outgoing: SpoolSource[Change[SocketAddress]]): Seq[SocketAddress] = {
    val current = resolve(host, port)
    resolvers.put(host + ":" + port, new HostToResolve(host, port, current, outgoing))
    synchronized {
      if (!started) {
        started = true
        start()
      }
    }
    current
  }

  def resolve(host: String, port: Int): Seq[SocketAddress] = {
    val hosts = InetAddress.getAllByName(host)
    val res = hosts.filter(a => a.isInstanceOf[Inet4Address]).map(a => new InetSocketAddress(a.getHostAddress, port))
    logger.info(host + " resolved to " + res.map(_.getAddress.getHostAddress).mkString(", "))
    res
  }

  private def update(h: HostToResolve) {
    val updated = resolve(h.host, h.port)
    val added = updated.filter(!h.current.contains(_))
    val removed = h.current.filter(!updated.contains(_))
    added.map(Cluster.Add.apply).foreach(h.outgoing.offer)
    removed.map(Cluster.Rem.apply).foreach(h.outgoing.offer)
    h.current = updated
  }


  private def start() {
    val t = new Thread("DNS Resolver for HTTPInvoker") {
      override def run() {
        while (true) {
          Thread.sleep(DnsTtl)
          import scala.collection.JavaConverters._
          resolvers.asScala.values.foreach(update)
        }
      }
    }

    t.start()
  }
}

class DnsResolvingCluster(host: String, port: Int, dnsTtlMillis: Long = 5 * 60 * 1000) extends Cluster[SocketAddress] with Logger {

  def snap: (Seq[SocketAddress], util.Future[Spool[Change[SocketAddress]]]) = {
    val outgoing = new SpoolSource[Change[SocketAddress]]
    val current = DnsResolvingCluster.addResolver(host, port, outgoing)
    current -> outgoing()
  }
}

