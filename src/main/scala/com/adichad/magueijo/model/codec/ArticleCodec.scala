package com.adichad.magueijo.model.codec

import com.adichad.magueijo.model.Article
import io.circe._
/**
  * Created by achadha on 07/11/2016.
  */
object ArticleCodec {
  implicit val decoder = Decoder.instance[Article] { cursor =>
    cursor.downField("name").as[String].map(Article)
  }

}
