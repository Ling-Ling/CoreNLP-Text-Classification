package org.template.recommendation

import io.prediction.controller.PDataSource
import io.prediction.controller.EmptyEvaluationInfo
import io.prediction.controller.EmptyActualResult
import io.prediction.controller.Params
import io.prediction.data.storage.Event
import io.prediction.data.storage.Storage

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.util.ErasureUtils;

import grizzled.slf4j.Logger

case class DataSourceParams(val appId: Int) extends Params

class DataSource(val dsp: DataSourceParams)
  extends PDataSource[TrainingData,
      EmptyEvaluationInfo, Query, EmptyActualResult] {

  @transient lazy val logger = Logger[this.type]

  override
  def readTraining(sc: SparkContext): TrainingData = {
    val eventsDb = Storage.getPEvents()
    val eventsRDD: RDD[Event] = eventsDb.find(
      appId = dsp.appId,
      entityType = Some("question"),
      eventNames = Some(List("$set")))(sc)
    
    val textClassRDD: RDD[TextClass] = eventsRDD.map { event =>
      val textClass = try {
        val textClassType: String = event.event match {
        case "$set" => event.properties.get[String]("text")
        case _ => throw new Exception(s"Unexpected event ${event} is read.")
        }
        TextClass(event.entityId,
              textClassType)
      } catch {
        case e: Exception => {
          logger.error(s"Cannot convert ${event} to TextClass. Exception: ${e}.")
          throw e
        }
      }
      textClass
    }
    new TrainingData(textClassRDD)
  }
}

case class TextClass(
  val text_type: String,
  val text: String
)

class TrainingData(
  val texts: RDD[TextClass]
) extends Serializable {
  override def toString = {
    s"queries: [${texts.count()}] (${texts.take(1).toList}...)"
  }
}