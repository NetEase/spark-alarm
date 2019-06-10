package com.netease.spark.alarm.sentry

import java.util

import com.google.gson.Gson
import com.netease.spark.alarm.{Alarmist, AlertMessage, AlertResp}
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.Charsets
import org.apache.commons.logging.LogFactory
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.apache.spark.SparkConf

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

class SentryAlarmist(conf: SparkConf) extends Alarmist {
  private val LOG = LogFactory.getFactory.getInstance(classOf[SentryAlarmist])

  private val client = HttpClients.createDefault()
  private val url = conf.get(SENTRY_URL, "")
  private val typ = conf.get(SENTRY_API_TYPE, SentryType.Stone.toString)
  private val api = url + typ
  private val to = conf.get(SENTRY_TO_LIST, "")
  private val secret = conf.get(SENTRY_APP_SECRET, "")
  private val appName = conf.get(SENTRY_APP_NAME, "")

  override def alarm(msg: AlertMessage): AlertResp = {

    val httpPost = new HttpPost(api)
    val now = System.currentTimeMillis()
    val sig = DigestUtils.md5Hex(secret + now)
    val params = List(new BasicNameValuePair("to", to),
      new BasicNameValuePair("title", msg.title),
      new BasicNameValuePair("content", msg.content),
      new BasicNameValuePair("isSync", "true"),
      new BasicNameValuePair("ac_appName", appName),
      new BasicNameValuePair("ac_timestamp", now.toString),
      new BasicNameValuePair("ac_signature", sig))
    httpPost.setEntity(new UrlEncodedFormEntity(params.asJava, Charsets.UTF_8))

    try {
      val response = client.execute(httpPost)
      val status = response.getStatusLine.getStatusCode
      val resp = EntityUtils.toString(response.getEntity)
      if (status == 200) {
        val map = new Gson().fromJson(resp, classOf[util.HashMap[String, Any]])
        SentryStatus.handleResponse(map)
      } else {
        LOG.error(s"Failed to send message to $to using API $api, status: $status" )
        AlertResp.failure(resp)
      }
    } catch {
      case NonFatal(e) =>
        LOG.error(s"Failed to send message to $to using API $api", e)
        val message = e.getMessage
        AlertResp.failure(message)
    }
  }
}
