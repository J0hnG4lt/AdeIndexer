package AdeIndexer

package object config {

  case class AdeIndexerConfig(
                             loggerName: String = "AdeIndexer",
                             directory: String = System.getProperty("user.dir"),
                             indexDirectory: String = System.getProperty("user.dir")+"index/"
                             )

}
