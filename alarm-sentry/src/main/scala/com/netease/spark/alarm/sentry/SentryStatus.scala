package com.netease.spark.alarm.sentry

import com.netease.spark.alarm.AlertResp
import org.apache.commons.logging.LogFactory

object SentryStatus extends Enumeration {
  private val LOG = LogFactory.getFactory.getInstance(this.getClass)

  type SentryStatus = Value

  val SUCCESS: SentryStatus = Value(200, "SUCCESS")
  val PARTIAL_SUCCESS: SentryStatus = Value(201, "PARTIAL_SUCCESS")
  val SYSTEM_ERROR: SentryStatus = Value(500, "SYSTEM_ERROR")
  val AUTHENTICATION_ERROR: SentryStatus = Value(501, "AUTHENTICATION_ERROR")
  val ILLEGAL_ARGUMENTS: SentryStatus = Value(502, "ILLEGAL_ARGUMENTS")
  val QUOTA_EXCEED: SentryStatus = Value(520, "QUOTA_EXCEED")

  def handleResponse(resp: java.util.HashMap[String, Any]): AlertResp = {
    val msg = resp.get("message") + ""
    resp.get("code").asInstanceOf[Double].toInt match {
      case 200 =>
        LOG.info("Succeed to send alert to Sentry")
        AlertResp.success(msg)
      case 201 =>
        LOG.warn("Succeed to send alert to Sentry, but some receiver may not receive the alert")
        AlertResp.success(msg)
      case 500 =>
        LOG.error(s"${SYSTEM_ERROR.toString}: failed  to send message to Sentry due to $msg")
        AlertResp.failure(msg)
      case 501 =>
        LOG.error(s"${AUTHENTICATION_ERROR.toString}: failed  to send message to Sentry due to $msg")
        AlertResp.failure(msg)
      case 502 =>
        LOG.error(s"${ILLEGAL_ARGUMENTS.toString}: failed  to send message to Sentry due to $msg")
        AlertResp.failure(msg)
      case 520 =>
        LOG.error(s"${QUOTA_EXCEED.toString}: failed  to send message to Sentry due to $msg")
        AlertResp.failure(msg)
      case _ =>
        LOG.error(s"UNKNOWN_ERROR: failed  to send message to Sentry due to $msg")
        AlertResp.failure(msg)
    }
  }
}
