package AdeIndexer.config

object Indexer {

  case class AdeIndexerConfig(
                               loggerName: String = "AdeIndexer",
                               directory: String = System.getProperty("user.dir"),
                               indexDirectory: String = System.getProperty("user.dir") + "index/"
                             )
}