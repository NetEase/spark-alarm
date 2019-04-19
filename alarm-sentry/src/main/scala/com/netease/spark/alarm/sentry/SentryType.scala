package com.netease.spark.alarm.sentry

object SentryType extends Enumeration {

  type SentryType = Value

  val /** 泡泡 */ POPO, /** 易信 */YiXin, /** 短信 */ SMS, /** 语音 */ Voice, Stone, /** 邮件 */ Email = Value
}
