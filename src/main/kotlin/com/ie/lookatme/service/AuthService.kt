package com.ie.lookatme.service

import io.vertx.ext.web.RoutingContext
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class AuthService(
  private val sessionService : SessionService,
  private val userService: UserService
) {

  fun authenticate(context : RoutingContext) {

    val sessionIdStr = context.request().getHeader("auth-token")
    val sessionId = if (sessionIdStr != null) UUID.fromString(sessionIdStr) else null

    val isSessionValid = if (sessionId != null) sessionService.isValid(sessionId) else false

    if (isSessionValid) {
      context.next()
    } else {
      context.response()
        .setStatusCode(401)
        .end()
    }
  }

  fun login(
    login : String,
    password : String,
    handler : (UUID?) -> Unit
  ) {
    userService.getUserByLogin(login) {user ->
      if (user != null) {
        if (comparePasswords(password, user.password)) {
          val sessionId = sessionService.put(user)

          handler.invoke(sessionId)
        } else {
          handler.invoke(null)
        }
      } else {
        handler.invoke(null)
      }
    }
  }

  fun logout(sessionId : UUID?, handler : (UUID?) -> Unit) {
    if (sessionId != null && sessionService.isValid(sessionId)) {
      sessionService.invalidate(sessionId);
      handler.invoke(sessionId)
    } else {
      handler.invoke(null)
    }
  }

  fun comparePasswords(password : String, hashed : String) : Boolean {
    return getPasswordHash(password) == hashed
  }

  fun getPasswordHash(password : String) : String {

    var spec: KeySpec = PBEKeySpec(password.toCharArray(), "111".toByteArray(), 65536, 128)
    var factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    return String(factory.generateSecret(spec).encoded)
  }
}
