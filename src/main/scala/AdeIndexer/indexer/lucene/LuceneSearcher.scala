package AdeIndexer.indexer.lucene

import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.indexer.SearcherBase
import AdeIndexer.indexer.lucene.LuceneIndexer
import AdeIndexer.postprocessing.Scaler.rescaleScores
import org.apache.lucene.index.{DirectoryReader, Term}
import org.apache.lucene.search.{BooleanClause, BooleanQuery, IndexSearcher, MatchAllDocsQuery, TermQuery}

import java.nio.file.Path
import java.util.logging.Logger

class LuceneSearcher extends SearcherBase {

  private val logger = Logger.getLogger(this.getClass.getName)
  var luceneIndexer: Option[LuceneIndexer] = None

  def addFilesToIndex(config: AdeIndexerConfig): Unit = {
    val folder = Path.of(config.directory)
    if ( this.luceneIndexer.isEmpty ) {
      this.luceneIndexer = Some(LuceneIndexer())
      this.luceneIndexer.get.addFilesToIndex(config = config)
    }
  }

  /** Search a built index by using a OR-type of Lucene Boolean query.
   * The indexer in config.indexDirectory is read and all the documents matching the query
   * are returned with a score and a filepath. The Similarity measure that we currently use
   * is a simple counter. For example, if query == "Georvic Tur" and "Georvic" appears in the document whereas "Tur"
   * does not appear, then the score would be 50 for that document. If both words appear, then the score would be 100.
   *
   * @param query  : words separated by whitespace.
   * @param config : a config case class for the Indexer.
   * @return a map of filepath -> score
   * */
  def searchIndexByBoolean(query: String, config: AdeIndexerConfig): Map[String, Float] = {
    logger.fine("searchIndexByBoolean")
    val indexer = this.luceneIndexer.get

    // Get the directory with the inverted index.
    val indexDirectory = indexer.buildDirectory(directoryUri = config.indexDirectory)
    val indexReader = DirectoryReader.open(indexDirectory)
    val totalIndexedDocs = indexReader.numDocs()

    logger.fine("Inverted Index: " + DirectoryReader.listCommits(indexDirectory).toArray().mkString("\n"))
    logger.fine("Total number of indexed docs: " + totalIndexedDocs)

    // Build a searcher for the indexed documents.
    val searcher = new IndexSearcher(indexReader)
    // We need to use the same similarity that we used during indexing.
    // TODO: allow for the use of a different similarity.
    searcher.setSimilarity(CountSimilarity())

    // We use a boolean OR query for all words given as input.
    val booleanQueryBuilder = new BooleanQuery.Builder
    val words = query.split(" ")
    words.foreach(word => {
      logger.fine(s"Query Word: $word")
      booleanQueryBuilder.add(new TermQuery(new Term("contents", word)), BooleanClause.Occur.SHOULD)
    }
    )
    val booleanQuery = booleanQueryBuilder.build()

    // Search documents and retrieve their paths and scores.
    val results = searcher.search(booleanQuery, totalIndexedDocs)
    val scoredDocs = results.scoreDocs.map(
      scoreDoc => {
        val idoc = scoreDoc.doc
        val doc = searcher.doc(idoc)
        (doc.getField("path").stringValue, scoreDoc.score)
      }
    ).toMap
    logger.fine("totalHits: " + results.totalHits.toString)
    scoredDocs
  }

  /** Retrieves all the documents of the index in config.indexDirectory with a score of 0.
   *
   * @param query  : words separated by whitespace.
   * @param config : a config case class for the Indexer.
   * @return a map of filepath -> score
   * */
  def searchIndexAll(query: String, config: AdeIndexerConfig): Map[String, Float] = {
    logger.fine("searchIndexAll")
    val indexer = this.luceneIndexer.get

    // Get the directory with the inverted index.
    val indexDirectory = indexer.buildDirectory(directoryUri = config.indexDirectory)
    val indexReader = DirectoryReader.open(indexDirectory)
    val totalIndexedDocs = indexReader.numDocs()

    logger.fine("Inverted Index: " + DirectoryReader.listCommits(indexDirectory).toArray().mkString("\n"))
    logger.fine("Total number of indexed docs: " + totalIndexedDocs)

    // Build a searcher for the indexed documents.
    val searcher = new IndexSearcher(indexReader)
    val matchAllDocsQuery = new MatchAllDocsQuery()
    val results = searcher.search(matchAllDocsQuery, totalIndexedDocs)

    // Search documents and retrieve their paths and scores. The score for this searcher is always zero.
    val scoredDocs = results.scoreDocs.map(
      scoreDoc => {
        val idoc = scoreDoc.doc
        val doc = searcher.doc(idoc)
        (doc.getField("path").stringValue, 0.toFloat)
      }
    ).toMap
    scoredDocs
  }

  /** Applies [[searchIndexByBoolean]] and [[searchIndexAll]] and then merges their results by using
   * the scores given by [[searchIndexByBoolean]].
   *
   * @param query  : words separated by whitespace.
   * @param config : a config case class for the Indexer.
   * @return a map of filepath -> score
   * */
  def searchIndexAndScoreAll(query: String, config: AdeIndexerConfig): Map[String, Float] = {
    logger.fine("searchIndexAndScoreAll")

    val allDocs = searchIndexAll(query = query, config = config)
    val scoredDocs = searchIndexByBoolean(query = query, config = config)

    // Get all the documents in the index but only use the score given by the query.
    val mergedDocs = allDocs.map(
      docAll => {
        (docAll._1, scoredDocs.getOrElse(docAll._1, docAll._2))
      }
    )

    // Rescale scores to 0 <= x <= 100
    val docsWithRescaledScores = rescaleScores(mergedDocs)
    docsWithRescaledScores
  }

}
