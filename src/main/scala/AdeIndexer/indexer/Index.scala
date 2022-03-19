package AdeIndexer.indexer

import org.apache.lucene.analysis.standard.{StandardAnalyzer, StandardTokenizerFactory}
import org.apache.lucene.analysis.custom.CustomAnalyzer
import org.apache.lucene.document.{Document, Field, FieldType, StringField, TextField}
import org.apache.lucene.index.{DirectoryReader, IndexOptions, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.store.{Directory, FSDirectory}
import org.apache.lucene.search.{BooleanClause, BooleanQuery, IndexSearcher, MatchAllDocsQuery, MultiPhraseQuery, PhraseQuery, Query, RegexpQuery, TermQuery, TopDocs, WildcardQuery}
import org.apache.lucene.queryparser.classic.QueryParser

import java.nio.file.{Path, Paths}
import java.io.{BufferedReader, File, FileReader}
import util.control.Breaks.*
import AdeIndexer.config.Indexer.AdeIndexerConfig
import AdeIndexer.indexer.CountSimilarity

import java.util.logging.Logger

object Index {

  val logger = Logger.getLogger(this.getClass.getName)

  def buildDirectory(directoryUri: String): FSDirectory = {
    val directory = FSDirectory.open(Path.of(directoryUri))
    directory
  }

  def addFilesToIndex(config: AdeIndexerConfig): Unit = {

    val indexDirectory = buildDirectory(directoryUri=config.indexDirectory)
    val fileDirectory = buildDirectory(directoryUri=config.directory)

    //val analyzer = new StandardAnalyzer()
    val analyzer = CustomAnalyzer.builder().withTokenizer("standard").build()

    val indexWriterConfig = new IndexWriterConfig(analyzer)
    indexWriterConfig.setSimilarity(CountSimilarity())
    indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
    val indexWriter = new IndexWriter(indexDirectory, indexWriterConfig)
    indexWriter.deleteAll()

    val files = fileDirectory.listAll()
    files.foreach(
      filename => {
        breakable {

          if (!filename.endsWith(".txt")){
            logger.fine(s"Skipping: ${filename}")
            break
          }

          val path = fileDirectory.getDirectory.resolve(filename).toAbsolutePath
          val file = path.toFile

          logger.info(s"Filename: ${path.toAbsolutePath}")

//          val fileReader = new FileReader(file)
//          val bufferedReader = new BufferedReader(fileReader)
          val lines = scala.io.Source.fromFile(file).mkString
          logger.info(lines)
          val document = new Document()
          //document.add(new TextField("contents", fileReader))
//          val fieldType = new FieldType()
//          fieldType.setStoreTermVectors(true)
//          fieldType.setStoreTermVectorPositions(true)
//          fieldType.setStoreTermVectorOffsets(true)
//          fieldType.setIndexOptions(IndexOptions.DOCS)

          //document.add(new Field("contents", lines, fieldType))
          document.add(new TextField("contents", lines, Field.Store.NO))

          document.add(new StringField("path", file.getPath, Field.Store.YES))
          document.add(new StringField("filename", file.getName, Field.Store.YES))

          indexWriter.addDocument(document)
          indexWriter.flush()
          indexWriter.commit()

        }

      }
    )

    indexWriter.close()
    fileDirectory.close()
    indexDirectory.close()
    logger.info(s"IndexWriter closed.")
  }

  def searchIndexByBoolean(query: String, config: AdeIndexerConfig):Map[String, Float] = {
    val indexDirectory = buildDirectory(directoryUri=config.indexDirectory)
    logger.info(DirectoryReader.listCommits(indexDirectory).toArray().mkString("\n"))
    val indexReader = DirectoryReader.open(indexDirectory)
    val searcher = new IndexSearcher(indexReader)
    searcher.setSimilarity(CountSimilarity())
    val booleanQueryBuilder = new BooleanQuery.Builder
    val words = query.split(" ")
    words.foreach( word => {
      logger.fine(s"Word: ${word}")
      booleanQueryBuilder.add(new TermQuery(new Term("contents", word)), BooleanClause.Occur.SHOULD)
    }
    )
    val booleanQuery = booleanQueryBuilder.build()

    val totalIndexedDocs = indexReader.numDocs()
    logger.info("numDocs: "+totalIndexedDocs)
    val results = searcher.search(booleanQuery, totalIndexedDocs)
    val scoredDocs = results.scoreDocs.map(
      scoreDoc => {
        val idoc = scoreDoc.doc
        val doc = searcher.doc(idoc)
        (doc.getField("path").stringValue, scoreDoc.score)
      }
    ).toMap
    logger.info("totalHits: " + results.totalHits.toString)
    scoredDocs
  }

  def searchIndexAll(query: String, config: AdeIndexerConfig):Map[String, Float]  = {
    val indexDirectory = buildDirectory(directoryUri=config.indexDirectory)
    logger.info(DirectoryReader.listCommits(indexDirectory).toArray().mkString("\n"))
    val indexReader = DirectoryReader.open(indexDirectory)
    val searcher = new IndexSearcher(indexReader)
    val matchAllDocsQuery = new MatchAllDocsQuery()
    val results = searcher.search(matchAllDocsQuery, 10)
    logger.info("numDocs: "+indexReader.numDocs())
    val scoredDocs = results.scoreDocs.map(
      scoreDoc => {
        val idoc = scoreDoc.doc
        val doc = searcher.doc(idoc)
        (doc.getField("path").stringValue, 0.toFloat)
      }
    ).toMap
    scoredDocs
  }

  def searchIndexAndScoreAll(query: String, config: AdeIndexerConfig): Map[String, Float]  = {
    val allDocs = searchIndexAll(query=query, config = config)
    val scoredDocs = searchIndexByBoolean(query=query, config = config)
    val mergedDocs = allDocs.map(
      docAll => {
        (docAll._1, scoredDocs.getOrElse(docAll._1, docAll._2))
      }
    )
    mergedDocs
  }

}
