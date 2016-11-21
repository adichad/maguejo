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

package com.adichad.magueijo

import com.adichad.magueijo.conf.Configured
import scala.collection.JavaConversions._

/**
  * Created by adichad on 02/07/16.
  */
object Launcher extends App with Configured {
  override val scope = ""

  try {
    info(sysprop("component.name"))
    info(s"java.library.path=${sysprop("java.library.path")}")
    info(s"args: ${args.toList}")
    if(args.length > 0)
      println("usage: java -jar ")
    else {
      info("Log path current: " + sysprop("log.path.current"))
      info("Log path archive: " + sysprop("log.path.archive"))

      if (boolean("sysout.detach")) System.out.close()
      if (boolean("syserr.detach")) System.err.close()

      resources("resources", strings("resource-construct-order"))

    }
  } catch {
    case e: Throwable =>
      error("fatal", e)
      throw e
  }

}
