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

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ReceiveTimeout}
import akka.http.scaladsl.model.StatusCodes._
import com.adichad.magueijo.conf.Configured
import com.adichad.magueijo.http.directives.{ExtendedDirectives, ImperativeRequestContext}
import com.adichad.magueijo.protocol.JsonSupport
import com.adichad.magueijo.protocol.response.ElasticsearchStats
import com.adichad.magueijo.server.{Elasticsearch, HyperGraphDB}
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse
import org.json4s.jackson.JsonMethods._

import scala.concurrent.duration._

/**
  * Created by adichad on 14/09/16.
  */
class HelloHandler(val scope: String) extends Actor with Configured with ExtendedDirectives with JsonSupport with AutoCloseable {
  context.setReceiveTimeout(1.seconds)
  private val startTime = System.currentTimeMillis()

  private val es = registered[Elasticsearch]("elasticsearch").client
  private var ictx: ImperativeRequestContext = _
  val hgdb = registered[HyperGraphDB]("hgdb")
  val db = hgdb.db



  override def close(): Unit = {
    info("stopping HelloHandler: "+this)
    context.stop(self)
  }

  override def receive: Receive = {

    case HelloRequest(ctx) =>
      try {
        info("received request")
        this.ictx = ctx

        es.admin().cluster().prepareNodesStats().execute(
          new ActionListener[NodesStatsResponse] {
            override def onFailure(e: Throwable): Unit = {
              try {
                ctx.fail(e)
              } finally {
                close()
              }
            }

            override def onResponse(response: NodesStatsResponse): Unit = {
              try {
                ctx.complete(OK, ElasticsearchStats(parse(response.toString)))
              } finally {
                close()
              }
            }
          }
        )
      } catch {
        case e: Throwable =>
          try {
            ctx.fail(e)
          } finally {
            close()
          }
      }

    case ReceiveTimeout =>
      try {
        ictx.complete(GatewayTimeout, (System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS).toString())
        error("received timeout!")
      } finally {
        close()
      }
  }
}