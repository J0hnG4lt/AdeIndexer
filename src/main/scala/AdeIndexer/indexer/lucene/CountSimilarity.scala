package AdeIndexer.indexer.lucene

/** Custom similarity for conducting simple counts.
 * */


import org.apache.lucene.search.similarities.{BasicStats, SimilarityBase}

import java.util.logging.Logger

/** A custom Similarity to count the number of appearances of single terms in the inverted index.
 * See [[org.apache.lucene.search.similarities.SimilarityBase]].
 * */
class CountSimilarity extends SimilarityBase {

  private val logger = Logger.getLogger(this.getClass.getName)

  /** Return the number of times a term has occurred in a document.
   * @param basicStats: See [[org.apache.lucene.search.similarities.BasicStats]].
   * @param v: the number of times a term has occurred in a document.
   * @param v1: the document length.
   * */
  override def score(basicStats: BasicStats, v: Double, v1: Double): Double = {
    logger.fine(s"CountSimilarity v: $v v1: $v1")
    logger.fine(s"basic stats ${basicStats.getDocFreq} ${basicStats.getTotalTermFreq} ${basicStats.getNumberOfDocuments} ${basicStats.getNumberOfFieldTokens}")
    if basicStats.getTotalTermFreq > 0 then 1.toDouble else 0.toDouble
  }

  override def toString: String = "CountSimilarity"
}

