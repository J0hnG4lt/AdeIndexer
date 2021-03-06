package AdeIndexer.indexer

import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.indexer.custom.CustomSearcher
import AdeIndexer.indexer.lucene.LuceneSearcher
import AdeIndexer.indexer.SearcherBase


object SearcherFactory {

  val names: List[String] = List("Lucene", "Custom")

  def buildSearcher(name: String): SearcherBase = {
    name match {
      case name2 if name2 == "Lucene" => LuceneSearcher()
      case name2 if name2 == "Custom" => CustomSearcher()
      case _ => throw IllegalArgumentException(s"$name is not a valid Indexer")
    }
  }
}
