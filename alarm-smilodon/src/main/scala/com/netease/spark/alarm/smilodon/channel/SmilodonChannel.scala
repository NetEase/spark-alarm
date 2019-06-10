package com.netease.spark.alarm.smilodon.channel

import com.netease.spark.alarm.smilodon.channel.ChannelType.ChannelType

case class SmilodonChannel private (channel_id: Int, title: String, content: String)

object SmilodonChannel {
  def apply(typ: ChannelType, title: String, content: String): SmilodonChannel = {
    val id = ChannelType.getIdByType(typ)
    new SmilodonChannel(id, title, content)
  }

  /**
   * Send alert message to Smilodon, then it will be alarm by POPO
   */
  def popo(title: String, content: String): SmilodonChannel = {
    SmilodonChannel(4, title, content)
  }

  /**
   * Send alert message to Smilodon, then it will be alarm by POPO
   */
  def popo(): SmilodonChannel = {
    SmilodonChannel(4, "", "")
  }

  /**
   * Send alert message to Smilodon, then it will be alarm by Stone
   */
  def stone(title: String, content: String): SmilodonChannel = {
    SmilodonChannel(5, title, content)
  }

  /**
   * Send alert message to Smilodon, then it will be alarm by Stone
   */
  def stone(): SmilodonChannel = {
    SmilodonChannel(5, "", "")
  }
}
