package AdeIndexer.indexer

import org.apache.lucene.search.similarities.{BasicStats, SimilarityBase}

import java.util.logging.Logger


class CountSimilarity extends SimilarityBase {

  val logger = Logger.getLogger(this.getClass.getName)

  override def score(basicStats: BasicStats, v: Double, v1: Double): Double = {
    logger.fine(s"CountSimilarity v: ${v} v1: ${v1}")
    v
  }

  override def toString: String = "CountSimilarity"
}

