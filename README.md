# AdeIndexer

A command line tool that builds an inverted index on a folder with .txt files and allows for the execution of
efficient searches on it. 

## Implementations

### Custom in-memory solution (this indexer is used by default)

A custom in-memory index was built with two Scala Collections:

- A mutable indexed sequence whose values are file paths and whose indices represent document IDs. This collection was chosen for its constant-time lookup.
- A mutable Hash Map for efficient lookups and updates. Its keys represent words and its values represent Hash Sets of document Ids. Document IDs are stored instead of Paths to reduce memory requirements.

### With Lucene

By default, an index directory will be created on the current directory if no `-i` option
is specified. This solution builds the inverted index on the file system rather than having it in memory. To use this
indexer, use the following option: `-n Lucene`.

## Layout and conventions

```text
src/
├── main
│   ├── resources
│   │   └── logging.properties
│   └── scala
│       └── AdeIndexer
│           ├── cli
│           │   └── ArgParser.scala
│           ├── config
│           │   ├── ArgParser.scala
│           │   └── Indexer.scala
│           ├── exceptions
│           │   └── CustomExceptions.scala
│           ├── indexer
│           │   ├── custom
│           │   │   ├── CustomIndexer.scala
│           │   │   └── CustomSearcher.scala
│           │   ├── lucene
│           │   │   ├── CountSimilarity.scala
│           │   │   ├── LuceneIndexer.scala
│           │   │   └── LuceneSearcher.scala
│           │   ├── SearcherBase.scala
│           │   └── SearcherFactory.scala
│           ├── logging
│           │   └── LoggerUtils.scala
│           ├── Main.scala
│           ├── postprocessing
│           │   └── Scaler.scala
│           └── repl
│               └── IndexingRepl.scala
└── test
    ├── resources
    │   ├── names2.txt
    │   ├── names3.txt
    │   ├── names.txt
    │   └── something_else.yaml
    └── scala
        └── AdeIndexer
            └── indexer
                ├── custom
                │   ├── CustomIndexerSuite.scala
                │   └── CustomSearcherSuite.scala
                └── lucene
                    ├── CountSimilaritySuite.scala
                    ├── LuceneIndexSuite.scala
                    └── LuceneSearcherSuite.scala


```

## Requirements

- openjdk version "17"
- Maven 3.8.1
- Developed with IntelliJ

## Usage

### Logging

Use the parameter `-Djava.util.logging.config.file=src/main/resources/logging.properties` with java to use a FINE logging level.

### Get help

```bash
java -Djava.util.logging.config.file=src/main/resources/logging.properties -jar ./target/adeindexer-0.0.2-SNAPSHOT.jar --help
```

The following should appear:

```commandline
AdeIndexer 0.0.2
Usage: AdeIndexer [options]

  -d, --directory <value>  d is the path to a directory with files that will be indexed.
  -i, --index-directory <value>
                           i is the path to a directory where the index will be stored
  -n, --name-indexer <value>
                           n is the name of the indexer that will be used. Options: Lucene, Custom
  -q, --query <value>      q is the query
  --help                   prints this usage text

```

### Build an inverted index and execute a single search:

```bash
java -Djava.util.logging.config.file=src/main/resources/logging.properties \
  -jar ./target/adeindexer-0.0.2-SNAPSHOT.jar \
  -d src/test/resources/ \
  -q "Georvic Victoria"
```

Something like the following should appear:

```commandline
Map(/home/georvic/repos/infra/AdeIndexer/src/test/resources/names.txt -> 100.0, /home/georvic/repos/infra/AdeIndexer/src/test/resources/names2.txt -> 33.333336, /home/georvic/repos/infra/AdeIndexer/src/test/resources/names3.txt -> 0.0)
```

### Build an inverted index and wait for user input:

```bash
java -Djava.util.logging.config.file=src/main/resources/logging.properties \
  -jar ./target/adeindexer-0.0.2-SNAPSHOT.jar \
  -d src/test/resources/
```

Something like the following should appear:

```commandline
 Hello, welcome to the AdeIndexer!


 To search for ocurrences of any set of words, just enter them separated by a space.
 For Example:
 search> Deutschland Frankreich
 You can use the following commands: :quit, :help

search> 
```

Introduce your queries like this and press enter:

```commandline
search> Georvic Victoria
```

Something like the following should appear:

```commandline
Map(/home/georvic/repos/infra/AdeIndexer/src/test/resources/names.txt -> 100.0, /home/georvic/repos/infra/AdeIndexer/src/test/resources/names2.txt -> 33.333336, /home/georvic/repos/infra/AdeIndexer/src/test/resources/names3.txt -> 0.0)
```

## References

- Shaded Jars: https://cloud.google.com/dataproc/docs/guides/manage-spark-dependencies?hl=en
- Lucene: 
  - https://lucene.apache.org/core/8_11_1/core/
  - https://www.goodreads.com/book/show/55042721-practical-apache-lucene-8
- Utility function from here to print/log case classes: https://gist.github.com/carymrobbins/7b8ed52cd6ea186dbdf8
- I did some research first on similar projects: https://github.com/PointerFLY/Lucene-Example/tree/master/src/main/java
