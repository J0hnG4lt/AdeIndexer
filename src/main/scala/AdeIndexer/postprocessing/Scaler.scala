package AdeIndexer.postprocessing
/** Utilities for rescaling Lucene scores.
 * */


/** A scaler for the scores returned by Lucene.
 * */
object Scaler {

  /** Rescale the max score into 100 and the min to 0.
   * @param scoredDocs: a map of identifier -> score.
   * @return the same map, but with rescaled scores.
   * */
  def rescaleScores(
                     scoredDocs: Map[String, Float],
                   maxValue: Option[Float] = None
                   ): Map[String, Float] = {
    val maxVal = maxValue match {
      case None => scoredDocs.maxBy(_._2)._2
      case Some(v) => v
    }
    scoredDocs.map(
      (key: String, value: Float) => {
        require(value <= maxVal, s"$value is greater than $maxVal")
        (key, if maxVal > 0 then (value/maxVal)*100 else 0.toFloat)
      }
    )
  }
}
