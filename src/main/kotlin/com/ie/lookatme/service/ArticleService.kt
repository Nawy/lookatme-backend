package com.ie.lookatme.service

import com.ie.lookatme.model.Article
import com.ie.lookatme.repository.DatabaseStorage
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple
import java.util.*

class ArticleService (
  private val storage : DatabaseStorage,
  private val log : Logger = LoggerFactory.getLogger(ArticleService::class.java)
) {

  fun create(article: Article, handler : (Article?) -> Unit) {
    storage.client.preparedQuery(
      "INSERT INTO articles (title, content, create_date, update_date, is_searchable, tags) VALUES ($1, $2, $3, $4, $5, $6)  RETURNING article_id",
      Tuple.of(
        article.title,
        article.content,
        article.createData,
        article.updateData,
        article.isSearchable,
        article.tags.joinToString(separator = ",")
      )
    ) { ar ->
      if (ar.succeeded()) {
        val rows = ar.result()
        val articleId = rows.iterator().next().getLong("article_id")
        val createdArticle = Article(
          articleId,
          article.title,
          article.content,
          article.isSearchable,
          article.createData,
          article.updateData,
          article.tags
        )
        handler.invoke(createdArticle)
      } else {
        ar.cause().printStackTrace()
        handler.invoke(null)
      }
    }
  }

  fun get(id: Long, handler : (Article?) -> Unit) {
    storage.client.preparedQuery(
      "SELECT article_id, title, content, is_searchable, create_date, update_date, tags FROM articles WHERE article_id=$1",
      Tuple.of(id)
    ) { ar ->
      if (ar.succeeded()) {
        val rows = ar.result()
        if (rows.count() > 0) {
          val row : Row = rows.iterator().next()
          val article = parseArticle(row)

          handler.invoke(article)
        } else {
          handler.invoke(null)
        }

      } else {
        ar.cause().printStackTrace()
        handler.invoke(null)
      }
    }
  }

  fun list(page: Int, pageSize: Int, handler : (List<Article?>) -> Unit) {
    storage.client.preparedQuery(
      "SELECT article_id, title, content, is_searchable, tags, create_date, update_date FROM articles ORDER BY create_date DESC OFFSET $1 LIMIT $2",
      Tuple.of(page*pageSize, pageSize)
    ) { ar ->
      if (ar.succeeded()) {
        val rows = ar.result()

        if (rows.count() > 0) {
          val articles : List<Article?> = rows.map { row ->  parseArticle(row) }.toList()
          handler.invoke(articles)
        } else {
          handler.invoke(Collections.emptyList())
        }

      } else {
        log.info(ar.cause())
        handler.invoke(Collections.emptyList())
      }
    }
  }

  fun update(article: Article, handler : (Article?) -> Unit) {
    storage.client.preparedQuery(
      "UPDATE articles SET " +
          " title = $1, content = $2, update_date = $3, is_searchable = $4, tags = $5 " +
          " WHERE article_id = $6",
      Tuple.of(
        article.title,
        article.content,
        article.updateData,
        article.isSearchable,
        article.tags.joinToString(separator = ","),
        article.id
      )
    ) { ar ->
      if (ar.succeeded()) {
        handler.invoke(article)
      } else {
        ar.cause().printStackTrace()
        handler.invoke(null)
      }
    }
  }

  fun delete(id : Long, handler : (Article?) -> Unit) {
    get(id) { article ->
      if (article != null) {
        storage.client.preparedQuery(
          "DELETE FROM article WHERE article_id = $1",
          Tuple.of(id)
        ) { ar ->
          if (ar.succeeded()) {
            handler.invoke(article)
          } else {
            log.error(ar.cause())
            handler.invoke(null)
          }
        }
      } else {
        // don't have a record
        handler.invoke(null)
      }
    }
  }

  private fun parseArticle(row : Row) : Article? {
    try {
      return Article(
        row.getLong("article_id"),
        row.getString("title"),
        row.getString("content"),
        row.getBoolean("is_searchable"),
        row.getLocalDateTime("create_date"),
        row.getLocalDateTime("update_date"),
        row.getString("tags").split(",")
      )
    } catch (e : Exception) {
      log.info("Error $e")
    }
    return null
  }
}
