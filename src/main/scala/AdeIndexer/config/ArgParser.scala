package AdeIndexer.config

import java.nio.file.Paths

object ArgParser {

  case class ArgParserConfig(
                              directory: String = System.getProperty("user.dir"),
                              indexDirectory: String = Paths.get(System.getProperty("user.dir"), "index").toString,
                              query: Option[String] = None,
                              indexer: String = "Custom",
                              wrongArgument: Boolean = false
                            )
}
