package com.adichad.magueijo.es.plugin.analysis.tokenfilter.recombining

import org.elasticsearch.common.settings.Settings
import org.elasticsearch.env.Environment
import org.elasticsearch.index.IndexSettings
import org.elasticsearch.index.analysis.TokenFilterFactory
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider
import org.elasticsearch.plugins.{AnalysisPlugin, Plugin}
import scala.collection.JavaConversions._

/**
  * Created by achadha on 09/11/2016.
  */
class RecombiningTokenFilterPlugin extends Plugin with AnalysisPlugin {
  val map = Map("recombining" -> new AnalysisProvider[TokenFilterFactory] {
    override def get(indexSettings: IndexSettings, environment: Environment, name: String, settings: Settings) =
      new RecombiningTokenFilterFactory(indexSettings, name, settings)
  })

  override def getTokenFilters = map
}
