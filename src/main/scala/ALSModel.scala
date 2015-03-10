package org.apache.spark.mllib.recommendation
// This must be the same package as Spark's MatrixFactorizationModel because
// MatrixFactorizationModel's constructor is private and we are using
// its constructor in order to save and load the model

import org.template.recommendation.ALSAlgorithmParams

import io.prediction.controller.IPersistentModel
import io.prediction.controller.IPersistentModelLoader
import io.prediction.data.storage.BiMap

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;

class ALSModel(
    val cl: Classifier[String, String])
  {
  def save(id: String, params: ALSAlgorithmParams,
    sc: SparkContext): Boolean = {
    sc.parallelize(Seq(cl)).saveAsObjectFile(s"/tmp/${id}/cl")
    true
  }

  override def toString = {
    s"empty"
  }
}

object ALSModel
  extends IPersistentModelLoader[ALSAlgorithmParams, ALSModel] {
  def apply(id: String, params: ALSAlgorithmParams,
    sc: Option[SparkContext]) = {
    new ALSModel(
      cl = sc.get
        .objectFile[Classifier[String, String]](s"/tmp/${id}/cl").first
    )
  }
}
