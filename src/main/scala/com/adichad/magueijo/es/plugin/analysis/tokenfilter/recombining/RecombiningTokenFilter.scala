package com.adichad.magueijo.es.plugin.analysis.tokenfilter.recombining

import java.io.IOException

import org.apache.lucene.analysis.{TokenFilter, TokenStream}
import org.apache.lucene.analysis.tokenattributes.{CharTermAttribute, PositionIncrementAttribute}

/**
  * Created by achadha on 15/11/2016.
  */
class RecombiningTokenFilter(input: TokenStream, separator: String) extends TokenFilter(input) {

  val termAttr = addAttribute(classOf[CharTermAttribute])
  val posIncrAttr = addAttribute(classOf[PositionIncrementAttribute])
  val sb = new StringBuilder

  @throws[IOException]
  override def reset() {
    super.reset()
    sb.setLength(0)
  }

  @throws[IOException]
  override def incrementToken: Boolean = {
    if (input.incrementToken) {
      sb.append(termAttr.buffer, 0, termAttr.length)
      while (input.incrementToken) this.sb.append(separator).append(termAttr.buffer, 0, termAttr.length)
      termAttr.setEmpty().append(sb)
      posIncrAttr.setPositionIncrement(1)
      true
    } else {
      input.end()
      false
    }
  }
}
