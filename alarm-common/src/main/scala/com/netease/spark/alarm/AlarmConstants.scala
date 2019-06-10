package com.netease.spark.alarm

object AlarmConstants {

  import Components._
  import AlertType._
  val SERVICE: String = "Spark"

  val STREAMING_APPLICATION_ERROR: String =
    "服务[" + SERVICE + "]-组件["+ STREAMING + "]-级别[" + Application + "] 异常告警"

  val STREAMING_BATCH_ERROR: String =
    "服务[" + SERVICE + "]-组件["+ STREAMING + "]-级别[" + Batch + "] 异常告警"

  val STREAMING_JOB_ERROR: String =
    "服务[" + SERVICE + "]-组件["+ STREAMING + "]-级别[" + Job + "] 异常告警"
}


