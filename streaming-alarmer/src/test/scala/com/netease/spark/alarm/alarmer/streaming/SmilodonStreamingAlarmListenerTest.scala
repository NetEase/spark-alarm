package com.netease.spark.alarm.alarmer.streaming

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming._
import org.scalatest.FunSuite

class SmilodonStreamingAlarmListenerTest extends FunSuite {

  val conf: SparkConf = new SparkConf(loadDefaults = true)
    .setMaster("local[2]")
    .setAppName("smilodon")
    .set("spark.alarm.smilodon.users", "yaooqin@hotmail.com")
    .set("spark.alarm.smilodon.channels", "POPO,STONE")
//    .set("spark.alarm.alarm.force.exit.on.stop", "true")

  ignore("on application end") {
    val input = Seq(Seq(1), Seq(2), Seq(3), Seq(4))
    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Durations.seconds(1))
    val listener = new SmilodonStreamingAlarmListener(sc.getConf)
    sc.addSparkListener(listener)
    val stream = new AlarmTestInputStream[Int](ssc, input, 2)
    stream.foreachRDD(_ => {})  // Dummy output stream
    ssc.start()
    sc.stop()
    assert(ssc.getState() === StreamingContextState.ACTIVE)
  }


  ignore("on application end with extra listener") {
    conf.set("spark.extraListeners", classOf[SmilodonStreamingAlarmListener].getName)
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
    conf.set("spark.extraListeners", classOf[SmilodonStreamingAlarmListener].getName)
      .set("spark.alarm.streaming.batch.process.time.threshold", "10ms")
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
