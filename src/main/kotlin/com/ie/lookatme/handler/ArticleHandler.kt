package com.ie.lookatme.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.ie.lookatme.model.Article
import com.ie.lookatme.service.ArticleService
import com.ie.lookatme.service.AuthService
import io.vertx.ext.web.Router
import java.time.LocalDateTime
import java.util.Collections.emptyList

class ArticleHandler(
  private val service : ArticleService,
  private val authService : AuthService,
  private val mapper : ObjectMapper,
  router : Router
) : MainHandler(router) {

  init {
    this.createArticle()
    this.updateArticle()
    this.listArticles()
    this.getArticle()
    this.removeArticle()
  }

  private fun createArticle() {
    postMapping("/article", authService::authenticate) { context ->
      context.request().bodyHandler { bodyAsString ->
        val artist : ArticleCreateDTO = mapper.readValue(bodyAsString.toString(), ArticleCreateDTO::class.java)

        service.create(
          Article(
            null,
            artist.title,
            artist.content,
            artist.isSearchable,
            LocalDateTime.now(),
            LocalDateTime.now(),
            artist.tags
          )
        ) { createdArticle  ->
          if (createdArticle == null) {
            context.response()
              .setStatusCode(500)
              .end()
          } else {
            val resultJson = mapper.writeValueAsString(createdArticle)
            context.response()
              .setStatusCode(201)
              .putHeader("content-type", "application/json")
              .end(resultJson)
          }
        }
      }
    }

  }

  private fun updateArticle() {
    patchMapping("/article", authService::authenticate) { context ->
      context.request().bodyHandler { bodyAsString ->
        val artist : ArticleUpdateDTO = mapper.readValue(
          bodyAsString.toString(),
          ArticleUpdateDTO::class.java
        )

        service.update(
          Article(
            artist.id,
            artist.title,
            artist.content,
            artist.isSearchable,
            LocalDateTime.now(),
            LocalDateTime.now(),
            artist.tags
          )
        ) { createdArticle  ->
          if (createdArticle == null) {
            context.response()
              .setStatusCode(500)
              .end()
          } else {
            val resultJson = mapper.writeValueAsString(createdArticle)
            context.response()
              .setStatusCode(201)
              .putHeader("content-type", "application/json")
              .end(resultJson)
          }
        }
      }
    }
  }

  private fun getArticle() {
    getMapping("/article/:id") {context ->
      val articleId = context.request().getParam("id").toLong()

      this.service.get(articleId) { article ->

        if (article != null) {
          var resultJson : String = mapper.writeValueAsString(article)
          context.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(resultJson)
        } else {
          context.response()
            .setStatusCode(404)
            .end()
        }
      }
    }
  }

  private fun listArticles() {
    getMapping("/article") { context ->
      val page = context.request().getParam("page")?.toInt() ?: 0
      var size = context.request().getParam("size")?.toInt() ?: 10

      if (size > 100) {
        size = 100
      }

      if (page < 0) {
        context.response()
          .setStatusCode(400)
          .end("Page size cannot be a negative value")
      } else {
        this.service.list(page, size) { articles ->
          var resultJson : String = mapper.writeValueAsString(articles)
          context.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(resultJson)
        }
      }
    }

  }

  private fun removeArticle() {
    deleteMapping("/article/:id", authService::authenticate) {context ->
      val articleId = context.request().getParam("id")?.toLong()

      service.delete(articleId!!) {deletedArticle ->
        if (deletedArticle != null) {
          var resultJson : String = mapper.writeValueAsString(deletedArticle)
          context.response()
            .setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(resultJson)
        } else {
          context.response()
            .setStatusCode(404)
            .putHeader("content-type", "application/json")
            .end()
        }
      }
    }
  }
}

data class ArticleCreateDTO(
  var title : String = "",
  var content : String = "",
  var isSearchable : Boolean = true,
  var tags : List<String> = emptyList()
)

data class ArticleUpdateDTO(
  var id : Long = -1,
  var title : String = "",
  var content : String = "",
  var isSearchable : Boolean = true,
  var tags : List<String> = emptyList()
)
