package com.ie.lookatme.service

import com.google.common.cache.CacheBuilder
import com.ie.lookatme.model.User
import java.util.*
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit

class SessionService {

  private val sessionMap : ConcurrentMap<UUID, User> = CacheBuilder.newBuilder()
    .concurrencyLevel(4)
    .maximumSize(10)
    .expireAfterAccess(24, TimeUnit.HOURS)
    .build<UUID, User>()
    .asMap()

  fun isValid(sessionId : UUID) : Boolean {
    return sessionMap[sessionId] != null
  }

  fun put(user : User) : UUID {
    val sessionId : UUID = UUID.randomUUID()
    sessionMap[sessionId] = user
    return sessionId
  }

  fun invalidate(sessionId : UUID) : User? {
    val user : User? = sessionMap[sessionId] ?: return null
    sessionMap.remove(sessionId)
    return user
  }

}
