package AdeIndexer.indexer.custom

import java.nio.file.Path
import java.util.logging.Logger
import scala.util.control.Breaks.{break, breakable}
import java.io.File
import scala.collection.mutable


class CustomIndexer(folder: Path) {
  var invertedIndex: Option[mutable.HashMap[String, mutable.HashSet[Int]]] = None
  var documents: Option[mutable.IndexedSeq[Path]] = None
  val splitRegex = "\\s+"
  private val logger = Logger.getLogger(this.getClass.getName)

  def getFilePaths(): mutable.IndexedSeq[Path]  = {
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

  def loadDocuments(): mutable.IndexedSeq[Path] = {
    val filePaths = this.documents match {
      case None => {
        this.documents = Some(this.getFilePaths())
        this.documents.get
      }
      case Some(existingDocuments) => existingDocuments
    }
    filePaths
  }

  def indexDocuments(): mutable.HashMap[String, mutable.HashSet[Int]] = {
    val index = this.invertedIndex.getOrElse(
      mutable.HashMap.empty[String, mutable.HashSet[Int]]
    )
    val documents = this.documents.get
    documents.zip(0 until documents.size).foreach {
      case (filepath: Path, documentId: Int) => {
        indexDocument(
          filepath = filepath,
          index = index,
          documentId = documentId
        )
      }
    }
    index
  }

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

  def loadIndex(): mutable.HashMap[String, mutable.HashSet[Int]] = {
    val index = this.invertedIndex match {
      case None => {
        this.invertedIndex = Some(this.indexDocuments())
        this.invertedIndex.get
      }
      case Some(existingIndex) => existingIndex
    }
    index
  }

  def tokenize(doc: String): Array[String] = {
    doc.split(this.splitRegex)
  }

}
