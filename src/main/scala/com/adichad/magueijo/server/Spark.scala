package com.adichad.magueijo.server

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConversions._

/**
  * Created by adichad on 14/09/16.
  */
class Spark(val scope: String) extends Server {
  private var sparkContext: SparkContext = _
  private var sqlContext: SQLContext = _

  override def bind(): Unit = {
    sparkContext =
      SparkContext.getOrCreate(
        new SparkConf()
          .setAll(props(""))
      )

    sqlContext = new SQLContext(sparkContext)
    info("spark contexts bound")
  }

  override def close(): Unit = {
    sparkContext.stop()
    info("spark context stopped")
  }
}
