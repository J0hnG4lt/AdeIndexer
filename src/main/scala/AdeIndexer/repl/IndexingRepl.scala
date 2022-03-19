package AdeIndexer.repl

import util.control.Breaks.{breakable, break}
import scala.io.StdIn.readLine
import AdeIndexer.indexer.Index.searchIndexAndScoreAll
import AdeIndexer.postprocessing.Scaler.rescaleScores
import AdeIndexer.config.Indexer.AdeIndexerConfig

object Commands extends Enumeration {
  type Command = Value

  val quit = Value(":quit")
  val help = Value(":help")
}

class IndexingRepl(config: AdeIndexerConfig) {

  val commands: String = Commands.values.mkString(", ")

  val instructions: String =
    s"""
      | To search for ocurrences of any set of words, just enter them separated by a space.
      | For Example:
      | search> Deutschland Frankreich
      | You can use the following commands: ${commands}
      |""".stripMargin

  val prompt: String = "search> "

  val greeting: String =
    """
      | Hello, welcome to the AdeIndexer!
      |""".stripMargin

  def quit(): Unit = break

  def printHelp(): Unit = println(instructions)

  def runIndexer(words: String):Unit = {
    val scoredDocs = searchIndexAndScoreAll(query=words, config=config)
    val rescaledDocs = rescaleScores(scoredDocs)
    println(rescaledDocs.toString())
  }

  def runCommand(inputValue: String): Unit = {
    inputValue match {
      case command if command == Commands.quit.toString => quit()
      case command if command == Commands.help.toString => printHelp()
      case words => runIndexer(words)
    }
  }

  def startReplLoop():Unit = {
    println(greeting)
    println(instructions)
    breakable {
      while (true) {
        val inputValue = readLine(prompt)
        runCommand(inputValue)
      }
    }
  }
}
