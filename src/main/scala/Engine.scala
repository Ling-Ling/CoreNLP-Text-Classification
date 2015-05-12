package org.template.classification

import io.prediction.controller.IEngineFactory
import io.prediction.controller.Engine

case class Query(
  val text: String,
  val gender: Option[Set[String]],
  val dizziness: Option[Set[String]],
  val convulsions: Option[Set[String]],
  val heart_palpitation: Option[Set[String]],
  val shortness_of_breath: Option[Set[String]],
  val headaches: Option[Set[String]],
  val effect_decreased: Option[Set[String]],
  val allergies_worse: Option[Set[String]],
  val bad_interaction: Option[Set[String]],
  val nausea: Option[Set[String]],
  val insomnia: Option[Set[String]]
) extends Serializable

case class PredictedResult(
  val queryResults: String
) extends Serializable

object ClassificationEngine extends IEngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("als" -> classOf[NLPAlgorithm]),
      classOf[Serving])
  }
}
