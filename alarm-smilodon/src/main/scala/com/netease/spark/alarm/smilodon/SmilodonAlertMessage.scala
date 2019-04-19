package com.netease.spark.alarm.smilodon

import java.util

import com.netease.spark.alarm.smilodon.SmilodonAlertLevel.SmilodonAlertLevel
import com.netease.spark.alarm.smilodon.channel.{ChannelType, SmilodonChannel}
import com.netease.spark.alarm.smilodon.event.SmilodonEvent
import org.apache.spark.SparkConf

import scala.collection.JavaConverters._

/**
 *
 * @param level_id 不填时默认值为1
 * @param event
 * @param default_title 默认报警信息标题，如发送邮件的主题；不为空时，若channel无title字段，将使用本字段值，若多channel的title相同，建议设置此字段;
 *                      可为空，使用系统默认title
 * @param default_content 默认报警信息；不为空时，若channel无content字段，将使用本字段值，若多channel的content相同，建议设置此字段；可为空，使用
 *                        系统默认content
 * @param channels 报警通道列表，可定制该通道的报警标题和信息
 * @param users 接收人email列表
 * @param groups  报警组id 列表
 */
case class SmilodonAlertMessage private(
    level_id: Int,
    event: SmilodonEvent,
    default_title: String,
    default_content: String,
    channels: java.util.List[SmilodonChannel],
    users: java.util.List[String],
    groups: java.util.List[String])

object SmilodonAlertMessage {
  def apply(
      level: SmilodonAlertLevel,
      event: SmilodonEvent,
      default_title: String,
      default_content: String,
      channels: util.List[SmilodonChannel],
      users: util.List[String],
      groups: util.List[String]): SmilodonAlertMessage = {
    val id = SmilodonAlertLevel.getIdByLevel(level)
    new SmilodonAlertMessage(id, event, default_title, default_content, channels, users, groups)
  }

  def apply(
      level: SmilodonAlertLevel,
      event: SmilodonEvent,
      default_title: String,
      default_content: String,
      channels: util.List[SmilodonChannel],
      users: util.List[String]): SmilodonAlertMessage = {
    val id = SmilodonAlertLevel.getIdByLevel(level)
    new SmilodonAlertMessage(id, event, default_title, default_content, channels, users, Seq.empty[String].asJava)
  }

  def apply(
      event: SmilodonEvent,
      default_title: String,
      default_content: String,
      channels: util.List[SmilodonChannel],
      users: util.List[String]): SmilodonAlertMessage = {
    new SmilodonAlertMessage(1, event, default_title, default_content, channels, users, Seq.empty[String].asJava)
  }

  def apply(
      level: SmilodonAlertLevel,
      event: SmilodonEvent,
      default_title: String,
      default_content: String,
      conf: SparkConf): SmilodonAlertMessage = {
    val id = SmilodonAlertLevel.getIdByLevel(level)
    val channels = conf.get(SMILODON_ALERT_CHANNELS, "POPO,STONE")
      .split(",").map(_.trim)
      .map(ChannelType.getIdByName)
      .map(SmilodonChannel(_, "", "")).toList.asJava
    val users = conf.get(SMILODON_ALERT_USERS, "").split(",").map(_.trim).toList.asJava
    val groups = conf.getOption(SMILODON_ALERT_GROUPS) match {
      case Some(li) if li.trim.nonEmpty => li.split(",").map(_.trim).toList.asJava
      case _ => Seq.empty[String].asJava
    }
    new SmilodonAlertMessage(id, event, default_title, default_content, channels, users, groups)
  }
}
