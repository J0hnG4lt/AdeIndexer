package AdeIndexer.exceptions

object CustomExceptions {

  class AdeIndexerException(message: String) extends Exception(message)
  class WrongArgumentsException(val message: String) extends AdeIndexerException(message)

}
