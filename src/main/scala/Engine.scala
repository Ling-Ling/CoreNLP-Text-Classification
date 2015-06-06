package org.template.classification

import io.prediction.controller.EngineFactory
import io.prediction.controller.Engine

class Query(
  val text: String,
  val gender: Option[String],
  val dizziness: Option[String],
  val convulsions: Option[String],
  val heart_palpitation: Option[String],
  val shortness_of_breath: Option[String],
  val headaches: Option[String],
  val effect_decreased: Option[String],
  val allergies_worse: Option[String],
  val bad_interaction: Option[String],
  val nausea: Option[String],
  val insomnia: Option[String]
) extends Serializable

class PredictedResult(
  val queryResults: String
) extends Serializable

class ActualResult(
  val queryResults: String
) extends Serializable

object ClassificationEngine extends EngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("als" -> classOf[NLPAlgorithm]),
      classOf[Serving])
  }
}
