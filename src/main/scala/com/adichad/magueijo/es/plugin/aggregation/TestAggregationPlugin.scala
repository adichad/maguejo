package com.adichad.magueijo.es.plugin.aggregation

import java.util

import org.elasticsearch.plugins.SearchPlugin.AggregationSpec
import org.elasticsearch.plugins.{Plugin, SearchPlugin}
import scala.collection.JavaConversions._
/**
  * Created by achadha on 17/11/2016.
  */
class TestAggregationPlugin extends Plugin with SearchPlugin {
  val aggregations = List[AggregationSpec](
    new AggregationSpec("test_aggregation", null, null)

  )

  override def getAggregations: util.List[AggregationSpec] = aggregations

}
