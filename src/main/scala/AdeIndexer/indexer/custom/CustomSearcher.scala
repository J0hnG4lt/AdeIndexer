package AdeIndexer.indexer.custom

import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.indexer.custom.CustomIndexer
import AdeIndexer.postprocessing.Scaler.rescaleScores
import AdeIndexer.indexer.SearcherBase

import java.nio.file.Path
import java.util.logging.Logger
import scala.collection.mutable


/** Defines all the methods related to Search that should be used with the Custom Indexer.
 *
 * */
class CustomSearcher extends SearcherBase {

  var customIndexer: Option[CustomIndexer] = None
  private val logger = Logger.getLogger(this.getClass.getName)

  /*** Counts the number of occurrences of all the words in every document and, then, rescales
   * into 0 <= x <= 100. 0 for no occurrences and 100 for all occurrences.
   *
   * @param documents  : indexed sequence that maps document Ids to their paths.
   * @param invertedIndex : a map of the form Token -> Set[Document ID].
   * @param words : a list of tokens that should be searched.
   * @return a map of filepath -> score
   * */
  def count(
             documents: mutable.IndexedSeq[Path],
             invertedIndex: mutable.HashMap[String, mutable.HashSet[Int]],
             words: List[String]
           ) :Map[String, Float] = {
    // Get all documents with a 0 score
    logger.fine(s"${words.size} words in the query.")
    val allDocs = documents.map(
      path => (path.toAbsolutePath.toString, 0.toFloat)
    ).toMap[String, Float]
    logger.fine(s"${allDocs.size} documents in total.")

    // Get all documents in which the words appear
    var appearances = List.empty[String]
    words.foreach(
      word => {
        val documentsForWord = invertedIndex.applyOrElse(word, _ => mutable.HashSet.empty[Int])
        documentsForWord.foreach(
          documentId => {
            val document = documents.apply(documentId)
            // TODO: this should be done with the document ID:
            appearances = appearances.prepended( document.toAbsolutePath.toString )
          }
        )
      }
    )

    // Sum all occurrences by document and get all the other documents with their default score of 0.
    val scoredDocs = appearances.groupMapReduce(elem => elem)(_ => 1.toFloat)(_ + _)
    logger.fine(s"${scoredDocs.size} appearances in total.")

    val mergedDocs = allDocs.map(
      docAll => {
        (docAll._1, scoredDocs.getOrElse(docAll._1, docAll._2))
      }
    )
    // Rescale scores to 0 <= x <= 100
    val docsWithRescaledScores = rescaleScores(scoredDocs = mergedDocs, maxValue = Some(words.size))
    docsWithRescaledScores

  }


  /*** Add all the documents to the inverted index.
   *
   * @param config : the config for the indexer.
   * */
  def addFilesToIndex(config: AdeIndexerConfig): Unit = {
    logger.fine("addFilesToIndex")
      val folder = Path.of(config.directory)
      if ( this.customIndexer.isEmpty ) {
        this.customIndexer = Some(CustomIndexer(folder=folder))
      }
  }

  /** Applies [[this.addFilesToIndex]] and [[this.count]] and then returns all the ranked documents.
   *
   * @param query  : words separated by whitespace.
   * @param config : a config case class for the Indexer.
   * @return a map of filepath -> score
   * */
  def searchIndexAndScoreAll(query: String, config: AdeIndexerConfig): Map[String, Float] = {
    logger.fine("searchIndexAndScoreAll")
    val folder = Path.of(config.directory)
    this.addFilesToIndex(config = config)
    val indexer = this.customIndexer.get
    val documents = indexer.loadDocuments()
    val invertedIndex = indexer.loadIndex()

    val tokens = indexer.tokenize(query).toList
    val rankedDocuments = this.count(
      documents = documents,
      invertedIndex = invertedIndex,
      words = tokens
    )
    rankedDocuments
  }

}
