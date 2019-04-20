package com.netease.spark.alarm.smilodon.channel

object ChannelType extends Enumeration {

  type ChannelType = Value

  val UNSUPPORTED = Value(1, "EMAIL, SMS, VOICE, YIXIN, etc.")
  val POPO = Value(4, "POPO")
  val STONE = Value(5, "Stone")

  def getIdByType(typ: ChannelType): Int = typ match {
    case POPO => 4
    case STONE => 5
    case _ => 1
  }

  def getIdByName(name: String): Int = name.toUpperCase() match {
    case "POPO" => 4
    case "STONE" => 5
    case _ => 1
  }
}
