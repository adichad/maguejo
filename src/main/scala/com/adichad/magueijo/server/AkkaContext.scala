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

package com.adichad.magueijo.server

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by adichad on 12/08/16.
  */
class AkkaContext(val scope: String) extends Server {
  implicit var actorSystem: ActorSystem = _
  implicit var materializer: ActorMaterializer = _
  implicit var executionContext: ExecutionContextExecutor = _
  var noop: ActorRef = _

  override def bind(): Unit = {
    actorSystem = ActorSystem(string("name"), conf("akka"))
    materializer = ActorMaterializer()
    executionContext = actorSystem.dispatcher
    noop = actorSystem.actorOf(Props.empty)
    info("akka bound")
  }

  override def close(): Unit = {
    materializer.shutdown()
    actorSystem.terminate()
  }
}
