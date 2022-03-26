package AdeIndexer.repl

/** A Domain specific language for our Read-Eval-Print Loop that applies our Lucene Indexer.
 * */

import util.control.Breaks.{breakable, break}
import scala.io.StdIn.readLine
import AdeIndexer.indexer.lucene.Index.searchIndexAndScoreAll
import AdeIndexer.postprocessing.Scaler.rescaleScores
import AdeIndexer.config.Indexer.AdeIndexerConfig

/**
 * An enum for all the commands that we can use in the Read-Eval-Print Loop. This is like our DSL.
 * */
object Commands extends Enumeration {
  type Command = Value

  val quit: Value = Value(":quit")
  val help: Value = Value(":help")
}

/**
 * @constructor Create a repl (Read-Eval-Print Loop) object for the application of the Lucene Indexer.
 * @param config: a configuration object for the Lucene Indexer.
 * */
class IndexingRepl(config: AdeIndexerConfig) {

  private val commands: String = Commands.values.mkString(", ")

  private val instructions: String =
    s"""
      | To search for ocurrences of any set of words, just enter them separated by a space.
      | For Example:
      | search> Deutschland Frankreich
      | You can use the following commands: $commands
      |""".stripMargin

  private val prompt: String = "search> "

  private val greeting: String =
    """
      | Hello, welcome to the AdeIndexer!
      |""".stripMargin

  private def quit(): Unit = break

  private def printHelp(): Unit = println(instructions)

  /** Applies the indexer to the words and prints results to standard output.
   * @param words: words separated by single spaces.
   * */
  def runIndexer(words: String):Unit = {
    val scoredDocs = searchIndexAndScoreAll(query=words, config=config)
    println(scoredDocs.mkString("\n"))
  }

  /** Evaluates the input given by the user in [[startReplLoop]]. If a command in [[Commands]] is not matched,
   * then the input is interpreted as a query for the inverted index.
   *
   * @param inputValue: the input value given by the user at the prompt in the repl.
   * */
  def runCommand(inputValue: String): Unit = {
    inputValue match {
      case command if command == Commands.quit.toString => quit()
      case command if command == Commands.help.toString => printHelp()
      case words => runIndexer(words)
    }
  }

  /** Starts the REPL loop. Greets the user, prints instructions and wait for user input.
   * */
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
