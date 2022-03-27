package AdeIndexer.indexer.lucene

import AdeIndexer.indexer.lucene.CountSimilarity
import org.apache.lucene.search.similarities.BasicStats
import org.scalatest.Assertions.assert
import org.scalatest.funsuite.AnyFunSuite

class CountSimilaritySuite extends AnyFunSuite {
  test("Only use the frequency term") {

    val expected = 1.toDouble
    val basicStatsMock = new BasicStats("", 1.toDouble)//mock(classOf[BasicStats])
    basicStatsMock.setDocFreq(expected.toLong)
    basicStatsMock.setTotalTermFreq(expected.toLong)
    val similarity = new CountSimilarity()
    val obtained = similarity.score(basicStatsMock, expected, 1)
    assert(expected == obtained, "We currently want to use a simple count of occurrences.")
  }
}
