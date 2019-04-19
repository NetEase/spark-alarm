package com.netease.spark.alarm.alarmer.streaming

import java.text.SimpleDateFormat
import java.util.Date

import com.netease.spark.alarm.{Alarmist, AlertMessage}
import com.netease.spark.alarm.AlarmConstants._
import org.apache.commons.logging.LogFactory
import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd, SparkListenerApplicationStart, SparkListenerEvent}
import org.apache.spark.streaming.scheduler.{StreamingListener, StreamingListenerBatchCompleted}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext

import scala.util.{Success, Try}

trait StreamingAlarmListener extends SparkListener with StreamingListener {
  private val LOG = LogFactory.getFactory.getInstance(classOf[StreamingAlarmListener])

  protected def alarmist: Alarmist

  protected def conf: SparkConf

  protected var applicationId: String = conf.getAppId
  protected var appName: String = conf.get("spark.app.name")

  protected val batchNoticeEnable: Boolean = conf.getBoolean(BATCH_NOTICE_ENABLE, defaultValue = true)
  protected val batchDelayRatio: Double = conf.getDouble(BATCH_DELAY_RATIO, 1)
  protected val batchProcessThreshold: Long = conf.getTimeAsMs(BATCH_PROCESS_TIME, "1s")


  override def onApplicationStart(applicationStart: SparkListenerApplicationStart): Unit = {
    StreamingContext.getActive().foreach { ssc =>
      val context = ssc.sparkContext
      applicationId = context.applicationId
      appName = context.appName
    }
  }
  /**
   * 用于捕获SparkContext变量stop, 但流式作业未正常结束
   * 场景举例:
   *    ApplicationMaster failover失败，当Driver端的Application Monitor检测到AM跪了，
   *    会触发调用SparkContext.stop
   */
  override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {
    if (LOG.isDebugEnabled) LOG.debug(s"onApplicationEnd called: $applicationEnd")
    val maybeContext = StreamingContext.getActive()
    maybeContext.foreach { ssc =>
      val context = ssc.sparkContext
      val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val startTime = dateFormat.format(new Date(context.startTime))
      val endTime = dateFormat.format(new Date(applicationEnd.time))
      val content = appFailContent(startTime, endTime)
      alarmist.alarm(AlertMessage(STREAMING_APPLICATION_ERROR, content))
      // ssc.stop(stopSparkContext = false, stopGracefully = false)
    }

    if (conf.getBoolean(FORCE_EXIT, defaultValue = false)) {
      System.exit(-1)
    }
  }

  /**
   * In [[SparkListener]]s, wrapped streaming events first come here
   */
  override def onOtherEvent(event: SparkListenerEvent): Unit = {
    if (LOG.isDebugEnabled) LOG.debug(s"onApplicationEnd called: $event")

    Try { event.getClass.getDeclaredField("streamingListenerEvent") } match {
      case Success(field) =>
        field.setAccessible(true)
        field.get(event) match {
          case bc: StreamingListenerBatchCompleted => onBatchCompleted(bc)
          case _ =>
        }
      case _ =>
    }
  }

  override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted): Unit = {
    if (LOG.isDebugEnabled) LOG.debug(s"onBatchCompleted called: $batchCompleted")

    val info = batchCompleted.batchInfo

    if (batchNoticeEnable) {
      val batchFailureReasons = info.outputOperationInfos.values.map(_.failureReason).filter(_.nonEmpty).mkString("\n")
      if (batchFailureReasons.nonEmpty) {
        val content = batchErrorContent(batchFailureReasons)
        alarmist.alarm(AlertMessage(STREAMING_BATCH_ERROR, content))
      }
    }

    val schedulingDelay = info.schedulingDelay.getOrElse(0L)
    val processingDelay = info.processingDelay.getOrElse(Long.MaxValue)
    if (processingDelay > batchProcessThreshold && schedulingDelay / processingDelay > batchDelayRatio) {
      val content = batchDelayContent(schedulingDelay, processingDelay)
      alarmist.alarm(AlertMessage(STREAMING_BATCH_ERROR, content))
    }
  }

  protected def batchDelayContent(schedulingDelay: Long, processingDelay: Long): String = {
    s"""
       |$STREAMING_BATCH_ERROR
       |
           |作业名: $appName
       |作业ID: $applicationId
       |诊断信息:
       |  批次调度延时过高(Scheduling Delay: $schedulingDelay ms / Processing Time: $processingDelay ms > $batchDelayRatio )
       |  单独关闭该类型警报可设置 $BATCH_DELAY_RATIO 的值
         """.stripMargin
  }

  protected def batchErrorContent(batchFailureReasons: String): String = {
    s"""
       |$STREAMING_BATCH_ERROR
       |
             |作业名: $appName
       |作业ID: $applicationId
       |诊断信息:
       |  $batchFailureReasons
       |  单独关闭该类型警报可设置$BATCH_NOTICE_ENABLE=false
           """.stripMargin
  }

  protected def appFailContent(startTime: String, endTime: String): String = {
    s"""
       |$STREAMING_APPLICATION_ERROR
       |
       |作业名: $appName
       |作业ID: $applicationId
       |启动时间: $startTime
       |停止时间: $endTime
       |
       |诊断信息:
       |  [SparkContext]终止与[StreamingContext]之前，尝试终止StreamingContext
       |  如进程无法正常退出，可尝试设置$FORCE_EXIT，强制退出（不推荐）
         """.stripMargin
  }

}
