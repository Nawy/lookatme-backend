package com.ie.lookatme.model

import java.time.LocalDateTime

data class Article(
  val id : Long? = null,
  val title: String,
  val content: String,
  val isSearchable: Boolean,
  val createData: LocalDateTime,
  val updateData: LocalDateTime,
  val tags : List<String>
)
