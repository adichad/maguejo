package com.adichad.magueijo.resource

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConversions._

/**
  * Created by adichad on 14/09/16.
  */
class Spark(val scope: String) extends ManagedResource {
  val sparkContext: SparkContext = SparkContext.getOrCreate(
    new SparkConf()
      .setAll(props(""))
  )
  val sqlContext: SQLContext = new SQLContext(sparkContext)
  info("spark contexts bound")

  override def close(): Unit = {
    sparkContext.stop()
    info("spark context stopped")
  }
}
