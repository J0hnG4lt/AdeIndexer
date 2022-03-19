package AdeIndexer.config

object ArgParser {

  case class ArgParserConfig(
                              directory: String = System.getProperty("user.dir"),
                              indexDirectory: String = System.getProperty("user.dir") + "/index/",
                              query: Option[String] = None,
                              wrongArgument: Boolean = false
                            )
}