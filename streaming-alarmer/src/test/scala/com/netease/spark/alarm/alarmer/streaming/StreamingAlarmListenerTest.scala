package com.netease.spark.alarm.alarmer.streaming

import org.scalatest.FunSuite

import scala.reflect.runtime.{universe => ru}

class StreamingAlarmListenerTest extends FunSuite {
  import StreamingAlarmListener._

  test("test get limit failure reason") {
    val className = "com.netease.spark.alarm.alarmer.streaming.StreamingAlarmListener"
    val limitFieldName = "failureReasonLimit"
    val factorFieldName = "maxLengthFactor"
    val lengthLimit = 10
    setStaticFieldValue(className, limitFieldName, lengthLimit)
    // length = 0
    val reason0 = ""
    // length < limit

    val reason1 = "Failed"
    // length > limit
    val reason2 = "Task Failed with Exception"
    // length > limit with multi lines not than lengthLimit * maxLengthFactor
    val reason3 = "Task Failed with Exception\n\tline2"

    setStaticFieldValue(className, factorFieldName, 10.0)
    assert(getLimitedFailureReason(reason0) === reason0)
    assert(getLimitedFailureReason(reason1) === reason1)
    assert(getLimitedFailureReason(reason2) === reason2)
    assert(getLimitedFailureReason(reason3) === reason3.split("\n")(0))

    setStaticFieldValue(className, factorFieldName, 1.1)
    assert(getLimitedFailureReason(reason2) === reason2.substring(0, (maxLengthFactor * failureReasonLimit).toInt))
  }

  def setStaticFieldValue(className: String, fieldName: String, value: Any): Unit = {
    try {
      val mirror = ru.runtimeMirror(getClass.getClassLoader)
      val moduleSymbol = mirror.staticModule(className)
      val moduleMirror = mirror.reflectModule(moduleSymbol)
      val instanceMirror = mirror.reflect(moduleMirror.instance)
      val limitField = moduleSymbol.typeSignature.decl(ru.TermName(fieldName))
      val limit = instanceMirror.reflectField(limitField.asTerm)
      limit.set(value)
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
}
