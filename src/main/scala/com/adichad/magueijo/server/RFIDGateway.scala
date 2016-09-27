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

import com.thingmagic._

/**
  * Created by adichad on 15/08/16.
  */
class RFIDGateway(val scope: String) extends Server {
  var reader: Reader = _

  private def connectReader(): Unit = {
    reader = Reader.create(string("reader-uri"))
    reader.connect()
  }

  override def bind(): Unit = {
    connectReader()

    reader.paramList().sorted.foreach { p =>
      info(
        p + "=" +
          (
            try {
              reader.paramGet(p) match {
                case v: Array[Array[Int]]=>"["+v.map(x=>"["+x.map(_.toString).mkString(", ")+"]").mkString(", ")+"]"
                case v: Array[Array[Any]]=>"["+v.map(x=>"["+x.map(_.toString).mkString(", ")+"]").mkString(", ")+"]"
                case v: Array[Array[AnyRef]]=>"["+v.map(x=>"["+x.map(_.toString).mkString(", ")+"]").mkString(", ")+"]"
                case v: Array[Int]=>"["+v.map(_.toString).mkString(", ")+"]"
                case v: Array[Long]=>"["+v.map(_.toString).mkString(", ")+"]"
                case v: Array[Float]=>"["+v.map(_.toString).mkString(", ")+"]"
                case v: Array[Double]=>"["+v.map(_.toString).mkString(", ")+"]"

                case v: Array[Any]=>"["+v.map(_.toString).mkString(", ")+"]"
                case v: Array[AnyRef]=>"["+v.map(_.toString).mkString(", ")+"]"
                case v: Any => v.toString
              }
            }
            catch {
              case e: Exception=>e.getMessage
            }
          )
      )
    }

    val power = reader.paramGet("/reader/radio/readPower").asInstanceOf[Int]

    reader.read(5000).foreach { trd =>
      import trd._
      val distance = math.pow(10, (power - getRssi) / 20.0d)
      info(s"EPC:$epcString\tTIME:$getTime\tFREQ:$getFrequency\tANT:$getAntenna\tCOUNT:$getReadCount\tRSSI:$getRssi\tDIST:$distance\tPHASE:$getPhase")
    }

    info("rfid-reader connected")
  }

  override def close(): Unit = {
    reader.destroy()
    info("rfid-reader shutdown")
  }
}
