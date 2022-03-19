package AdeIndexer.postprocessing

object Scaler {
  def rescaleScores(scoredDocs: Map[String, Float]): Map[String, Float] = {
    val maxValue = scoredDocs.maxBy(_._2)._2
    scoredDocs.map(
      (key: String, value: Float) => {
        (key, if maxValue > 0 then (value/maxValue)*100 else 0.toFloat)
      }
    )
  }
}
