package AdeIndexer.postprocessing

object Scaler {
  def rescaleScores(scoredDocs: Map[String, Float]): Map[String, Float] = {
    val maxValue = scoredDocs.maxBy(_._2)._2
    scoredDocs.map(
      (key, value) => {
        (key, (value/maxValue)*100)
      }
    )
  }
}
