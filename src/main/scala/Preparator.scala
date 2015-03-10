package org.template.recommendation

import io.prediction.controller.PPreparator

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

//TODO get rid of params
import io.prediction.controller.Params

case class CustomPreparatorParams(
  filepath: String
) extends Params

class PreparedData(
  val texts: RDD[TextClass]
)  extends Serializable

class Preparator (pp: CustomPreparatorParams)
  extends PPreparator[TrainingData, PreparedData] {
  
  def prepare(sc: SparkContext, trainingData: TrainingData): PreparedData = {
    new PreparedData(texts = trainingData.texts)
  }
}

