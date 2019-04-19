package com.netease.spark.alarm

case class AlertResp(status: Boolean, ret: String)

object AlertResp {
  def success(ret: String) = apply(status = true, ret)
  def failure(ret: String) = apply(status = false, ret)
}

