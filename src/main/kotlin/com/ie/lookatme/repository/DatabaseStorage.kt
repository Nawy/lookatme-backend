package com.ie.lookatme.repository

import io.vertx.core.Vertx
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions

class DatabaseStorage(
  private val port : Int,
  private val hostname : String,
  private val database : String,
  private val user : String,
  private val password : String,
  private val vertx : Vertx
  ) {

  val client : PgPool

  init {
    var connectOptions = PgConnectOptions()
    connectOptions.host = this.hostname
    connectOptions.port = this.port
    connectOptions.database = this.database
    connectOptions.user = this.user
    connectOptions.password = this.password


    var poolOptions = PoolOptions()
    poolOptions.maxSize = 5

    this.client = PgPool.pool(this.vertx, connectOptions, poolOptions)
  }

  fun finalize() {
    this.client.close()
  }
}
