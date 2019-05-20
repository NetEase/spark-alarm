package com.netease.spark.alarm.alarmer.streaming

import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

import com.netease.spark.alarm.{Alarmist, AlertMessage}
import com.netease.spark.alarm.AlarmConstants._
import org.apache.commons.logging.LogFactory
import org.apache.spark.scheduler.{SparkListener, SparkListenerApplicationEnd, SparkListenerApplicationStart, SparkListenerEvent}
import org.apache.spark.streaming.scheduler.{StreamingListener, StreamingListenerBatchCompleted, StreamingListenerBatchStarted, StreamingListenerOutputOperationCompleted}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{StreamingContext, Time}

import scala.util.{Success, Try}
import scala.collection.JavaConverters._

trait StreamingAlarmListener extends SparkListener with StreamingListener {

  import StreamingAlarmListener._

  private val LOG = LogFactory.getFactory.getInstance(classOf[StreamingAlarmListener])

  protected def alarmist: Alarmist

  protected def conf: SparkConf

  protected var applicationId: String = conf.getAppId
  protected var appName: String = conf.get("spark.app.name")

  protected val batchNoticeEnable: Boolean = conf.getBoolean(BATCH_NOTICE_ENABLE, defaultValue = true)
  protected val batchDelayRatio: Double = conf.getDouble(BATCH_DELAY_RATIO, 1)
  protected val batchProcessThreshold: Long = conf.getTimeAsMs(BATCH_PROCESS_TIME, "1s")
  protected lazy val batchSet: java.util.Map[Time, Long] = new ConcurrentHashMap[Time, Long]

  protected val jobNoticeEnable: Boolean = conf.getBoolean(JOB_NOTICE_ENABLE, defaultValue = false)

  override def onApplicationStart(applicationStart: SparkListenerApplicationStart): Unit = {
    if (LOG.isDebugEnabled) LOG.debug(s"onApplicationStart called: $applicationStart")

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
    if (LOG.isDebugEnabled) LOG.debug(s"onOtherEvent called: $event")

    Try { event.getClass.getDeclaredField("streamingListenerEvent") } match {
      case Success(field) =>
        field.setAccessible(true)
        field.get(event) match {
          case as: SparkListenerApplicationStart => onApplicationStart(as)
          case ae: SparkListenerApplicationEnd => onApplicationEnd(ae)
          case bs: StreamingListenerBatchStarted => onBatchStarted(bs)
          case bc: StreamingListenerBatchCompleted => onBatchCompleted(bc)
          case oc: StreamingListenerOutputOperationCompleted => onOutputOperationCompleted(oc)
          case _ =>
        }
      case _ =>
    }
  }

  override def onBatchStarted(batchStarted: StreamingListenerBatchStarted): Unit = {
    if (LOG.isDebugEnabled) LOG.debug(s"onBatchStarted called: $batchStarted")

    if (batchNoticeEnable) {
      val currentTime = System.currentTimeMillis()
      batchSet.entrySet().asScala.foreach { kv =>
        val start = kv.getValue
        if (currentTime - start > batchProcessThreshold) {
          alarmist.alarm(AlertMessage(STREAMING_BATCH_ERROR,
            batchTimeoutContent(currentTime - start)))
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

  override def onOutputOperationCompleted(outputOperationCompleted: StreamingListenerOutputOperationCompleted): Unit = {
    if (LOG.isDebugEnabled) LOG.debug(s"onOutputOperationCompleted called: $outputOperationCompleted")

    if (jobNoticeEnable) {
      val jobFailureReason = outputOperationCompleted.outputOperationInfo.failureReason
      jobFailureReason.foreach { reason =>
        alarmist.alarm(AlertMessage(STREAMING_JOB_ERROR, jobErrorContent(reason)))
      }
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
       |  (长度限制: $failureReasonLimit)
       |  ${getLimitFailureReason(batchFailureReasons)}
       |  单独关闭该类型警报可设置$BATCH_NOTICE_ENABLE=false
     """.stripMargin
  }

  protected def batchTimeoutContent(processDuration: Long): String = {
    s"""
       |$STREAMING_BATCH_ERROR
       |
       |作业名: $appName
       |作业ID: $applicationId
       |诊断信息:
       |  批次调度运行时长已超阈值(Process Duration: $processDuration ms > Processing Threshold: $batchProcessThreshold ms)
       |  单独关闭该类型警报可设置 $BATCH_DELAY_RATIO 的值
     """.stripMargin
  }

  protected def jobErrorContent(jobErrorReason: String): String = {
    s"""
       |$STREAMING_JOB_ERROR
       |
       |作业名: $appName
       |作业ID: $applicationId
       |诊断信息:
       |  (长度限制: $failureReasonLimit)
       |  ${getLimitFailureReason(jobErrorReason)}
       |  单独关闭该类型警报可设置$JOB_NOTICE_ENABLE=false
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
object StreamingAlarmListener {
  // The length limit for failureReason to sent
  val failureReasonLimit: Int = 200

  def getLimitFailureReason(failureReason: String): String = {
    val nextLineIndex = failureReason.indexOf("\n", failureReasonLimit)
    if (nextLineIndex == -1) {
      failureReason
    } else {
      failureReason.substring(0, nextLineIndex)
    }
  }
}
