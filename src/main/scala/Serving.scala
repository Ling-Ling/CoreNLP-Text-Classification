package org.template.recommendation

import io.prediction.controller.LServing

import scala.io.Source
import io.prediction.controller.Params

case class ServingParams(filepath: String) extends Params

class Serving (val params: ServingParams) extends LServing[Query, PredictedResult] {

  override
  def serve(query: Query, 
            predictedResults: Seq[PredictedResult]): PredictedResult = {
    predictedResults.head
  }
}
