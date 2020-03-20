package com.ie.lookatme.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.ie.lookatme.service.AuthService
import io.vertx.ext.web.Router
import java.util.*

class AuthHandler(
  private val authService : AuthService,
  private val mapper : ObjectMapper,
  router : Router
) : MainHandler(router) {

  init {
    this.login()
    this.logout()
  }

  fun login() {
    postMapping("/login") {context ->
      context.request().bodyHandler { bodyAsString ->
        val loginData: LoginDTO = mapper.readValue(bodyAsString.toString(), LoginDTO::class.java)
        authService.login(loginData.login, loginData.password) { sessionId ->
          if (sessionId != null) {
            var resultJson : String = mapper.writeValueAsString(
              SessionDTO(sessionId)
            )
            context.response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json")
              .end(resultJson)
          } else {
            context.response()
              .setStatusCode(401)
              .end()
          }
        }
      }
    }
  }

  fun logout() {
    postMapping("/logout") {context ->
      val sessionIdStr = context.request().getHeader("auth-token")
      val sessionId = if (sessionIdStr != null) UUID.fromString(sessionIdStr) else null

      authService.logout(sessionId) {result ->
        if (result != null) {
          context.response()
            .setStatusCode(200)
            .end()
        } else {
          context.response()
            .setStatusCode(401)
            .end()
        }
      }
    }
  }
}

data class LoginDTO(
  val login : String = "",
  val password : String = ""
)

data class SessionDTO(
  val sessionId : UUID
)

