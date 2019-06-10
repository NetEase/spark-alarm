package com.netease.spark.alarm.smilodon

object SmilodonAlertLevel extends Enumeration {

  type SmilodonAlertLevel = Value

  val GENERAL: SmilodonAlertLevel = Value(1, "general")
  val NORMAL: SmilodonAlertLevel = Value(2, "normal")
  val WARN: SmilodonAlertLevel = Value(3, "warn")
  val ERROR: SmilodonAlertLevel = Value(4, "error")
  val FATAL: SmilodonAlertLevel = Value(5, "fatal")

  def getIdByLevel(level: SmilodonAlertLevel): Int = {
    level match {
      case NORMAL => 2
      case WARN => 3
      case ERROR => 4
      case FATAL => 5
      case _ => 1
    }
  }
}
