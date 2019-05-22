package com.netease.spark.alarm.alarmer.streaming

import org.scalatest.FunSuite

import scala.reflect.runtime.{universe => ru}
import scala.util.control.NonFatal
import scala.util.{Failure, Try}

class StreamingAlarmListenerTest extends FunSuite {
  test("test get limit failure reason") {
    val className = "com.netease.spark.alarm.alarmer.streaming.StreamingAlarmListener"
    val fieldName = "failureReasonLimit"
    setStaticFieldValue(className, fieldName, 10)

    // length = 0
    val reason0 = ""
    // length < limit
    val reason1 = "Failed"
    // length > limit
    val reason2 = "Task Failed with Exception"
    // length > limit with multi lines
    val reason3 = "Task Failed with Exception\n\tjava.io.IOException"

    assert(StreamingAlarmListener.getLimitFailureReason(reason0) === reason0)
    assert(StreamingAlarmListener.getLimitFailureReason(reason1) === reason1)
    assert(StreamingAlarmListener.getLimitFailureReason(reason2) === reason2)
    assert(StreamingAlarmListener.getLimitFailureReason(reason3) === reason3.split("\n")(0))
  }

  def setStaticFieldValue(className: String, fieldName: String, value: Any): Unit = {
    Try {
      val mirror = ru.runtimeMirror(getClass.getClassLoader)
      val moduleSymbol = mirror.staticModule(className)
      val moduleMirror = mirror.reflectModule(moduleSymbol)
      val instanceMirror = mirror.reflect(moduleMirror.instance)
      val limitField = moduleSymbol.typeSignature.decl(ru.TermName(fieldName))
      val limit = instanceMirror.reflectField(limitField.asTerm)
      limit.set(value)
    } match {
      case Failure(e) => e.printStackTrace()
      case _ =>
    }
  }
}
