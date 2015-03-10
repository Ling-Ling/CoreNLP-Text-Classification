package org.template.recommendation

import io.prediction.controller.PAlgorithm
import io.prediction.controller.Params
import io.prediction.data.storage.BiMap

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.{Rating => MLlibRating}
import org.apache.spark.mllib.recommendation.ALSModel

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

case class ALSAlgorithmParams(
  val rank: Int,
  val numIterations: Int,
  val lambda: Double) extends Params

class ALSAlgorithm(val ap: ALSAlgorithmParams)
  extends PAlgorithm[PreparedData, ALSModel, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]
  val cdc = new ColumnDataClassifier("data/medtest.prop");
  
  def train(data: PreparedData): ALSModel = {
    // MLLib ALS cannot handle empty training data.
    require(!data.texts.take(1).isEmpty,
      s"RDD[TextClass] in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")

    var cdata = data.texts.collect();
/*    var clabel = new LinkedList<String>;
    var cvalue = new LinkedList<String>
    var labelIndex: Index[String] = new HashIndex[String](); 
    labelIndex.addAll(data.texts.map(_.text_type));
    var valueIndex: Index[String] = new HashIndex[String](); 
    valueIndex.addAll(data.texts.map(_.text));
    val clDateset = new Dataset(data.text.length, labelIndex, valueIndex);
    val classifier = cdc.makeClassifier(clDataset);
*/    
    val classifier = cdc.makeClassifier(cdc.readTrainingExamples("data/medtest.train"))
    new ALSModel(
      cl = classifier)
  }

  def predict(model: ALSModel, query: Query): PredictedResult = {
    
    val cl = model.cl
    var line = ""
    val d = cdc.makeDatumFromLine("\t" + query.text)
    new PredictedResult(query.text + " ==> " + cl.classOf(d))
  }
}
