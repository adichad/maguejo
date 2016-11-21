package com.adichad.magueijo.es.plugin.analysis.tokenfilter.recombining

import org.apache.lucene.analysis.TokenStream
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.index.IndexSettings
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory

/**
  * Created by achadha on 15/11/2016.
  */
class RecombiningTokenFilterFactory (indexSettings: IndexSettings, name: String, settings: Settings)
  extends AbstractTokenFilterFactory(indexSettings, name, settings) {
  private val separator = settings.get("separator", " ")

  override def create(tokenStream: TokenStream): TokenStream = {
    new RecombiningTokenFilter(tokenStream, separator)
  }
}
