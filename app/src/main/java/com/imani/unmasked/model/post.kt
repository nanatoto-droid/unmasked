package com.imani.unmasked.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val anonymous: Boolean = false
)
