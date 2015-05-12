package org.template.classification

import io.prediction.controller.PDataSource
import io.prediction.controller.EmptyEvaluationInfo
import io.prediction.controller.EmptyActualResult
import io.prediction.controller.Params
import io.prediction.data.storage.Event
import io.prediction.data.storage.Storage
import io.prediction.data.store.PEventStore

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import grizzled.slf4j.Logger

case class DataSourceParams(val appName: String) extends Params

class DataSource(val dsp: DataSourceParams)
  extends PDataSource[TrainingData,
      EmptyEvaluationInfo, Query, EmptyActualResult] {

  @transient lazy val logger = Logger[this.type]

  override
  def readTraining(sc: SparkContext): TrainingData = {
  
    val eventsRDD: RDD[Event] = PEventStore.find(
      appName = dsp.appName,
      entityType = Some("question"),
      eventNames = Some(List("twitter"))
    )(sc).cache()

    val labeledPoints: RDD[TextClass] = eventsRDD
      .filter {event => event.event == "twitter"}
      .map { event =>

      try {
        TextClass(
          text_type = event.entityId,
          text = event.properties.get[String]("text"),
          gender = event.properties.get[String]("gender")
        ) 
      } catch {
        case e: Exception =>
          logger.error(s"Cannot convert ${event} to TextClass." +
            s" Exception: ${e}.")
          throw e
      }
    }
    
    new TrainingData(labeledPoints)
  }
}

case class TextClass(
  val text_type: String,
  val text: String,
  val gender: String
)

class TrainingData(
  val texts: RDD[TextClass]
) extends Serializable {
  override def toString = {
    s"queries: [${texts.count()}] (${texts.take(1).toList}...)"
  }
}
