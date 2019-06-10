package com.netease.spark.alarm

object Components extends Enumeration {
  type Components = Value
  val CORE, SQL, STREAMING, MLLIB, GRAPHX = Value
}
