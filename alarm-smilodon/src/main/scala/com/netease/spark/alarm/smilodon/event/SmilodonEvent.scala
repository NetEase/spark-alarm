package com.netease.spark.alarm.smilodon.event

import com.netease.spark.alarm.{AlarmConstants, AlertType, Components}

/**
 *
 * @param service 报警是由哪个服务产生的
 * @param component 报警是由服务的哪个组件产生的
 * @param `type` 具体的事件类型，比如任务失败，服务挂掉等
 */
case class SmilodonEvent(service: String, component: String, `type`: String)

object SmilodonEvent {
  def streamingAppEvent(): SmilodonEvent =
    SmilodonEvent(AlarmConstants.SERVICE, Components.STREAMING.toString, AlertType.Application.toString)

  def streamingBatchEvent(): SmilodonEvent =
    SmilodonEvent(AlarmConstants.SERVICE, Components.STREAMING.toString, AlertType.Batch.toString)

}
