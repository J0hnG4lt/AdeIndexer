package AdeIndexer.indexer

import org.scalatest.funsuite.AnyFunSuite
import AdeIndexer.indexer.CountSimilarity
import org.apache.lucene.search.similarities.BasicStats

import org.scalatest.Assertions.assert

class CountSimilaritySuite extends AnyFunSuite {
  test("Only use the frequency term") {

    val expected = 43.toDouble
    val basicStatsMock = new BasicStats("", 1.toDouble)//mock(classOf[BasicStats])
    val similarity = new CountSimilarity()
    val obtained = similarity.score(basicStatsMock, expected, 1)
    assert(expected == obtained, "We currently want to use a simple count of occurrences.")
  }
}
