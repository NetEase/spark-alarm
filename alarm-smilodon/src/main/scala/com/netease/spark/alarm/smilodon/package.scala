package com.netease.spark.alarm

package object smilodon {

  private val PREFIX = "spark.alarm.smilodon."
  val SMILODON_URL: String = PREFIX + "url"
  val SMILODON_ALERT_CHANNELS: String = PREFIX + "channels"
  val SMILODON_ALERT_USERS: String = PREFIX + "users"
  val SMILODON_ALERT_GROUPS: String = PREFIX + "groups"
}
