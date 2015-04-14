package org.template.classification

import io.prediction.controller.PAlgorithm
import io.prediction.controller.Params

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.recommendation.{Rating => MLlibRating}
import org.apache.spark.mllib.classification.Model

import grizzled.slf4j.Logger
import scala.collection.JavaConversions._

import edu.stanford.nlp.classify.Classifier;
import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.classify.GeneralDataset;
import edu.stanford.nlp.classify.Dataset;
import edu.stanford.nlp.util.Index;
import edu.stanford.nlp.util.HashIndex;

import scala.collection.mutable.LinkedList

case class AlgorithmParams(
  val lambda: Double) extends Params

class NLPAlgorithm(val ap: AlgorithmParams)
  extends PAlgorithm[PreparedData, Model, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]
  val cdc = new ColumnDataClassifier("data/medtest.prop");
  
  def train(sc: SparkContext, data: PreparedData): Model = {
    // MLLib ALS cannot handle empty training data.
    require(!data.texts.take(1).isEmpty,
      s"RDD[TextClass] in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")

    var labelIndex: Index[String] = new HashIndex[String](); 
    labelIndex.addAll(List.fromArray(data.texts.map(_.text_type).collect()));
    var valueIndex: Index[String] = new HashIndex[String](); 
    valueIndex.addAll(List.fromArray(data.texts.map(_.text).collect()));
    val clDataset = new Dataset(data.texts.count().toInt, valueIndex, labelIndex);
    val classifier = cdc.makeClassifier(clDataset);
    
    new Model(
      cl = classifier)
  }

  def predict(model: Model, query: Query): PredictedResult = {
    
    val cl = model.cl
    var line = ""
    val d = cdc.makeDatumFromLine("\t" + query.text)
    new PredictedResult(query.text + " ==> " + cl.classOf(d))
  }
}
