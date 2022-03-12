# spark-using-scalapb

## Reference
[https://scalapb.github.io/docs/sparksql/](https://scalapb.github.io/docs/sparksql/)

## Usage

1. Build

```shell
$ sbt assembly

```

2. Submit the job

```shell
$ $SPARK_HOME/bin/spark-submit \
--jars . \
--class myexample.RunDemo \
target/scala-2.12/spark-using-scalapb-assembly-0.1.0-SNAPSHOT.jar

```
