package com.netease.spark.alarm.alarmer.streaming
import com.netease.spark.alarm.Alarmist
import com.netease.spark.alarm.sentry.SentryAlarmist
import org.apache.spark.SparkConf

class SentryStreamingAlarmListener(override val conf: SparkConf) extends StreamingAlarmListener {
  override val alarmist: Alarmist = new SentryAlarmist(conf)

}
