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

package org.elasticsearch.node

import java.util

import com.adichad.magueijo.conf.Configured
import org.elasticsearch.Version
import org.elasticsearch.common.cli.Terminal
import org.elasticsearch.node.internal.InternalSettingsPreparer
import org.elasticsearch.plugins.Plugin

import scala.collection.JavaConversions._

/**
  * Created by adichad on 18/08/16.
  */
object CustomNodeBuilder extends Configured {
  override val scope = "es"
  implicit class NodeBuilderPimp(val nodeBuilder: NodeBuilder) {
    def buildCustom = {
      val settings = nodeBuilder.settings.build()
      val plugins: util.Collection[Class[_ <: Plugin]] =
        settings.getAsArray("plugin.types").map(t=>Class.forName(t).asInstanceOf[Class[Plugin]]).toSeq
      new Node(InternalSettingsPreparer.prepareEnvironment(settings, Terminal.DEFAULT), Version.CURRENT, plugins)
    }

    def nodeCustom = {
      buildCustom.start()
    }
  }

}
