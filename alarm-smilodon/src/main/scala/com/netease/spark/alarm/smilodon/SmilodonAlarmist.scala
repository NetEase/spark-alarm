package com.netease.spark.alarm.smilodon

import com.google.gson.Gson
import com.netease.spark.alarm.{Alarmist, AlertMessage, AlertResp}
import org.apache.commons.logging.LogFactory
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.spark.SparkConf

import scala.util.control.NonFatal

class SmilodonAlarmist(conf: SparkConf) extends Alarmist {
  private val LOG = LogFactory.getFactory.getInstance(classOf[SmilodonAlarmist])

  private val client = HttpClients.createDefault()
  private val url = conf.get(SMILODON_URL, "http://10.165.139.76:8765/v1/alerts")
  override def alarm(msg: AlertMessage): AlertResp = {
    val httpPost = new HttpPost(url)
    val entity = new StringEntity(msg.content, ContentType.APPLICATION_JSON)
//    httpPost.setHeader("Content-Type", "application/json")
    httpPost.setEntity(entity)
    try {
      val response = client.execute(httpPost)
      val code = response.getStatusLine.getStatusCode
      val resp = EntityUtils.toString(response.getEntity)

      if (code == 200) {
        LOG.info("Succeed to send alert to Smilondon")
        AlertResp.success(resp)
      } else {
        LOG.error(s"Failed to send alert to Smilondon, status: $code, msg: $resp")
        AlertResp.failure(resp)
      }
    } catch {
      case NonFatal(e) =>
        LOG.error("Failed to send alert to Smilondon " + msg.content , e)
        AlertResp.failure(e.getMessage)
    }
  }

  def alarm(msg: SmilodonAlertMessage): AlertResp = {
    val jsonMsg = new Gson().toJson(msg)
    alarm(AlertMessage("", jsonMsg))
  }
}
