package com.netease.spark.alarm.alarmer.streaming
import com.netease.spark.alarm.Alarmist
import com.netease.spark.alarm.email.EmailAlarmist
import org.apache.spark.SparkConf

class EmailStreamingAlarmListener(override val conf: SparkConf) extends StreamingAlarmListener {
  override val alarmist: Alarmist = new EmailAlarmist(conf)
}
