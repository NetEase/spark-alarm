package com.netease.spark.alarm

package object email {

  private val PREFIX = "spark.alarm.email."
  val HOSTNAME = PREFIX + "smtp.host"
  val SMTP_PORT = PREFIX + "smtp.port"
  val USERNAME = PREFIX + "username"
  val PASSWORD = PREFIX + "password"
  val SSL_ON_CONNECT = PREFIX + "ssl.on.connect"
  val FROM = PREFIX + "from"
  val TO = PREFIX + "to"
}
