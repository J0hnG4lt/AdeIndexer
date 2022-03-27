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

  test("CustomSearcher should return all documents, but only rank matching docs.") {

    val mockQuery = "Georvic"
    val customSearcher = CustomSearcher()
    val scoredDocs = customSearcher.searchIndexAndScoreAll(
      query=mockQuery,
      config = mockConfig
    )

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

  test("CustomSearcher should score with 0 all documents for a word that is not in any of them.") {

    val mockQuery = "Aldo"
    val customSearcher = CustomSearcher()
    val scoredDocs = customSearcher.searchIndexAndScoreAll(
      query=mockQuery,
      config = mockConfig
    )

    scoredDocs.foreach(
      docAndScore => {
        assert(
          docAndScore._2 == 0.toFloat,
          s"We expeted that no document would score 0 for $mockQuery"
        )
      }
    )
  }

  test("CustomSearcher should score with 50 all documents when only one of two words are available in any of them.") {

    val mockQuery = "Tur Orlene"
    val customSearcher = CustomSearcher()
    val scoredDocs = customSearcher.searchIndexAndScoreAll(
      query=mockQuery,
      config = mockConfig
    )

    scoredDocs.foreach(
      docAndScore => {
        assert(
          docAndScore._2 == 50.toFloat,
          s"We expeted that no document would score 0 for $mockQuery"
        )
      }
    )
  }

  test("CustomSearcher should not be affected by repeated words in the query.") {

    val mockQuery = "Tur Tur Tur Tur"
    val customSearcher = CustomSearcher()
    val scoredDocs = customSearcher.searchIndexAndScoreAll(
      query=mockQuery,
      config = mockConfig
    )

    scoredDocs.foreach(
      docAndScore => {
        docAndScore._1 match {
          case filepath if filepath.endsWith("names.txt") => assert(
            docAndScore._2 == 100.toFloat,
            "We expeted that the document containing the query would have scored 100"
          )
          case filepath if filepath.endsWith("names2.txt") => assert(
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
