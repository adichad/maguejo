package com.adichad.magueijo.es.plugin.analysis.tokenfilter.stem

import com.adichad.maguejo.es.plugin.analysis.tokenfilter.stem._
import org.apache.lucene.analysis.TokenStream
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.index.IndexSettings
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory

import scala.collection.JavaConversions._

/**
  * Created by achadha on 17/11/2016.
  */
class StemmingTokenFilterFactory (indexSettings: IndexSettings, name: String, settings: Settings)
  extends AbstractTokenFilterFactory(indexSettings, name, settings) {
  private val stemmerType = settings.get("stemmer")
  private val minLen = settings.getAsInt("min-len", 3).toInt
  private val blackList = new java.util.HashSet[String](settings.getAsArray("exclude", new Array[String](0)).toSeq)
  private val stemmerFactory: () => Stemmer = stemmerType match {
    case "plural" => () => new PluralStemmer(blackList, minLen)
    case "participle" => () => new ParticipleStemmer(blackList, minLen)
    case x => throw new Exception(s"unknown stemmer type: $x in definition: $name on index: ${indexSettings.getIndexMetaData.getIndex.getName}")
  }

  private val bitPos = settings.getAsInt("mark-bit", 0).toInt
  private val markStems = settings.getAsBoolean("mark-stems", false).booleanValue()
  private val augment = settings.getAsBoolean("augment", false).booleanValue()

  private val tokenFilterFactory: TokenStream => TokenStream =
    if(augment)
      new AugmentingStemmingTokenFilter(_, markStems, bitPos, stemmerFactory())
    else
      new ReplacingStemmingTokenFilter(_, markStems, bitPos, stemmerFactory())

  override def create(tokenStream: TokenStream) =
    tokenFilterFactory(tokenStream)
}
