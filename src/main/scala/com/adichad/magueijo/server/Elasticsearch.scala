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

import org.elasticsearch.client.Client
import org.elasticsearch.common.cli.Terminal
import org.elasticsearch.env.Environment
import org.elasticsearch.node.CustomNodeBuilder._
import org.elasticsearch.node.Node
import org.elasticsearch.node.NodeBuilder._
import org.elasticsearch.node.internal.InternalSettingsPreparer


/**
  * Created by adichad on 17/08/16.
  */
class Elasticsearch(val scope: String) extends Server {
  var esNode: Node = _
  var client: Client = _

  def close(): Unit = {
    client.close()
    esNode.close()
  }

  override def bind(): Unit = {
    esNode = nodeBuilder
      .clusterName(string("cluster.name"))
      .local(false)
      .data(boolean("node.data"))
      .settings(settings("")).nodeCustom

    client = esNode.client
  }
}
