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
import org.joda.time._

class Algorithm() {
	def getFirstMondayDate(month : Int): LocalDate = {
		val firstDayOfMonth = new LocalDate(2017, month, 1) // 2017-01-01

		val dayOfFirstDayOfMonth = firstDayOfMonth.getDayOfWeek() // 2017-01-01 is Monday, Tuesday, etc.

		if (dayOfFirstDayOfMonth == 1) firstDayOfMonth// today is Monday
		else {
			val weekFromFirstDayOfMonth = firstDayOfMonth.plusDays(7) 
			val mondayDate = weekFromFirstDayOfMonth.withDayOfWeek(DateTimeConstants.MONDAY)

			mondayDate
		}
	}
}
