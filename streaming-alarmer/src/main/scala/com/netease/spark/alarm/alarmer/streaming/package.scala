package com.netease.spark.alarm.alarmer

package object streaming {
  private val PREFIX = "spark.alarm.streaming."

  val FORCE_EXIT: String = PREFIX + "application.force.exit"
  val BATCH_NOTICE_ENABLE: String = PREFIX + "batch.error.enable"
  val BATCH_DELAY_RATIO: String = PREFIX + "batch.delay.ratio"
  val BATCH_PROCESS_TIME: String = PREFIX + "batch.process.time.threshold"

}
