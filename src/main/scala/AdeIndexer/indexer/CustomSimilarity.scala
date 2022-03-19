package AdeIndexer.indexer

import org.apache.lucene.search.similarities.{BasicStats, SimilarityBase}
import AdeIndexer.logging.LoggerUtils.logger


class CustomSimilarity extends SimilarityBase {
  override def score(basicStats: BasicStats, v: Double, v1: Double): Double = {
    logger.info(s"v: ${v} v1: ${v1}")
    v
  }

  override def toString: String = "Similarity"
}

