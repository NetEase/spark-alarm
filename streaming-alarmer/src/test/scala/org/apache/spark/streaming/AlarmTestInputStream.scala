package org.apache.spark.streaming

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.scheduler.StreamInputInfo

import scala.reflect.ClassTag

/**
 * This is a input stream just for the testsuites. This is equivalent to a checkpointable,
 * replayable, reliable message queue like Kafka. It requires a sequence as input, and
 * returns the i_th element at the i_th batch under manual clock.
 */
class AlarmTestInputStream[T: ClassTag](_ssc: StreamingContext, input: Seq[Seq[T]], numPartitions: Int)
  extends InputDStream[T](_ssc) {

  def start() {}

  def stop() {}

  def compute(validTime: Time): Option[RDD[T]] = {
    logInfo("Computing RDD for time " + validTime)
    val index = ((validTime - zeroTime) / slideDuration - 1).toInt
    val selectedInput = if (index < input.size) input(index) else Seq[T]()

    // lets us test cases where RDDs are not created
    if (selectedInput == null) {
      return None
    }

    // Report the input data's information to InputInfoTracker for testing
    val inputInfo = StreamInputInfo(id, selectedInput.length.toLong)
    ssc.scheduler.inputInfoTracker.reportInfo(validTime, inputInfo)

    val rdd = ssc.sc.makeRDD(selectedInput, numPartitions)
    logInfo("Created RDD " + rdd.id + " with " + selectedInput)
    Some(rdd)
  }
}
