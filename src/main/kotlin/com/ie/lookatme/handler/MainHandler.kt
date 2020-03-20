package com.ie.lookatme.handler

import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

abstract class MainHandler(
  protected val router : Router
) {

  protected fun postMapping(
    uri : String,
    authHandler : ((RoutingContext) -> Unit)? = null,
    handler : (RoutingContext) -> Unit
  ) {
    applyHandler(
      router.post(uri),
      authHandler,
      handler
    )
  }

  protected fun getMapping(
    uri : String,
    authHandler : ((RoutingContext) -> Unit)? = null,
    handler : (RoutingContext) -> Unit
  ) {
    applyHandler(
      router.get(uri),
      authHandler,
      handler
    )
  }

  protected fun patchMapping(
    uri : String,
    authHandler : ((RoutingContext) -> Unit)? = null,
    handler : (RoutingContext) -> Unit
  ) {
    applyHandler(
      router.patch(uri),
      authHandler,
      handler
    )
  }

  protected fun putMapping(
    uri : String,
    authHandler : ((RoutingContext) -> Unit)? = null,
    handler : (RoutingContext) -> Unit
  ) {
    applyHandler(
      router.put(uri),
      authHandler,
      handler
    )
  }

  protected fun deleteMapping(
    uri : String,
    authHandler : ((RoutingContext) -> Unit)? = null,
    handler : (RoutingContext) -> Unit
  ) {
    applyHandler(
      router.delete(uri),
      authHandler,
      handler
    )
  }

  private fun applyHandler(
    route : Route,
    authHandler : ((RoutingContext) -> Unit)? = null,
    handler : (RoutingContext) -> Unit
  ) {
    val newRoute : Route = route.produces("application/json")

    if (authHandler != null) {
      route.handler { context -> authHandler.invoke(context) }
    }
    newRoute.handler { context -> handler.invoke(context) }
  }
}
