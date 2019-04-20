package com.netease.spark.alarm.email

import com.netease.spark.alarm.AlertMessage
import org.apache.spark.SparkConf
import org.scalatest.FunSuite

class EmailAlarmistTest extends FunSuite {
  private val conf = new SparkConf(true)
  private val message = AlertMessage("title", "content")
  ignore("email alarm") {
    conf.set(HOSTNAME, "smtp-mail.outlook.com")
      .set(FROM, "yaooqinn@hotmail.com")
      .set(SSL_ON_CONNECT, "true")
      .set(USERNAME, "yaooqinn")
      .set(PASSWORD, "*****")
      .set(TO, "yaooqinn@hotmail.com")
    val alarmist = new EmailAlarmist(conf)
    val response = alarmist.alarm(message)
    assert(response.status)
  }

}
