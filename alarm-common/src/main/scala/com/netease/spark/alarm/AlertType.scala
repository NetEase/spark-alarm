package com.netease.spark.alarm

/**
 *
 */
object AlertType extends Enumeration {
  type AlertType = Value
  val Application, Batch, Job, Stage, Task = Value
}
