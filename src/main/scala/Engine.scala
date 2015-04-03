package org.template.classification

import io.prediction.controller.IEngineFactory
import io.prediction.controller.Engine

case class Query(
  val text: String
) extends Serializable

case class PredictedResult(
  val queryResults: String
) extends Serializable

object ClassificationEngine extends IEngineFactory {
  def apply() = {
    new Engine(
      classOf[DataSource],
      classOf[Preparator],
      Map("als" -> classOf[ALSAlgorithm]),
      classOf[Serving])
  }
}
