package AdeIndexer.indexer.lucene

import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.postprocessing.Scaler.rescaleScores
import AdeIndexer.indexer.SearcherBase

import org.apache.lucene.analysis.custom.CustomAnalyzer
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.search.*
import org.apache.lucene.store.FSDirectory

import java.nio.file.Path
import java.util.logging.Logger
import scala.util.control.Breaks.{break, breakable}

/** Defines all the methods related to the Inverted Index implemented by Lucene.
 *
 * */
class LuceneIndexer {

  private val logger = Logger.getLogger(this.getClass.getName)
  val splitRegex = "\\s+"

  /** Builds a File System Directory representation for Lucene.
   *
   * @param directoryUri : the path to the local directory.
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
   * @param config : a config case class for the Indexer.
   * */
  def addFilesToIndex(config: AdeIndexerConfig): Unit = {

    // Get the directory with files and the directory for the index.
    val indexDirectory = buildDirectory(directoryUri = config.indexDirectory)
    val fileDirectory = buildDirectory(directoryUri = config.directory)

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
          if (!filename.endsWith(".txt")) {
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

  def tokenize(doc: String): Array[String] = {
    doc.split(this.splitRegex).toSet.toArray
  }

}
