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

import akka.http.scaladsl.model.RemoteAddress

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration

/**
  * Created by adichad on 03/07/16.
  */

class Common(val scope: String) extends Router {

  info(s"router ${string("name")} instantiated")
  override def route =

    corsHandler {
      withRequestTimeout(Duration(string("default-timeout"))) {
        withPrecompressedMediaTypeSupport {
          validateIP {
            encodeResponse {
              configureds[Router]("routes").values().map(_.route).reduceLeft(_ ~ _)
            }
          }(f = (ip: RemoteAddress)=>true)
        }
      }
    }

}
