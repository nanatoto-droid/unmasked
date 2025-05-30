
package com.imani.unmasked.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val anonymous: Boolean = false,
    val likes: List<String> = emptyList(),
    val comments: List<Comment> = emptyList()
)

data class Comment(
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Long = 0
)
