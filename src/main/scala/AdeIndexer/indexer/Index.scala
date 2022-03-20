package AdeIndexer.indexer

import org.apache.lucene.analysis.standard.{StandardAnalyzer, StandardTokenizerFactory}
import org.apache.lucene.analysis.custom.CustomAnalyzer
import org.apache.lucene.document.{Document, Field, FieldType, StringField, TextField}
import org.apache.lucene.index.{DirectoryReader, IndexOptions, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.store.{Directory, FSDirectory}
import org.apache.lucene.search.{BooleanClause, BooleanQuery, IndexSearcher, MatchAllDocsQuery, Query, TermQuery}
import org.apache.lucene.queryparser.classic.QueryParser

import java.util.logging.Logger
import java.nio.file.{Path, Paths}
import java.io.{BufferedReader, File, FileReader}
import util.control.Breaks.{break, breakable}

import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.indexer.CountSimilarity
import AdeIndexer.postprocessing.Scaler.rescaleScores


/** Defines all the methods related to the Inverted Index implemented by Lucene.
 *
 * */
object Index {

  private val logger = Logger.getLogger(this.getClass.getName)

  /** Builds a File System Directory representation for Lucene.
   *
   * @param directoryUri: the path to the local directory.
   * @return a directory representation for Lucene.
   * */
  def buildDirectory(directoryUri: String): FSDirectory = {
    val directory = FSDirectory.open(Path.of(directoryUri))
    directory
  }

  /** Reads all the files contained in config.directory and builds and index in config.indexDirectory.
   * The contents, filename, filepaths of the files are taken into account.
   * Currently, a simple score based on counts is used as a Similarity measure.
   *
   * @param config: a config case class for the Indexer.
   * */
  def addFilesToIndex(config: AdeIndexerConfig): Unit = {

    // Get the directory with files and the directory for the index.
    val indexDirectory = buildDirectory(directoryUri=config.indexDirectory)
    val fileDirectory = buildDirectory(directoryUri=config.directory)

    // Use a custom analyzer to ensure exact matches. Lucene otherwise would apply NLP transformations to the tokens.
    val analyzer = CustomAnalyzer.builder().withTokenizer("standard").build()
    val indexWriterConfig = new IndexWriterConfig(analyzer)

    // We use a custom Similarity to replicate the Score that is required by the challenge.
    // TODO: use a better similarity or allow for the use of a different one by config
    indexWriterConfig.setSimilarity(CountSimilarity())
    indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
    val indexWriter = new IndexWriter(indexDirectory, indexWriterConfig)

    // Delete previous inverted index.
    indexWriter.deleteAll()

    val files = fileDirectory.listAll()
    files.foreach(
      filename => {
        breakable {

          // We only index text files.
          // TODO: extend this for other file formats.
          if (!filename.endsWith(".txt")){
            logger.fine(s"Skipping: $filename")
            break
          }

          // Read the file
          val path = fileDirectory.getDirectory.resolve(filename).toAbsolutePath
          val file = path.toFile
          logger.fine(s"Indexing File: ${path.toAbsolutePath}")

          val source = scala.io.Source.fromFile(file)
          val lines = source.mkString
          source.close()
          logger.fine(lines)

          // Index the file contents along with its path and filename
          val document = new Document()
          document.add(new TextField("contents", lines, Field.Store.NO))
          document.add(new StringField("path", file.getPath, Field.Store.YES))
          document.add(new StringField("filename", file.getName, Field.Store.YES))

          // Commit to Index
          indexWriter.addDocument(document)
          indexWriter.flush()
          indexWriter.commit()
        }
      }
    )

    // Close all open resources
    indexWriter.close()
    fileDirectory.close()
    indexDirectory.close()
    logger.info(s"All files in ${config.directory} have been indexed.")
  }

  /** Search a built index by using a OR-type of Lucene Boolean query.
   * The indexer in config.indexDirectory is read and all the documents matching the query
   * are returned with a score and a filepath. The Similarity measure that we currently use
   * is a simple counter. For example, if query == "Georvic Tur" and "Georvic" appears in the document whereas "Tur"
   * does not appear, then the score would be 50 for that document. If both words appear, then the score would be 100.
   *
   * @param query: words separated by whitespace.
   * @param config: a config case class for the Indexer.
   * @return a map of filepath -> score
   * */
  def searchIndexByBoolean(query: String, config: AdeIndexerConfig):Map[String, Float] = {
    logger.fine("searchIndexByBoolean")

    // Get the directory with the inverted index.
    val indexDirectory = buildDirectory(directoryUri=config.indexDirectory)
    val indexReader = DirectoryReader.open(indexDirectory)
    val totalIndexedDocs = indexReader.numDocs()

    logger.fine("Inverted Index: "+DirectoryReader.listCommits(indexDirectory).toArray().mkString("\n"))
    logger.fine("Total number of indexed docs: "+totalIndexedDocs)

    // Build a searcher for the indexed documents.
    val searcher = new IndexSearcher(indexReader)
    // We need to use the same similarity that we used during indexing.
    // TODO: allow for the use of a different similarity.
    searcher.setSimilarity(CountSimilarity())

    // We use a boolean OR query for all words given as input.
    val booleanQueryBuilder = new BooleanQuery.Builder
    val words = query.split(" ")
    words.foreach( word => {
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
   * @param query: words separated by whitespace.
   * @param config: a config case class for the Indexer.
   * @return a map of filepath -> score
   * */
  def searchIndexAll(query: String, config: AdeIndexerConfig):Map[String, Float]  = {
    logger.fine("searchIndexAll")

    // Get the directory with the inverted index.
    val indexDirectory = buildDirectory(directoryUri=config.indexDirectory)
    val indexReader = DirectoryReader.open(indexDirectory)
    val totalIndexedDocs = indexReader.numDocs()

    logger.fine("Inverted Index: "+DirectoryReader.listCommits(indexDirectory).toArray().mkString("\n"))
    logger.fine("Total number of indexed docs: "+totalIndexedDocs)

    // Build a searcher for the indexed documents.
    val searcher = new IndexSearcher(indexReader)
    val matchAllDocsQuery = new MatchAllDocsQuery()
    val results = searcher.search(matchAllDocsQuery, 10)

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
   * @param query: words separated by whitespace.
   * @param config: a config case class for the Indexer.
   * @return a map of filepath -> score
   * */
  def searchIndexAndScoreAll(query: String, config: AdeIndexerConfig): Map[String, Float]  = {
    logger.fine("searchIndexAndScoreAll")

    val allDocs = searchIndexAll(query=query, config = config)
    val scoredDocs = searchIndexByBoolean(query=query, config = config)

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
