# AdeIndexer

```bash
java -Djava.util.logging.config.file=src/main/resources/logging.properties \
  -jar ./target/adeindexer-0.0.1-SNAPSHOT.jar \
  -d src/test/resources/ \
  -i src/test/resources/index/ \
  -q ".*Alba.*"
```