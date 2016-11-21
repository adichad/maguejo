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

package com.adichad.magueijo.conf

import java.io.{File, IOException, PrintWriter}
import java.lang.management.ManagementFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.Properties

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import com.adichad.magueijo.resource.ManagedResource
import com.typesafe.config.{Config, ConfigFactory}
import grizzled.slf4j.Logging
import org.elasticsearch.common.settings.Settings

import scala.collection.JavaConversions._
import scala.collection._
import scala.collection.convert.decorateAsScala._
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.TimeUnit
import scala.reflect.ClassTag

/**
  * Created by adichad on 02/07/16.
  */

object Configured extends Configured {
  override protected[this] val scope = ""
  private val registry: concurrent.Map[String, Any] = new ConcurrentHashMap[String, Any]().asScala
  private val closeables = new java.util.Vector[AutoCloseable]
  Runtime.getRuntime addShutdownHook new Thread {
    override def run() = {
      try {
        info(string("component.name")+" shutdown initiated")
        closeables.foreach(_.close())
        info("resources terminated")
        info(string("component.name")+" shutdown complete")
      } catch {
        case e: Throwable => error("shutdown hook failure", e)
      }
    }
  }


  private val originalConfig =
    configure("environment", "application", "environment_defaults", "application_defaults")
  private var config = originalConfig
  private val staticProperties = strings("system.properties.static")
  configureSystem(staticProperties: _*)
  configureSystemDynamic()
  // logging configured at this point

  private def getPid(fallback: String) = {
    val jvmName = ManagementFactory.getRuntimeMXBean.getName
    val index = jvmName indexOf '@'
    if (index > 0) {
      try {
        jvmName.substring(0, index).toLong.toString
      } catch {
        case e: NumberFormatException ⇒ fallback
      }
    } else fallback
  }

  val destPath = string("daemon.pidfile")
  val pidFile = new File(destPath)
  if (pidFile.createNewFile) {
    val pid = getPid("unknown-pid")
    (new PrintWriter(pidFile) append pid).close()
    pidFile.deleteOnExit()
    info(s"pid file [pid]: $destPath [$pid]")
    pid
  } else {
    throw new IOException(s"pid file already exists at: $destPath")
  }

  private val configSource = configured[ConfigSource]("config.source")
  configSource.initConfig()

  def apply(config: Config): Unit = {
    val c = ConfigFactory.load(config.withFallback(originalConfig))
    synchronized(this.config = c)
    configureSystemDynamic()
  }

  private def configureSystemDynamic(): Unit = {
    val dynamicProperties = strings("system.properties.dynamic")
    dynamicProperties.removeAll(staticProperties)
    configureSystem(dynamicProperties: _*)
  }

  private def configure(resources: String*) =
    ConfigFactory.load(
      resources
        .map(ConfigFactory.parseResourcesAnySyntax)
        .reduceLeft(_ withFallback _)
        .withFallback(ConfigFactory.systemEnvironment()))

  private def configureSystem(propertyNames: String*) =
    propertyNames.foreach{p =>
      System.setProperty(p, string(p))
    }

}

trait Configured extends Logging {
  private val c = Configured
  import Configured.{config, registry, closeables}
  protected[this] val scope: String

  private[this] final def path(part: String) =
    if(scope.isEmpty) part else if(part.isEmpty) scope else s"$scope.$part"

  private[this] final def path(prefix: String, part: String) =
    if(prefix.isEmpty) part else if(part.isEmpty) prefix else s"$prefix.$part"


  protected[this] final def configured[T <: Configured](part: String, register: Boolean = false): T = {
    if(register) {
      val key = string(s"$part.name")
      registry.put(key, Class.forName(string(s"$part.type")).getConstructor(classOf[String]).newInstance(path(part)).asInstanceOf[T])
      registry(key).asInstanceOf[T]
    } else
      Class.forName(string(s"$part.type")).getConstructor(classOf[String]).newInstance(path(part)).asInstanceOf[T]
  }

  protected[this] final def configureds[T <: Configured](part: String, register: Boolean = false): java.util.Map[String, T] = {
    (config getAnyRef path(part)).asInstanceOf[java.util.Map[String, AnyRef]]
      .map { x =>
        val p = path(path(part), x._1)
        val obj = Class.forName(string(s"${path(part, x._1)}.type")).getConstructor(classOf[String]).newInstance(p).asInstanceOf[T]
        obj match {
          case o: AutoCloseable => closeables.add(o)
          case _ =>
        }
        if(register) {
          val key = string(s"${path(part, x._1)}.name")
          registry.put(key, obj)
          x._1 -> registry(key).asInstanceOf[T]
        } else {
          x._1 -> obj
        }
      }.toMap[String, T]
  }


  protected[this] final def resources(part: String, sequence: Seq[String]): Unit = {
    val mapped = (config getAnyRef path(part)).asInstanceOf[java.util.Map[String, AnyRef]]
    (sequence map (s=> (s, mapped(s))))
      .map { x =>
        val p = path(path(part), x._1)
        val resource = Class.forName(string(s"${path(part, x._1)}.type")).getConstructor(classOf[String]).newInstance(p).asInstanceOf[ManagedResource]
        resource match {
          case o: AutoCloseable => closeables.add(o)
          case _ =>
        }
        val key = string(s"${path(part, x._1)}.name")
        registry.put(key, resource)
        x._1 -> registry(key).asInstanceOf[ManagedResource]
      }.toMap[String, Configured]

  }

  protected[this] final def actor[T <: Actor](part: String, register: Boolean = false)
                                             (implicit system: ActorSystem, materializer: Materializer, executionContext: ExecutionContextExecutor, t: ClassTag[T]): ActorRef = {
    if(register) {
      val key = string(s"$part.name")
      registry.put(key, system.actorOf(Props(t.runtimeClass, path(part))))
      registry(key).asInstanceOf[ActorRef]
    } else
      system.actorOf(Props(t.runtimeClass, path(part)))
  }


  protected[this] final def keys(part: String) = (config getAnyRef path(part)).asInstanceOf[java.util.Map[String, Any]].keySet
  protected[this] final def vals[T](part: String) = (config getAnyRef path(part)).asInstanceOf[java.util.Map[String, T]].values

  protected[this] final def size(part: String) = config getBytes path(part)
  protected[this] final def boolean(part: String) = config getBoolean path(part)
  protected[this] final def int(part: String) = config getInt path(part)
  protected[this] final def long(part: String) = config getLong path(part)
  protected[this] final def double(part: String) = config getDouble path(part)
  protected[this] final def string(part: String) = config getString path(part)
  protected[this] final def duration(part: String) = config getDuration path(part)
  protected[this] final def duration(part: String, unit: TimeUnit) = config getDuration(path(part), unit)

  protected[this] final def conf(part: String = "") = config getConfig path(part)

  protected[this] final def sizes(part: String) = config getBytes path(part)
  protected[this] final def booleans(part: String) = config getBooleanList path(part)
  protected[this] final def ints(part: String) = config getIntList path(part)
  protected[this] final def longs(part: String) = config getLongList path(part)
  protected[this] final def doubles(part: String) = config getDoubleList path(part)
  protected[this] final def strings(part: String) = config getStringList path(part)
  protected[this] final def durations(part: String) = config getDurationList path(part)
  protected[this] final def durations(part: String, unit: TimeUnit) = config getDurationList(path(part), unit)
  protected[this] final def confs(part: String) = config getConfigList path(part)

  protected[this] final def props(part: String): Properties = {
    val p = new Properties
    val c = conf(part)
    for(e <- c.entrySet())
      p.setProperty(e.getKey, c getString e.getKey)
    p
  }

  protected[this] final def registered[T <: Configured](key: String): T = {
    registry(key).asInstanceOf[T]
  }

  protected[this] final def sysprop(part: String) =
    System.getProperty(path(part))

  protected[this] def settings(part: String) = {
    val settings = Settings.builder()
    val c = conf(part)
    for( e <- c.entrySet() ) {
      try {
        settings.put(e.getKey, c.getString(e.getKey))
      } catch {
        case ex:Exception => settings.putArray(e.getKey, c.getStringList(e.getKey):_*)
      }
    }
    settings.build()
  }

}
