package com.ie.lookatme

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.ie.lookatme.handler.ArticleHandler
import com.ie.lookatme.handler.AuthHandler
import com.ie.lookatme.handler.UserHandler
import com.ie.lookatme.model.Article
import com.ie.lookatme.repository.DatabaseStorage
import com.ie.lookatme.service.ArticleService
import com.ie.lookatme.service.AuthService
import com.ie.lookatme.service.SessionService
import com.ie.lookatme.service.UserService
import io.vertx.core.AbstractVerticle
import io.vertx.core.Context
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import java.time.LocalDateTime

class MainVerticle : AbstractVerticle() {

  private var storage : DatabaseStorage? = null
  private var mapper : ObjectMapper = this.createObjectMapper()

  private var articleService : ArticleService? = null
  private var userService : UserService? = null
  private var authService : AuthService? = null
  private var sessionService : SessionService = SessionService()

  override fun init(vertx: Vertx?, context: Context?) {
    super.init(vertx, context)

    this.mapper = this.createObjectMapper()
    this.storage = DatabaseStorage(
      System.getenv("PG_PORT")?.toInt()?: 32768,
      System.getenv("PG_HOSTNAME")?: "localhost",
      System.getenv("PG_DATABASE")?: "postgres",
      System.getenv("PG_USERNAME")?: "postgres",
      System.getenv("PG_PASSWORD")?: "postgres",
      vertx!!
    )

    this.articleService = ArticleService(this.storage!!)
    this.userService = UserService(this.storage!!)
    this.authService = AuthService(this.sessionService, this.userService!!)
  }

  override fun start(startPromise: Promise<Void>) {

    var server = vertx.createHttpServer()
    var router = Router.router(vertx)

    ArticleHandler(articleService!!, authService!!, mapper, router)
    UserHandler(userService!!, authService!!, mapper, router)
    AuthHandler(authService!!, mapper, router)

    server.requestHandler(router)
      .listen(System.getenv("APP_PORT")?.toInt()?: 8888) { http ->
      if (http.succeeded()) {
        startPromise.complete()
        println("HTTP server started on port 8888")
      } else {
        startPromise.fail(http.cause())
      }
    }
  }

  private fun createObjectMapper() : ObjectMapper {
    var mapper = ObjectMapper()
    mapper.registerModule(JavaTimeModule())
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    return mapper
  }
}
