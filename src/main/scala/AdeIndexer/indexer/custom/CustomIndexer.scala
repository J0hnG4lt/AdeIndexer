package AdeIndexer.indexer.custom

import java.nio.file.Path
import java.util.logging.Logger
import scala.util.control.Breaks.{break, breakable}
import java.io.File
import scala.collection.mutable


/** Defines a custom in-memory indexer that uses mutable data structures defined in Scala Collections.
 *
 * */
class CustomIndexer(folder: Path) {
  var invertedIndex: Option[mutable.HashMap[String, mutable.HashSet[Int]]] = None
  var documents: Option[mutable.IndexedSeq[Path]] = None
  val splitRegex = "\\s+"
  private val logger = Logger.getLogger(this.getClass.getName)

  /*** Gets an indexed sequences of file paths with efficient lookups.
   *
   * @return an indexed sequences of file paths.
   * */
  def getFilePaths: mutable.IndexedSeq[Path]  = {
    val file = folder.toFile
    if (!file.isDirectory){
      throw IllegalArgumentException("a folder should be given.")
    }
    file.listFiles
      .filter(_.isFile)
      .filter(_.getName.endsWith(".txt"))
      .map(_.toPath)
      .to(mutable.IndexedSeq)
  }

  /*** Gets an indexed sequences of file paths with efficient lookups. This is stored into [[this.documents]].
   *
   * @return an indexed sequences of file paths.
   * */
  def loadDocuments(): mutable.IndexedSeq[Path] = {
    val filePaths = this.documents match {
      case None =>
        this.documents = Some(this.getFilePaths)
        this.documents.get
      case Some(existingDocuments) => existingDocuments
    }
    logger.info("Document paths loaded into memory.")
    filePaths
  }

  /*** Builds an inverted index for balance efficient lookups and updates. To reduce memory consumption, document IDs
   * are used.
   *
   * @return a hashmap of word -> documents that contain that word.
   * */
  def indexDocuments(): mutable.HashMap[String, mutable.HashSet[Int]] = {
    val index = this.invertedIndex.getOrElse(
      mutable.HashMap.empty[String, mutable.HashSet[Int]]
    )
    val documents = this.documents.get
    documents.zipWithIndex.foreach {
      case (filepath: Path, documentId: Int) =>
        indexDocument(
          filepath = filepath,
          index = index,
          documentId = documentId
        )
    }
    index
  }

  /*** Mutates [[this.invertedIndex]] for all the words contained in the document represented by [[filepath]] and
   * adds its [[documentId]] into the hash sets corresponding to these words.
   *
   * @param filepath: the filepath of the document with given documentId.
   * @param index: the data structured where the indexed words are being stored.
   * @param documentId: the ID of the document that is to be indexed.
   * */
  def indexDocument(
                     filepath: Path,
                     index:  mutable.HashMap[String, mutable.HashSet[Int]],
                     documentId: Int
                   ): Unit = {

    val file = filepath.toFile
    logger.fine(s"Indexing File: ${filepath.toAbsolutePath}")

    val source = scala.io.Source.fromFile(file)
    val lines = source.mkString
    val tokens = this.tokenize(lines)
    tokens.foreach(
      token => {
        val documentSet = index.getOrElse(token, mutable.HashSet.empty[Int])
        documentSet.add(documentId)
        index.update(token, documentSet)
      }
    )
  }

  /*** Builds an inverted index for balance efficient lookups and updates. To reduce memory consumption, document IDs
   * are used. This inverted index is stored into [[this.invertedIndex]]
   *
   * @return a hashmap of word -> documents that contain that word.
   * */
  def loadIndex(): mutable.HashMap[String, mutable.HashSet[Int]] = {
    val index = this.invertedIndex match {
      case None =>
        this.invertedIndex = Some(this.indexDocuments())
        this.invertedIndex.get
      case Some(existingIndex) => existingIndex
    }
    logger.info("Index loaded into memory.")
    index
  }

  def tokenize(doc: String): Array[String] = {
    doc.split(this.splitRegex).toSet.toArray
  }

}
