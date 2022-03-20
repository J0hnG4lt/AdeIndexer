package AdeIndexer.config

import java.nio.file.Paths

object Indexer {

  case class AdeIndexerConfig(
                               loggerName: String = "AdeIndexer",
                               directory: String = System.getProperty("user.dir"),
                               indexDirectory: String = Paths.get(System.getProperty("user.dir"), "index").toString
                             )
}