package myexample

import com.example.protos.demo._

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.{functions => F}
import org.apache.spark.rdd.RDD
import scalapb.spark.Implicits._
import scalapb.spark.ProtoSQL

import org.apache.log4j.Logger
import org.apache.log4j.Level

import java.io.FileOutputStream

object RunDemo {

  def main(Args: Array[String]): Unit = {

    // INFOのログ出力を止める
    Logger.getLogger("org").setLevel(Level.OFF)
  
    val spark = SparkSession.builder()
      .appName("ScalaPB Demo")
      .master("local[2]")
      .getOrCreate()

    val sc = spark.sparkContext

    // protocol buffersで生成されたcase classに基づいてデータフレームを生成する
    val personsDF: DataFrame = ProtoSQL.createDataFrame(spark, testData)

    // data frame
    println("data frame of person message --------------------------")
    personsDF.printSchema()
    personsDF.show()
    
    personsDF.createOrReplaceTempView("persons")
    spark.sql("SELECT * FROM persons WHERE age > 32").show()
    // data set 
    println("data set of person message --------------------------")
    val personsDS1: Dataset[Person] = personsDF.as[Person]
    // personsDS1.collect().foreach(println)

    personsDS1.show()
    
    // people message
    val peopleDF: DataFrame = ProtoSQL.createDataFrame(spark, Vector(PersonList(testData)))
    println("data frame of people message --------------------------")
    peopleDF.printSchema()
    peopleDF.show()
    
    // from binary to protos and back
    // Seq(Person)の１要素ずつmapでバイナリに変換していく。toByteArrayはPersonのメソッド
    println("from binary to protos and back --------------------------")
    val binaryDS: Dataset[Array[Byte]] = spark.createDataset(testData.map(_.toByteArray))
    binaryDS.show()
    // binaryDSの行ごとにrow -> parseFrom(row)
    val protosDS: Dataset[Person] = binaryDS.map(Person.parseFrom(_))
    protosDS.show()

    def encode(person: Person): Unit = {
      val file = new FileOutputStream("data/tmp.pb")
      try {
        person.writeTo(file)
      } finally {
        file.close()
      }
    }
    
    protosDS.map(encode(_))

    // udf
    println("UDFs ---------------------------------------------------")
    val parsePersons = ProtoSQL.udf { bytes: Array[Byte] => Person.parseFrom(bytes)}
    // stringtocolumnの使用でエラー
    // binaryDS.select(col("person")).withColumn("person", parsePersons(col("value"))).show()
    
  }

  val testData = for(i <- 1 to 5) yield {
    Person(
      name = Some("Joe"),
      age = Some(30 + i),
      gender = if (i%2 == 0) Some(Gender.MALE) else Some(Gender.FEMALE),
      addresses = Seq(
        Address(city = Some("San Francisco"), street = Some(s"No.$i"))
      )
    )
  }
}
