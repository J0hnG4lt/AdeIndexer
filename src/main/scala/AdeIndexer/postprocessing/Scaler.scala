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
  def rescaleScores(scoredDocs: Map[String, Float]): Map[String, Float] = {
    val maxValue = scoredDocs.maxBy(_._2)._2
    scoredDocs.map(
      (key: String, value: Float) => {
        (key, if maxValue > 0 then (value/maxValue)*100 else 0.toFloat)
      }
    )
  }
}
