package AdeIndexer

import AdeIndexer.cli.ArgParser

import AdeIndexer.logging.LoggerUtils.prettyPrint
import AdeIndexer.indexer.Index.{
  addFilesToIndex,
  searchIndexAndScoreAll
}
import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.config.ArgParser.ArgParserConfig
import java.util.logging.Logger
import AdeIndexer.repl.IndexingRepl

object Main {

  private val logger = Logger.getLogger(this.getClass.getName)

  /** Builds an argparser for the user and starts the REPL if no query is given as argument. If a query is given
   * as an argument, then a single search is conducted. To do this, an inverted index is built with Lucene.
   *
   * @param args: input arguments for the AdeIndexer.
   * */
  def main(args: Array[String]): Unit = {
    // Build the arg parser.
    val parser = ArgParser.buildParser()
    val argConfig = cli.ArgParser.parse(parser = parser, args = args)

    logger.info("Input arguments: ")
    logger.info(prettyPrint(argConfig))

    // Configure our indexer with the input arguments.
    val config = AdeIndexerConfig(
      directory = argConfig.directory,
      indexDirectory = argConfig.indexDirectory
    )

    // Build the inverted index.
    addFilesToIndex(config=config)

    // if a query is given, then return all scored documents. Otherwise, start a REPL and wait for user input.
    val repl = new IndexingRepl(config=config)
    var scoredDocs: Option[Map[String, Float]] = None
    argConfig.query match {
      case None => repl.startReplLoop()
      case Some(query) => scoredDocs = Some(searchIndexAndScoreAll(query=query, config=config))
    }
    scoredDocs match {
      case Some(docs) => println(docs.toString())
      case _ => println("REPL finished.")
    }
  }

}
