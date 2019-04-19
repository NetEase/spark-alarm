package com.netease.spark.alarm.email

import com.netease.spark.alarm.{Alarmist, AlertMessage, AlertResp}
import org.apache.commons.logging.LogFactory
import org.apache.commons.mail.{DefaultAuthenticator, EmailConstants, SimpleEmail}
import org.apache.spark.SparkConf

class EmailAlarmist(conf: SparkConf) extends Alarmist {

  private val LOG = LogFactory.getFactory.getInstance(classOf[EmailAlarmist])
  private val hostName = conf.get(HOSTNAME, "")
  private val useSSL = conf.getBoolean(SSL_ON_CONNECT, defaultValue = false)
  private val port = conf.getOption(SMTP_PORT) match {
    case Some(v) => v.toInt
    case _ => if (useSSL) 465 else 25
  }
  private val user = conf.get(USERNAME, "")
  private val password = conf.get(PASSWORD, "")
  private val from = conf.get(FROM, "")
  private val toList = conf.get(TO, from).split(",")

  override def alarm(msg: AlertMessage): AlertResp = {
    try {
      val email = new SimpleEmail()
      email.setHostName(hostName)
      email.setSmtpPort(port)
      email.setCharset(EmailConstants.UTF_8)
      val authn = new DefaultAuthenticator(user, password)
      email.setAuthenticator(authn)
      email.setSSLOnConnect(useSSL)
      email.setFrom(from)
      email.setSubject(msg.title)
      email.setMsg(msg.content)
      email.addTo(toList: _*)
      val ret = email.send()
      AlertResp.success(ret)
    } catch {
      case e: Exception =>
        LOG.error(s"User $user failed to send email from $from to $toList", e)
        AlertResp.failure(e.getMessage)
    }
  }
}
