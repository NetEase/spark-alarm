package com.netease.spark.alarm.smilodon

import com.netease.spark.alarm.smilodon.channel.{ChannelType, SmilodonChannel}
import com.netease.spark.alarm.smilodon.event.SmilodonEvent
import org.apache.spark.SparkConf
import org.scalatest.FunSuite

import scala.collection.JavaConverters._

class SmilodonAlarmistTest extends FunSuite {
  private val conf = new SparkConf(true)
  private val event = SmilodonEvent("TEST", "ALERT", "TASK_FAILED")

  ignore("smilodon alarm") {
    val stone = SmilodonChannel.stone()
    val popo = SmilodonChannel(ChannelType.POPO, "title", "spark 服务有异常吗？")
    val msg = SmilodonAlertMessage(
      SmilodonAlertLevel.WARN,
      event,
      "Spark 服务异常",
      "Spark 服务没什么异常",
      Seq(stone, popo).asJava,
      List("yaooqinn@hotmail.com").asJava)

    val alarmist = new SmilodonAlarmist(conf)
    val response = alarmist.alarm(msg)
    assert(response.status)
  }

  ignore("smilodon alarm using spark conf") {
    conf.set("spark.alarm.smilodon.users", "yaooqinn@hotmail.com")
      .set("spark.alarm.smilodon.channels", "POPO,STONE")

    val msg = SmilodonAlertMessage(
      SmilodonAlertLevel.WARN,
      event,
      "Spark 服务异常",
      "Spark 服务没什么异常",
      conf)

    val alarmist = new SmilodonAlarmist(conf)
    val response = alarmist.alarm(msg)
    assert(response.status)
  }

}
