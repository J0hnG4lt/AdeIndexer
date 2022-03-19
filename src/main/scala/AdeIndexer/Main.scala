package AdeIndexer

import AdeIndexer.cli.ArgParser

import AdeIndexer.logging.LoggerUtils.prettyPrint
import AdeIndexer.indexer.Index.{
  addFilesToIndex,
  searchIndexAndScoreAll
}
import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.config.ArgParser.ArgParserConfig
import AdeIndexer.postprocessing.Scaler
import java.util.logging.Logger
import AdeIndexer.repl.IndexingRepl

object Main {

  val logger = Logger.getLogger(this.getClass.getName)

  def main(args: Array[String]): Unit = {
    val parser = ArgParser.buildParser()
    val argConfig = cli.ArgParser.parse(parser = parser, args = args)

    logger.info("Input arguments: ")
    logger.info(prettyPrint(argConfig))

    val config = AdeIndexerConfig(
      directory = argConfig.directory,
      indexDirectory = argConfig.indexDirectory
    )
    addFilesToIndex(config=config)

    val repl = new IndexingRepl(config=config)
    var scoredDocs: Option[Map[String, Float]] = None
    argConfig.query match {
      case None => repl.startReplLoop()
      case Some(query) => {
        scoredDocs = Some(searchIndexAndScoreAll(query=query, config=config))
      }
    }
    scoredDocs match {
      case Some(docs) => println(docs.toString())
      case _ => None
    }
  }

}
