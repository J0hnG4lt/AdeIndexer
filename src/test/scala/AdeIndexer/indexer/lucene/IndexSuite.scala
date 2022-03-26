package AdeIndexer.indexer.lucene

import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.indexer.lucene.Index.{addFilesToIndex, searchIndexAll, searchIndexAndScoreAll, searchIndexByBoolean}
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.store.FSDirectory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite

import java.io.File
import java.nio.file.{Files, Path, Paths}

class IndexSuite extends AnyFunSuite with BeforeAndAfterAll {
  private val tempDir = Files.createTempDirectory("tempIndex").toFile
  private val mockConfig = AdeIndexerConfig(
    loggerName = "test",
    directory = Paths.get("src", "test", "resources").toString,
    indexDirectory = tempDir.getAbsolutePath
  )
  private val mockQuery = "Georvic"

  test("The inverted index actually index all .txt files.") {
    println(mockConfig.indexDirectory)
    addFilesToIndex(config = mockConfig)
    val indexDirectory = FSDirectory.open(Path.of(mockConfig.indexDirectory))
    val fileDirectory = FSDirectory.open(Path.of(mockConfig.directory))
    val numberOfFiles = fileDirectory.listAll().count(filename => filename.endsWith(".txt"))
    val indexReader = DirectoryReader.open(indexDirectory)
    val totalIndexedDocs = indexReader.numDocs()
    assert(totalIndexedDocs == numberOfFiles, "Not all docs were indexed.")
  }

  test("searchIndexAll should return all documents.") {

    val scoredDocs = searchIndexAll(query = mockQuery, config = mockConfig)
    val fileDirectory = FSDirectory.open(Path.of(mockConfig.directory))
    val numberOfFiles = fileDirectory.listAll().count(filename => filename.endsWith(".txt"))
    val numberOfRetrievedScoredDocs = scoredDocs.size
    assert(numberOfRetrievedScoredDocs == numberOfFiles, "Not all docs were returned.")
  }

  test("searchIndexByBoolean should return only matched documents.") {

    val scoredDocs = searchIndexByBoolean(query = mockQuery, config = mockConfig)
    val fileDirectory = FSDirectory.open(Path.of(mockConfig.directory))
    val numberOfFilesWithMatchingQuery = 1
    val numberOfRetrievedScoredDocs = scoredDocs.size
    assert(
      numberOfRetrievedScoredDocs == numberOfFilesWithMatchingQuery,
      "The boolean query should have only matched one document"
    )
  }

  test("searchIndexAndScoreAll should return all documents, but only rank matching docs.") {

    val scoredDocs = searchIndexAndScoreAll(query = mockQuery, config = mockConfig)
    val fileDirectory = FSDirectory.open(Path.of(mockConfig.directory))
    val numberOfFiles = fileDirectory.listAll().count(filename => filename.endsWith(".txt"))
    val numberOfRetrievedScoredDocs = scoredDocs.size
    assert(numberOfRetrievedScoredDocs == numberOfFiles, "Not all docs were returned.")
    scoredDocs.foreach(
      docAndScore => {
        docAndScore._1 match {
          case filepath if filepath.endsWith("names.txt") => assert(
            docAndScore._2 == 100.toFloat,
            "We expeted that the document containing the query would have scored 100"
          )
          case filepath if !filepath.endsWith("names.txt") => assert(
            docAndScore._2 == 0.toFloat,
            "We expeted that documents not containing the query would have scored 0"
          )
        }
      }
    )
  }

  override def afterAll(): Unit = {
    tempDir.deleteOnExit()
  }
}
