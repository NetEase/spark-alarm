package com.netease.spark.alarm

trait Alarmist {

  /**
   * Send the alert message to possible external SMS, EMAIL, Phone system.
   *
   * @param msg the alert message to send
   * @return a [[AlertResp]] with status and an optional message
   */
  def alarm(msg: AlertMessage): AlertResp
}
