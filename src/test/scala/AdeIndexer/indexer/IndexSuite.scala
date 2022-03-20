package AdeIndexer.indexer

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.BeforeAndAfterAll
import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.indexer.Index.{addFilesToIndex, searchIndexAll, searchIndexAndScoreAll, searchIndexByBoolean}
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.store.FSDirectory

import java.io.File
import java.nio.file.Files
import java.nio.file.{Path, Paths}

class IndexSuite extends AnyFunSuite with BeforeAndAfterAll {
  val tempDir = Files.createTempDirectory("tempIndex").toFile
  val mockConfig = AdeIndexerConfig(
    loggerName = "test",
    directory = Paths.get("src", "test", "resources").toString,
    indexDirectory = tempDir.getAbsolutePath.toString
  )

  test("The inverted index actually index all .txt files.") {
    println(mockConfig.indexDirectory)
    addFilesToIndex(mockConfig)
    val indexDirectory = FSDirectory.open(Path.of(mockConfig.indexDirectory))
    val fileDirectory = FSDirectory.open(Path.of(mockConfig.directory))
    val numberOfFiles = fileDirectory.listAll().filter(filename => filename.endsWith(".txt")).length
    val indexReader = DirectoryReader.open(indexDirectory)
    val totalIndexedDocs = indexReader.numDocs()
    assert(totalIndexedDocs == numberOfFiles)
  }

  override def afterAll(): Unit = {
    tempDir.deleteOnExit()
  }
}
