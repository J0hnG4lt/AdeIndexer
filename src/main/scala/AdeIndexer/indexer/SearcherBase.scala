package AdeIndexer.indexer

import AdeIndexer.config.Indexer.AdeIndexerConfig

abstract class SearcherBase{
  def searchIndexAndScoreAll(query: String, config: AdeIndexerConfig): Map[String, Float]
  def addFilesToIndex(config: AdeIndexerConfig): Unit
}
