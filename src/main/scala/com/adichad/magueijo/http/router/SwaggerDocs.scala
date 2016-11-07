/*
 * Copyright 2016 Aditya Varun Chadha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adichad.magueijo.http.router

import akka.http.scaladsl.model.StatusCodes._
import com.adichad.magueijo.http.router.hello.Hello
import com.adichad.magueijo.resource.AkkaContext
import com.github.swagger.akka.model.Info
import com.github.swagger.akka.{HasActorSystem, SwaggerHttpService}

import scala.reflect.runtime.{universe => ru}

/**
  * Created by adichad on 05/07/16.
  */
case class SwaggerDocs(scope: String)
                  extends Router with SwaggerHttpService with HasActorSystem {

  val akka = registered[AkkaContext]("akka")
  implicit val actorSystem = akka.actorSystem
  implicit val materializer = akka.materializer
  implicit val executionContext = akka.executionContext

  override val apiTypes = Seq(ru.typeOf[Hello])
  override val info = Info(version = "1.0")
  override def route =
    routes ~
      pathPrefix("swagger") {
        getFromResourceDirectory("swagger") ~
          pathEndOrSingleSlash {
            get(redirect("/swagger/index.html?url=/api-docs/swagger.json", PermanentRedirect))
          }
      } ~
      pathEndOrSingleSlash {
        getFromResourceDirectory("swagger") ~
          get(redirect("/swagger/index.html?url=/api-docs/swagger.json", PermanentRedirect))
      }
}