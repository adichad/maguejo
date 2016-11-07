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

package com.adichad.magueijo.resource

import org.hypergraphdb.storage.HGStoreImplementation
import org.hypergraphdb._

/**
  * Created by adichad on 15/08/16.
  */
class HyperGraphDB(val scope: String) extends ManagedResource {
  lazy val c = new HGConfiguration()
  c.setTransactional(boolean("transactional"))
  c.setStoreImplementation(Class.forName(string("storage-impl")).getConstructor().newInstance().asInstanceOf[HGStoreImplementation])
  lazy val hgdb = HGEnvironment.get(string("path.data"), c)
  info("hypergraph-db instantiated")
  def db = hgdb

  override def close(): Unit = {
    hgdb.close()
  }
}
