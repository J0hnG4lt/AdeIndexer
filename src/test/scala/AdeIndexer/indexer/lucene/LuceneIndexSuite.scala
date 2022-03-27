package AdeIndexer.indexer.lucene

import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.indexer.lucene.LuceneIndexer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.store.FSDirectory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import java.nio.file.{Files, Path, Paths}

class LuceneIndexSuite extends AnyFunSuite with BeforeAndAfterAll {
  private val tempDir = Files.createTempDirectory("tempIndex").toFile
  private val mockConfig = AdeIndexerConfig(
    loggerName = "test",
    directory = Paths.get("src", "test", "resources").toString,
    indexDirectory = tempDir.getAbsolutePath
  )
  private val mockQuery = "Georvic"
  private val indexer = LuceneIndexer()

  test("The inverted index actually index all .txt files.") {
    println(mockConfig.indexDirectory)
    indexer.addFilesToIndex(config = mockConfig)
    val indexDirectory = FSDirectory.open(Path.of(mockConfig.indexDirectory))
    val fileDirectory = FSDirectory.open(Path.of(mockConfig.directory))
    val numberOfFiles = fileDirectory.listAll().count(filename => filename.endsWith(".txt"))
    val indexReader = DirectoryReader.open(indexDirectory)
    val totalIndexedDocs = indexReader.numDocs()
    assert(totalIndexedDocs == numberOfFiles, "Not all docs were indexed.")
  }
  
}
