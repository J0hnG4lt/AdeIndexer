# AdeIndexer

A command line tool that builds an inverted index on a folder with .txt files and allows for the execution of 
efficient searches on it. By default, an index directory will be created on the current directory if no `-i` option 
is specified. This solution builds the inverted index on the file system rather than having it in memory.

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
│           ├── config: here we define all the config used by the package.
│           │   ├── ArgParser.scala
│           │   └── Indexer.scala
│           ├── exceptions
│           │   └── CustomExceptions.scala
│           ├── indexer: all the code related to Lucene here.
│           │   ├── CountSimilarity.scala: a custom similarity measure.
│           │   └── Index.scala: all the code related to the inverted index and searches.
│           ├── logging
│           │   └── LoggerUtils.scala
│           ├── Main.scala: entrypoint for the JAR
│           ├── postprocessing
│           │   └── Scaler.scala: a utility function for rescaling scores to 0 <= x <= 100
│           └── repl
│               └── IndexingRepl.scala
└── test
    ├── resources
    │   ├── names2.txt
    │   ├── names3.txt
    │   └── names.txt
    └── scala
        └── AdeIndexer
            └── indexer
                ├── CountSimilaritySuite.scala
                └── IndexSuite.scala

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
java -Djava.util.logging.config.file=src/main/resources/logging.properties -jar ./target/adeindexer-0.0.1-SNAPSHOT.jar --help
```

The following should appear:

```commandline
AdeIndexer 0.0.1
Usage: AdeIndexer [options]

  -d, --directory <value>  d is the path to a directory with files that will be indexed.
  -i, --index-directory <value>
                           i is the path to a directory where the index will be stored
  -q, --query <value>      q is the query
  --help                   prints this usage text
```

### Build an inverted index and execute a single search:

```bash
java -Djava.util.logging.config.file=src/main/resources/logging.properties \
  -jar ./target/adeindexer-0.0.1-SNAPSHOT.jar \
  -d src/test/resources/ \
  -i ./index/ \
  -q "Georvic Victoria"
```

Something like the following should appear:

```commandline
Map(/home/georvic/repos/infra/AdeIndexer/src/test/resources/names.txt -> 100.0, /home/georvic/repos/infra/AdeIndexer/src/test/resources/names2.txt -> 33.333336, /home/georvic/repos/infra/AdeIndexer/src/test/resources/names3.txt -> 0.0)
```

### Build an inverted index and wait for user input:

```bash
java -Djava.util.logging.config.file=src/main/resources/logging.properties \
  -jar ./target/adeindexer-0.0.1-SNAPSHOT.jar \
  -d src/test/resources/ \
  -i ./index/
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
