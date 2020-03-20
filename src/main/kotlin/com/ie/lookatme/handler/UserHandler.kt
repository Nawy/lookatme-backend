package com.ie.lookatme.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.ie.lookatme.model.User
import com.ie.lookatme.service.AuthService
import com.ie.lookatme.service.UserService
import io.vertx.ext.web.Router

class UserHandler(
  private val service : UserService,
  private val authService : AuthService,
  private val mapper : ObjectMapper,
  router : Router
) : MainHandler(router) {

  init {
    this.initUser()
    this.createUser()
    this.updateUser()
  }

  fun initUser() {
    putMapping("/user") { context ->
      context.request().bodyHandler { bodyAsString ->
        val user: CreateUserDTO = mapper.readValue(bodyAsString.toString(), CreateUserDTO::class.java)

        service.list(0, 1) {userList ->
          if (userList.isEmpty()) {
            service.create(
              User(null, user.login, user.password)
            ) {createdUser ->
              if (createdUser != null) {
                val resultJson = mapper.writeValueAsString(
                  ResponseUserDTO(createdUser.id!!, createdUser.login)
                )
                context.response()
                  .setStatusCode(201)
                  .putHeader("content-type", "application/json")
                  .end(resultJson)
              } else {
                context.response()
                  .setStatusCode(403)
                  .putHeader("content-type", "application/json")
                  .end()
              }
            }
          } else {
            context.response()
              .setStatusCode(403)
              .putHeader("content-type", "application/json")
              .end()
          }
        }
      }
    }
  }

  fun createUser() {
    postMapping("/user") { context ->
      context.response()
        .setStatusCode(301)
        .end()
    }
  }

  fun updateUser() {
    postMapping("/user/:id") { context ->
      context.response()
        .setStatusCode(301)
        .end()
    }
  }
}

data class ResponseUserDTO(
  val id : Long = 0,
  val login : String = ""
)

data class CreateUserDTO(
  val login : String = "",
  val password : String = ""
)
