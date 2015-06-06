package org.template.classification

import io.prediction.controller.PDataSource
import io.prediction.controller.EmptyEvaluationInfo
import io.prediction.controller.Params
import io.prediction.data.storage.Event
import io.prediction.data.store.PEventStore

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import grizzled.slf4j.Logger

case class DataSourceParams(val appName: String) extends Params

class DataSource(val dsp: DataSourceParams)
  extends PDataSource[TrainingData,
      EmptyEvaluationInfo, Query, ActualResult] {

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
          gender = event.properties.getOpt[String]("gender"),
          dizzy = event.properties.getOpt[String]("dizziness"),
          convul = event.properties.getOpt[String]("convulsions"),
          heart = event.properties.getOpt[String]("heart_palpitation"),
          breath = event.properties.getOpt[String]("shortness_of_breath"),
          head = event.properties.getOpt[String]("headaches"),
          effect = event.properties.getOpt[String]("effect_decreased"),
          allergy = event.properties.getOpt[String]("allergies_worse"),
          bad = event.properties.getOpt[String]("bad_interaction"),
          nausea = event.properties.getOpt[String]("nausea"),
          insomnia = event.properties.getOpt[String]("insomnia")
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
  override
  def readEval(sc: SparkContext)
  : Seq[(TrainingData, EmptyEvaluationInfo, RDD[(Query, ActualResult)])] = {
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
          gender = event.properties.getOpt[String]("gender"),
          dizzy = event.properties.getOpt[String]("dizziness"),
          convul = event.properties.getOpt[String]("convulsions"),
          heart = event.properties.getOpt[String]("heart_palpitation"),
          breath = event.properties.getOpt[String]("shortness_of_breath"),
          head = event.properties.getOpt[String]("headaches"),
          effect = event.properties.getOpt[String]("effect_decreased"),
          allergy = event.properties.getOpt[String]("allergies_worse"),
          bad = event.properties.getOpt[String]("bad_interaction"),
          nausea = event.properties.getOpt[String]("nausea"),
          insomnia = event.properties.getOpt[String]("insomnia")
        ) 
      } catch {
        case e: Exception =>
          logger.error(s"Cannot convert ${event} to TextClass." +
            s" Exception: ${e}.")
          throw e
      }
    }.cache()
    
    (0 until 1).map { idx =>
      val random = idx
      (
        new TrainingData(labeledPoints),
        new EmptyEvaluationInfo(),
        labeledPoints.map {
          p => (new Query(p.text, p.gender, p.dizzy, p.convul, p.heart, p.breath, p.head, p.effect, p.allergy, p.bad, p.nausea, p.insomnia), 
            new ActualResult(p.text_type))
        }
      )
    }
  }
}

case class TextClass(
  val text_type: String,
  val text: String,
  val gender: Option[String],
  val dizzy: Option[String],
  val convul: Option[String],
  val heart: Option[String],
  val breath: Option[String],
  val head: Option[String],
  val effect: Option[String],
  val allergy: Option[String],
  val bad: Option[String],
  val nausea: Option[String],
  val insomnia: Option[String]
)

class TrainingData(
  val texts: RDD[TextClass]
) extends Serializable {
  override def toString = {
    s"queries: [${texts.count()}] (${texts.take(1).toList}...)"
  }
}
