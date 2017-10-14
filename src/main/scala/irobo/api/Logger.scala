package irobo.api

import org.slf4j.LoggerFactory

/**
 * Mix-in this trait to get access to logger
 */
trait Logger {
  final val logger = LoggerFactory.getLogger(getClass)
  final val criticalLogger = org.slf4j.LoggerFactory.getLogger("critical")
}

object Logger extends Logger {
  def getLogger(clazz: Class[_]) = LoggerFactory.getLogger(clazz)
  def getCriticalLogger = org.slf4j.LoggerFactory.getLogger("critical")
}
