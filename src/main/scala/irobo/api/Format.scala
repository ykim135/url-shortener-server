package irobo.api

object Format {
  def tryStringToInt(a: String): Option[Int] =
    if(a == "") None else Some(a.toInt)
}
