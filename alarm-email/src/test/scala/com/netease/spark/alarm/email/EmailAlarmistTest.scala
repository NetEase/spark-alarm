package com.netease.spark.alarm.email

import com.netease.spark.alarm.AlertMessage
import org.apache.spark.SparkConf
import org.scalatest.FunSuite

class EmailAlarmistTest extends FunSuite {
  private val conf = new SparkConf(true)
  private val message = AlertMessage("title", "content")
  ignore("email alarm") {
    conf.set(HOSTNAME, "corp.netease.com")
      .set(FROM, "hzyaoqin@corp.netease.com")
      .set(SSL_ON_CONNECT, "true")
      .set(USERNAME, "hzyaoqin")
      .set(PASSWORD, "***")
      .set(TO, "hzyaoqin@corp.netease.com,yaooqinn@hotmail.com, 11215016@zju.edu.cn")
    val alarmist = new EmailAlarmist(conf)
    val response = alarmist.alarm(message)
    assert(response.status)
  }

}
