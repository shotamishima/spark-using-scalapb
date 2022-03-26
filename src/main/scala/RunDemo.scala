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

import java.io.{FileInputStream, FileOutputStream}

object RunDemo {

  def main(Args: Array[String]): Unit = {

    // Stop log INFO
    Logger.getLogger("org").setLevel(Level.OFF)
  
    def initSpark(): SparkSession = {
      SparkSession.builder()
        .appName("ScalaPB Demo")
        .master("local[2]")
        .getOrCreate()
    }
    
    // declare message contents
    val newPerson = for (i <- 1 to 5) yield {
      Person(
        name = Some("Joe"),
        age = Some(30 + i),
        gender = if (i%2==0) Some(Gender.MALE) else Some(Gender.FEMALE),
        addresses = Vector(
          Address(city = Some("San Francisco"), street = Some(s"No.$i"))
        )
      )
    }
    
    /**
     * decode protocol buffers binary file and create dataframe
     */
    def readFromFile(path: String): Unit = {
      
      val spark = initSpark()

      val fileInputStream = new FileInputStream(path)
      
      try {
        val personList = PersonList.parseFrom(fileInputStream)
        println(personList.toProtoString)
        
        val peopleDF: DataFrame = ProtoSQL.createDataFrame(spark, Seq(personList))
        peopleDF.printSchema()
        peopleDF.show()
        
      } finally {
        fileInputStream.close()
      }

    }
    
    def addPerson(path: String): Unit = {
      
      val spark = initSpark()

      val personsDF: DataFrame = ProtoSQL.createDataFrame(spark, newPerson)
      val personsDS: Dataset[Person] = personsDF.as[Person]
      
      personsDS.show(truncate=false)
      personsDS.foreach { person =>
        val file = new FileOutputStream(path + person.age.getOrElse(0).toString + "_tmp_person.pb")
        try {
          person.writeTo(file)
        } finally {
          file.close()
        }
      }
    }

    def addPeople(path: String): Unit = {
      
      val spark = initSpark()
      
      // create dataframe from vector
      val newPeople = for (i <- 1 to 2) yield {
        PersonList(
          tags = Some(s"$i"),
          people = newPerson 
        )
      }

      val peopleDF: DataFrame = ProtoSQL.createDataFrame(spark, newPeople)
      val peopleDS: Dataset[PersonList] = peopleDF.as[PersonList]
      
      println("encoding...")
      // encode and output to stream
      peopleDS.foreach { row =>
        val file = new FileOutputStream(path + row.tags.getOrElse(0).toString + "_tmp_people.pb")
        try {
          row.writeTo(file)
        } finally {
          file.close()
        }
      }
    }

    // addPeople("data/")
    // addPerson("data/")
    readFromFile("data/1_tmp_people.pb")
  
  }
}
