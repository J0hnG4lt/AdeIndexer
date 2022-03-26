package AdeIndexer.indexer.custom

import AdeIndexer.indexer.custom.CustomIndexer
import org.scalatest.Assertions.assert
import org.scalatest.funsuite.AnyFunSuite

import java.nio.file.Paths

class CustomIndexerSuite extends AnyFunSuite {
  val folder = Paths.get("src", "test", "resources")
  val testTokens = Map("Georvic"->1, "Tur"->2, "Victoria"->1, "Jorge"->1, "Orlene"->1)

  test("CustomIndexer should find all text documents") {

    val indexer = CustomIndexer(folder=folder)
    val filePaths = indexer.getFilePaths()
    assert(filePaths.size == 3, "Unexpected number of documents.")
  }

  testTokens.foreach {
    case (testToken, appearances) => {

      test(s"The indexer should index $testToken in $appearances documents.") {
        val indexer = CustomIndexer(folder = folder)
        val documents = indexer.loadDocuments()

        assert(documents.size > 0, "We expected at least one document.")

        val invertedIndex = indexer.loadIndex()

        assert(invertedIndex.size == appearances, "We expected at least one indexed word.")

        val testDocuments = invertedIndex.apply(testToken)
        testDocuments.foreach(
          documentId => {
            val documentPath = documents.apply(documentId)
            val documentFilepath = documentPath.toString
            val documentFile = documentPath.toFile
            val contents = scala.io.Source.fromFile(documentFile).mkString
            assert(
              contents.contains(testToken),
              s"The document $documentFilepath does not contain the test token $testToken. File Contents: $contents"
            )

          }
        )
      }

    }
  }


}
