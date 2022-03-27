package AdeIndexer.cli

import scopt.OParser

import AdeIndexer.exceptions.CustomExceptions.WrongArgumentsException
import AdeIndexer.config.ArgParser.ArgParserConfig
import AdeIndexer.indexer.SearcherFactory

object ArgParser {

  def buildParser(): OParser[Unit, ArgParserConfig] = {
    val builder = OParser.builder[ArgParserConfig]
    val parser = {
      OParser.sequence(
        builder.programName("AdeIndexer"),
        builder.head("AdeIndexer", "0.0.1"),
        builder.opt[String]('d', "directory")
          .action((directory, config) => config.copy(directory = directory))
          .text("d is the path to a directory with files that will be indexed."),
        builder.opt[String]('i', "index-directory")
          .action((indexDirectory, config) => config.copy(indexDirectory = indexDirectory))
          .text("i is the path to a directory where the index will be stored"),
        builder.opt[String]('n', "name-indexer")
          .action((indexer, config) => config.copy(indexer = indexer))
          .validate(indexer =>
            if (SearcherFactory.names.contains(indexer)) builder.success
            else builder.failure("Value <max> must be >0"))
          .text(s"n is the name of the indexer that will be used. Options: ${SearcherFactory.names.mkString(", ")}"),
        builder.opt[String]('q', "query")
          .action((query, config) => config.copy(query = Some(query)))
          .text("q is the query"),
        builder.help("help").text("prints this usage text")
        // more options here...
      )
    }
    parser
  }

  def parse(parser: OParser[Unit, ArgParserConfig], args: Array[String]): ArgParserConfig = {
    // OParser.parse returns Option[Config]
    OParser.parse(parser, args, ArgParserConfig()) match {
      case Some(config) => config
      case _ => throw WrongArgumentsException(s"Wrong arguments ${args.mkString(" ")}")
    }
  }
}
