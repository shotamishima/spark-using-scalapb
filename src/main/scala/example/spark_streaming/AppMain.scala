package example.spark_streaming

import org.apache.spark._
import org.apache.spark.streaming._

object AppMain {
    // socket stream
    def main(Args: Array[String]): Unit = {
        
        val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")

        // 1秒周期でマイクロバッチを実行する
        val ssc = new StreamingContext(conf, Seconds(5))
        
        // create DStream
        val lines = ssc.socketTextStream("localhost", 9999)

        val words = lines.flatMap(_.split(" "))

        val pairs = words.map(word => (word, 1))
        val wordCounts = pairs.reduceByKey(_ + _)

        wordCounts.print()

        ssc.start()
        ssc.awaitTermination() // 停止時間を設定可能 millisec

    }
}