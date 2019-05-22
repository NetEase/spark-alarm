package com.netease.spark.alarm.alarmer.streaming
import java.text.SimpleDateFormat
import java.util.Date

import com.netease.spark.alarm.smilodon.{SmilodonAlarmist, SmilodonAlertLevel, SmilodonAlertMessage}
import com.netease.spark.alarm.smilodon.event.SmilodonEvent
import com.netease.spark.alarm.AlarmConstants._
import org.apache.commons.logging.LogFactory
import org.apache.spark.SparkConf
import org.apache.spark.scheduler.SparkListenerApplicationEnd
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.scheduler.{StreamingListenerBatchCompleted, StreamingListenerBatchStarted, StreamingListenerOutputOperationCompleted}

import scala.collection.JavaConverters._

class SmilodonStreamingAlarmListener(override val conf: SparkConf) extends StreamingAlarmListener {
  private val LOG = LogFactory.getFactory.getInstance(classOf[SmilodonStreamingAlarmListener])
  override val alarmist: SmilodonAlarmist = new SmilodonAlarmist(conf)

  override def onApplicationEnd(applicationEnd: SparkListenerApplicationEnd): Unit = {
    if (LOG.isDebugEnabled) LOG.debug(s"onApplicationEnd called: $applicationEnd")

    val maybeContext = StreamingContext.getActive()
    maybeContext.foreach { ssc =>
      val context = ssc.sparkContext
      val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val startTime = dateFormat.format(new Date(context.startTime))
      val endTime = dateFormat.format(new Date(applicationEnd.time))
      val content = appFailContent(startTime, endTime)
      LOG.error(content)
      val event = SmilodonEvent.streamingAppEvent()
      val alertMessage = SmilodonAlertMessage(SmilodonAlertLevel.FATAL, event, STREAMING_APPLICATION_ERROR, content, conf)
      alarmist.alarm(alertMessage)
      // Spark 内部机制会保障Streaming 无法在ListenerBus中被调用
      // ssc.stop(stopSparkContext = false, stopGracefully = false)
    }

    if (conf.getBoolean(FORCE_EXIT, defaultValue = false)) {
      System.exit(-1)
    }
  }

  override def onBatchStarted(batchStarted: StreamingListenerBatchStarted): Unit = {
    if (LOG.isDebugEnabled) LOG.debug(s"onBatchStarted called: $batchStarted")

    if (batchNoticeEnable) {
      val currentTime = System.currentTimeMillis()
      batchSet.entrySet().asScala.foreach { kv =>
        val start = kv.getValue
        if (currentTime - start > batchProcessThreshold) {
          val content = batchTimeoutContent(currentTime - start)
          val event = SmilodonEvent.streamingBatchEvent()
          val alertMessage = SmilodonAlertMessage(SmilodonAlertLevel.FATAL, event, STREAMING_BATCH_ERROR, content, conf)
          alarmist.alarm(alertMessage)
        }
      }
      val batchInfo = batchStarted.batchInfo
      batchSet.put(batchInfo.batchTime, batchInfo.processingStartTime.getOrElse(System.currentTimeMillis()))
    }
  }

  override def onBatchCompleted(batchCompleted: StreamingListenerBatchCompleted): Unit = {
    if (LOG.isDebugEnabled) LOG.debug(s"onBatchCompleted called: $batchCompleted")

    val info = batchCompleted.batchInfo

    if (batchNoticeEnable) {
      batchSet.remove(info.batchTime)
      val batchFailureReasons = info.outputOperationInfos.values.map(_.failureReason).filter(_.nonEmpty).mkString("\n")
      if (batchFailureReasons.nonEmpty) {
        val content = batchErrorContent(batchFailureReasons)
        val event = SmilodonEvent.streamingBatchEvent()
        val alertMessage = SmilodonAlertMessage(SmilodonAlertLevel.FATAL, event, STREAMING_BATCH_ERROR, content, conf)
        alarmist.alarm(alertMessage)
      }
    }

    val schedulingDelay = info.schedulingDelay.getOrElse(0L)
    val processingDelay = info.processingDelay.getOrElse(Long.MaxValue)
    if (processingDelay > batchProcessThreshold && schedulingDelay / processingDelay > batchDelayRatio) {
      val content = batchDelayContent(schedulingDelay, processingDelay)
      val event = SmilodonEvent.streamingBatchEvent()
      val alertMessage = SmilodonAlertMessage(SmilodonAlertLevel.FATAL, event, STREAMING_BATCH_ERROR, content, conf)
      alarmist.alarm(alertMessage)
    }
  }

  override def onOutputOperationCompleted(outputOperationCompleted: StreamingListenerOutputOperationCompleted): Unit = {
    if (LOG.isDebugEnabled) LOG.debug(s"onOutputOperationCompleted called: $outputOperationCompleted")

    if (jobNoticeEnable) {
      val jobFailureReason = outputOperationCompleted.outputOperationInfo.failureReason
      jobFailureReason.foreach { reason =>
        val content = jobErrorContent(reason)
        val event = SmilodonEvent.streamingJobEvent()
        val alertMessage = SmilodonAlertMessage(SmilodonAlertLevel.FATAL, event, STREAMING_JOB_ERROR, content, conf)
        alarmist.alarm(alertMessage)
      }
    }
  }
}
