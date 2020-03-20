package com.ie.lookatme.service

import com.ie.lookatme.model.Article
import com.ie.lookatme.model.User
import com.ie.lookatme.repository.DatabaseStorage
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.Tuple
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UserService(
  private val storage: DatabaseStorage,
  private val log: Logger = LoggerFactory.getLogger(UserService::class.java)
) {

  fun create(user: User, handler : (User?) -> Unit) {
    storage.client.preparedQuery(
      "INSERT INTO users (login, password, role) VALUES ($1, $2, $3)  RETURNING user_id",
      Tuple.of(
        user.login,
        getPasswordHash(user.password),
        "ADMIN"
      )
    ) { ar ->
      if (ar.succeeded()) {
        val rows = ar.result()
        val userId = rows.iterator().next().getLong("user_id")
        val createdUser = User(
          userId,
          user.login,
          user.password
        )
        handler.invoke(createdUser)
      } else {
        log.error(ar.cause())
        handler.invoke(null)
      }
    }
  }

  fun update(user: User, handler : (User?) -> Unit) {
    storage.client.preparedQuery(
      "UPDATE users SET login = $1, password = $2 WHERE user_id = $3",
      Tuple.of(user.login, user.password, user.id)
    ) { ar ->
      if (ar.succeeded()) {
        handler.invoke(user)
      } else {
        log.error(ar.cause())
        handler.invoke(null)
      }
    }
  }

  fun getUserByLogin(login : String, handler : (User?) -> Unit) {
    storage.client.preparedQuery(
      "SELECT user_id, login, password FROM users WHERE login=$1",
      Tuple.of(login)
    ) { ar ->
      if (ar.succeeded()) {
        val rows = ar.result()
        if (rows.count() > 0) {
          val row : Row = rows.iterator().next()

          val user = parseUser(row)
          handler.invoke(user)
        } else {
          handler.invoke(null)
        }

      } else {
        log.error(ar.cause())
        handler.invoke(null)
      }
    }
  }

  fun list(page : Int, size : Int, handler : (List<User>) -> Unit) {
    storage.client.preparedQuery(
      "SELECT user_id, login FROM users OFFSET $1 LIMIT $2",
      Tuple.of(page*size, size)
    ) { ar ->
      if (ar.succeeded()) {
        val rows = ar.result()

        if (rows.count() > 0) {
          val users : List<User> = rows.map { row ->  parseUser(row)!! }.toList()
          handler.invoke(users)
        } else {
          handler.invoke(Collections.emptyList())
        }

      } else {
        log.info(ar.cause())
        handler.invoke(Collections.emptyList())
      }
    }
  }

  fun get(id: Long, handler : (User?) -> Unit) {
    storage.client.preparedQuery(
      "SELECT user_id, login, password FROM users WHERE user_id=$1",
      Tuple.of(id)
    ) { ar ->
      if (ar.succeeded()) {
        val rows = ar.result()
        if (rows.count() > 0) {
          val row : Row = rows.iterator().next()

          val user = parseUser(row)
          handler.invoke(user)
        } else {
          handler.invoke(null)
        }

      } else {
        ar.cause().printStackTrace()
        handler.invoke(null)
      }
    }
  }

  private fun parseUser(row : Row) : User? {
    try {
      return User(
        row.getLong("user_id"),
        row.getString("login"),
        row.getString("password")?: ""
      )
    } catch (e : Exception) {
      log.info(e)
    }
    return null
  }

  fun comaprePasswords(password : String, hashed : String) : Boolean {
    return getPasswordHash(password) == hashed
  }

  fun getPasswordHash(password : String) : String {

    var spec: KeySpec = PBEKeySpec(password.toCharArray(), "111".toByteArray(), 65536, 128)
    var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    return String(factory.generateSecret(spec).encoded)
  }
}
