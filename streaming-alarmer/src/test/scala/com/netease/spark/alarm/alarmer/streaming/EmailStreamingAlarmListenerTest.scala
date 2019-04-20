package com.netease.spark.alarm.alarmer.streaming

import com.netease.spark.alarm.email._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming._
import org.scalatest.FunSuite

class EmailStreamingAlarmListenerTest extends FunSuite {
  private val conf = new SparkConf(true)
    .setMaster("local[2]")
    .setAppName("smilodon")
    .set(HOSTNAME, "smtp-mail.outlook.com")
    .set(SMTP_PORT, "587")
    .set(FROM, "yaooqinn@hotmail.com")
    .set(SSL_ON_CONNECT, "true")
    .set(USERNAME, "hzyaoqin")
    .set(PASSWORD, "***")
    .set(TO, "yaooqinn@hotmail.com")


  ignore("on application end") {
    val input = Seq(Seq(1), Seq(2), Seq(3), Seq(4))
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Durations.seconds(1))
    val listener = new EmailStreamingAlarmListener(sc.getConf)
    sc.addSparkListener(listener)
    val stream = new AlarmTestInputStream[Int](ssc, input, 2)
    stream.foreachRDD(_ => {})  // Dummy output stream
    ssc.start()
    sc.stop()
    assert(ssc.getState() === StreamingContextState.ACTIVE)
  }

  ignore("on application end with extra listener") {
    conf.set("spark.extraListeners", classOf[EmailStreamingAlarmListener].getName)
    val input = Seq(Seq(1), Seq(2), Seq(3), Seq(4))
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Durations.seconds(1))
    val stream = new AlarmTestInputStream[Int](ssc, input, 2)
    stream.foreachRDD(_ => {})  // Dummy output stream
    ssc.start()
    sc.stop()
    assert(ssc.getState() === StreamingContextState.ACTIVE)
  }

  ignore("on batch completed") {
    conf.set("spark.extraListeners", classOf[EmailStreamingAlarmListener].getName)
    val input = Seq(Seq(1), Seq(2), Seq(3), Seq(4))
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(1))
    val stream = new AlarmTestInputStream[Int](ssc, input, 2)
    stream.foreachRDD(_ => {})  // Dummy output stream
    ssc.start()

    Thread.sleep(2000)
    ssc.stop(stopSparkContext = true, stopGracefully = true)
  }

}
