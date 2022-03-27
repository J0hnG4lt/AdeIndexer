package AdeIndexer.indexer.custom

import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.indexer.custom.CustomIndexer
import AdeIndexer.indexer.custom.CustomSearcher
import org.scalatest.Assertions.assert
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.Paths

class CustomSearcherSuite extends AnyFunSuite {

  private val mockConfig = AdeIndexerConfig(
    loggerName = "test",
    directory = Paths.get("src", "test", "resources").toString,
    indexDirectory = ""
  )
  private val mockQuery = "Georvic"
  private val customSearcher = CustomSearcher()
  private val scoredDocs = customSearcher.searchIndexAndScoreAll(
    query=mockQuery,
    config = mockConfig
  )

  test("CustomSearcher should return all documents, but only rank matching docs.") {

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


}
