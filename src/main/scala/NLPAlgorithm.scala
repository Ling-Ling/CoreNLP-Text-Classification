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
import java.io._
import Array._

case class AlgorithmParams(
  val lambda: Double) extends Params

class NLPAlgorithm(val ap: AlgorithmParams)
  extends PAlgorithm[PreparedData, Model, Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]
  val cdc = new ColumnDataClassifier("data/data.prop");
  
  def train(sc: SparkContext, data: PreparedData): Model = {
    // MLLib ALS cannot handle empty training data.
    require(!data.texts.take(1).isEmpty,
      s"RDD[TextClass] in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")

    var labelList = data.texts.map(_.text_type).collect();
    var textList = data.texts.map(_.text).collect();
    var genderList = data.texts.map(_.gender).collect();
    val pw = new PrintWriter("corenlpData")
    
    for (x <- 0 to data.texts.count().toInt-1) {
      pw.print(labelList(x) + "\t" + textList(x) + "\t" + genderList(x) + "\n")   
    }

    /*
    var labelIndex: Index[String] = new HashIndex[String](); 
    var featureIndex: Index[String] = new HashIndex[String](); 
    labelIndex.addAll(List.fromArray(data.texts.map(_.text_type).collect()));
    featureIndex.addAll(List.fromArray(data.texts.map(_.text).collect()));

    val clDataset = new Dataset(data.texts.count().toInt, featureIndex, labelIndex)
    val classifier = cdc.makeClassifier(clDataset);
    */
    
    val classifier = cdc.makeClassifier(cdc.readTrainingExamples("corenlpData"))
    new Model(cl = classifier)
  }

  def predict(model: Model, query: Query): PredictedResult = {
    
    val cl = model.cl
    /*var line = ""
    for (line <- ObjectBank.getLineIterator("data/medtest.test", "utf-8")) {
      val dd = cdc.makeDatumFromLine(line);
      System.out.println(line + " ==> " + cl.classOf(dd));
    }*/

    var line = "\t" + query.text + "\t"
    if (query.gender.isEmpty) line += "unknown"
    line += query.gender + "\t"
    if (query.dizziness.isEmpty) line += "unknown"
    line += query.dizziness + "\t"
    if (query.convulsions.isEmpty) line += "unknown"
    line += query.convulsions + "\t"
    if (query.heart_palpitation.isEmpty) line += "unknown"
    line += query.heart_palpitation + "\t";
    if (query.shortness_of_breath.isEmpty) line += "unknown"
    line += query.shortness_of_breath + "\t"
    if (query.headaches.isEmpty) line += "unknown"
    line += query.headaches + "\t"
    if (query.effect_decreased.isEmpty) line += "unknown"
    line += query.effect_decreased + "\t"
    if (query.allergies_worse.isEmpty) line += "unknown"
    line += query.allergies_worse + "\t";
    if (query.bad_interaction.isEmpty) line += "unknown"
    line += query.bad_interaction + "\t";
    if (query.nausea.isEmpty) line += "unknown"
    line += query.nausea + "\t";
    if (query.insomnia.isEmpty) line += "unknown"
    line += query.insomnia + "\t";

    val d = cdc.makeDatumFromLine(line)

    new PredictedResult(query.text + " ==> " + cl.classOf(d))
  }
}
