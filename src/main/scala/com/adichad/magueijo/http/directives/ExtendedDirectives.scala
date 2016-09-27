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

package com.adichad.magueijo.http.directives

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.model.{HttpResponse, RemoteAddress, StatusCodes}
import akka.http.scaladsl.server._

import scala.concurrent.Promise

/**
  * Created by adichad on 16/07/16.
  */
trait ExtendedDirectives extends Directives {


  //this directive adds access control headers to normal responses
  private def addAccessControlHeaders: Directive0 = {
    mapResponseHeaders { headers =>
      `Access-Control-Allow-Origin`.* +:
        `Access-Control-Allow-Credentials`(true) +:
        `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With") +:
        headers
    }
  }

  //this handles preflight OPTIONS requests. TODO: see if can be done with rejection handler,
  //otherwise has to be under addAccessControlHeaders
  private def preflightRequestHandler: Route = options {
    complete(HttpResponse(StatusCodes.OK).withHeaders(
      `Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)
    )
    )
  }

  def corsHandler(r: Route): Route = addAccessControlHeaders {
    preflightRequestHandler ~ r
  }

  def extractClientIPOrUnknown: Directive1[RemoteAddress] =
    extractClientIP | headerValuePF { case _ ⇒ RemoteAddress.Unknown }


  def validateIP(r: Route)(f: RemoteAddress => Boolean): Route = {
    extractClientIPOrUnknown { ip=>
      validate(f(ip), "invalid request source") {
        r
      }
    }
  }



  // a custom directive
  def imperativelyComplete(inner: ImperativeRequestContext => Unit): Route = { ctx: RequestContext =>
    val p = Promise[RouteResult]()
    inner(new ImperativeRequestContext(ctx, p))
    p.future
  }

}

// an imperative wrapper for request context
final class ImperativeRequestContext(ctx: RequestContext, promise: Promise[RouteResult]) {
  private implicit val ec = ctx.executionContext
  def complete(obj: ToResponseMarshallable): Unit = ctx.complete(obj).onComplete(promise.complete)
  def fail(error: Throwable): Unit = ctx.fail(error).onComplete(promise.complete)
}