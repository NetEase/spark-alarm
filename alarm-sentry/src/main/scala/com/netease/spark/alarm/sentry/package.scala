package com.netease.spark.alarm

package object sentry {
  private val PREFIX = "spark.alarm.sentry."
  val SENTRY_URL: String = PREFIX + "url"
  val SENTRY_API_TYPE: String = PREFIX + "api.type"
  val SENTRY_TO_LIST: String = PREFIX + "to"
  val SENTRY_APP_SECRET: String = PREFIX + "app.secret"
  val SENTRY_APP_NAME: String = PREFIX + "app.name"
}
