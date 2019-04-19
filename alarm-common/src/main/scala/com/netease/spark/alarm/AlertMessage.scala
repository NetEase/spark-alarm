package com.netease.spark.alarm

/**
 * Message used to be sent by [[Alarmist]]
 * @param title message title
 * @param content message body
 */
case class AlertMessage(title: String, content: String)
