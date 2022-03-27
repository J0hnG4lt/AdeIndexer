package AdeIndexer.indexer.custom

import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.indexer.custom.CustomIndexer
import AdeIndexer.postprocessing.Scaler.rescaleScores
import AdeIndexer.indexer.SearcherBase

import java.nio.file.Path
import scala.collection.mutable

class CustomSearcher extends SearcherBase {

  var customIndexer: Option[CustomIndexer] = None

  def count(
             documents: mutable.IndexedSeq[Path],
             invertedIndex: mutable.HashMap[String, mutable.HashSet[Int]],
             words: List[String]
           ) :Map[String, Float] = {
    val allDocs = documents.map(
      path => (path.toAbsolutePath.toString, 0.toFloat)
    ).toMap[String, Float]
    var appearances = List.empty[String]
    words.foreach(
      word => {
        val documentsForWord = invertedIndex.apply(word)
        documentsForWord.foreach(
          documentId => {
            val document = documents.apply(documentId)
            appearances = appearances.prepended( document.toAbsolutePath.toString )
          }
        )
      }
    )
    val scoredDocs = appearances.groupMapReduce(elem => elem)(_ => 1.toFloat)(_ + _)
    val mergedDocs = allDocs.map(
      docAll => {
        (docAll._1, scoredDocs.getOrElse(docAll._1, docAll._2))
      }
    )
    // Rescale scores to 0 <= x <= 100
    val docsWithRescaledScores = rescaleScores(mergedDocs)
    docsWithRescaledScores

  }

  def addFilesToIndex(config: AdeIndexerConfig): Unit = {
      val folder = Path.of(config.directory)
      if ( this.customIndexer.isEmpty ) {
        this.customIndexer = Some(CustomIndexer(folder=folder))
      }
  }

  def searchIndexAndScoreAll(query: String, config: AdeIndexerConfig): Map[String, Float] = {
    val folder = Path.of(config.directory)
    this.addFilesToIndex(config = config)
    val indexer = this.customIndexer.get
    val documents = indexer.loadDocuments()
    val invertedIndex = indexer.loadIndex()

    val tokens = indexer.tokenize(query).toList
    val rankedDocuments = count(
      documents = documents,
      invertedIndex = invertedIndex,
      words = tokens
    )
    rankedDocuments
  }

}
