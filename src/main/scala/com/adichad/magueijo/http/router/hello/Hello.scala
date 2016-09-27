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

package com.adichad.magueijo.http.router.hello

import javax.ws.rs.Path

import akka.http.scaladsl.server._
import com.adichad.magueijo.http.router.Router
import com.adichad.magueijo.server.AkkaContext
import io.swagger.annotations.{Api, ApiOperation}

/**
  * Created by adichad on 05/07/16.
  */
@Path("/es/stats")
@Api(value = "/es/stats", produces = "application/json")
case class Hello(scope: String) extends Router {
  val akka = registered[AkkaContext]("akka")
  implicit val system = akka.actorSystem
  implicit val materializer = akka.materializer
  implicit val executionContext = akka.executionContext

  @ApiOperation(value = "Elasticsearch Cluster Stats", nickname = "esstats", httpMethod = "GET")
  override def route: Route =
    path("es"/"stats") {
      get {
        extractRequestContext { reqCtx =>
          extractRequest { req =>
            parameters('scan_duration_ms ? 1000) { (name) =>
              imperativelyComplete { ctx =>
                actor[HelloHandler]("") ! HelloRequest(ctx)
              }
            }
          }
        }
      }
    }
}
