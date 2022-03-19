package AdeIndexer

import AdeIndexer.cli.ArgParser
import AdeIndexer.logging.LoggerUtils.logger
import AdeIndexer.logging.LoggerUtils.prettyPrint
import AdeIndexer.indexer.Index.{addFilesToIndex, buildDirectory, searchIndex}
import AdeIndexer.config.AdeIndexerConfig

object Main {

  def main(args: Array[String]): Unit = {
    val parser = ArgParser.buildParser()
    val argConfig = cli.ArgParser.parse(parser = parser, args = args)

    logger.info("Input arguments: ")
    logger.info(prettyPrint(argConfig))

    val config = AdeIndexerConfig(directory = argConfig.directory, indexDirectory = argConfig.indexDirectory)
    addFilesToIndex(config=config)
    //val directory = buildDirectory(config=config.directory)
    searchIndex(query=argConfig.query, config=config)
  }

}
