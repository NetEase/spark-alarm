package com.netease.spark.alarm.sentry

import com.netease.spark.alarm.AlertMessage
import org.apache.spark.SparkConf
import org.scalatest.FunSuite

class SentryAlarmistTest extends FunSuite {

  private val conf = new SparkConf(true)
  private val message = AlertMessage("title", "content")

  ignore("sentry alarm") {
    conf.set(SENTRY_API_TYPE, "Stone")
      .set(SENTRY_APP_SECRET, "da1ce204-52aa-4d6a-835b-ea659dd2d0d3")
      .set(SENTRY_TO_LIST, "yaooqinn@hotmail.com")
      .set(SENTRY_APP_NAME, "NeSparkLogAnalyzer")
    val alarmist = new SentryAlarmist(conf)
    val response = alarmist.alarm(message)
    assert(!response.status)
  }

  ignore("smilodon alarm") {
    conf.set(SENTRY_API_TYPE, "Stone")
      .set(SENTRY_APP_SECRET, "4204243f-30bb-4bb9-9bdc-50f034778671")
      .set(SENTRY_TO_LIST, "hzyaoqin")
      .set(SENTRY_APP_NAME, "smilodon")
    val alarmist = new SentryAlarmist(conf)
    val response = alarmist.alarm(message)
    assert(response.status)
  }
}
