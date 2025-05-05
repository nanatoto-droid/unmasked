package com.imani.unmasked.ui.theme.Screens.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.imani.unmasked.data.AuthViewModel
import com.imani.unmasked.model.Post
import com.imani.unmasked.model.PostViewModel


@Composable
fun FeedScreen(authViewModel: AuthViewModel, postViewModel: PostViewModel, navController: NavHostController) {
    val posts by postViewModel.posts.collectAsState()

    Scaffold (
        bottomBar = {
            BottomNavigation {
                BottomNavigationItemView(
                    selected = true,
                    onClick = { navController.navigate("feed") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
                    label = { Text("Feed") }
                )
                BottomNavigationItemView(
                    selected = false,
                    onClick = { navController.navigate("create") },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Post") },
                    label = { Text("Post") }
                )
                BottomNavigationItemView(
                    selected = false,
                    onClick = { navController.navigate("profile") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        },
        floatingActionButton = TODO()
    ) {
        LazyColumn(modifier = Modifier
            .padding(it)) {
            items(posts) { post ->
                PostItem(post = post)
            }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    val user = Firebase.auth.currentUser
    val userId = user?.uid ?: ""

    var liked by remember { mutableStateOf(post.likes.contains(userId)) }
    var commentText by remember { mutableStateOf("") }

    Card (
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        elevation = 8.dp,
        backgroundColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
        Column(modifier = Modifier
            .padding(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (post.anonymous) {
                    // Just show "Anonymous" without click
                    Text(
                        text = "Anonymous",
                        style = MaterialTheme.typography.headlineMedium
                    )
                } else {
                    // Show username (clickable in future, optional)
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }


                // Show Delete button only if I own the post
                if (post.userId == userId) {
                    IconButton(onClick = {
                        deletePost(post) {

                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberImagePainter(post.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.text)

            Spacer(modifier = Modifier.height(8.dp))

            // Like button
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                IconButton(onClick = {
                    toggleLike(post.id, userId, liked)
                    liked = !liked
                }) {
                    Icon(
                        imageVector = if (liked) Icons.Default.Favorite
                        else Icons.Default.FavoriteBorder,
                        contentDescription = "Like"
                    )
                }
                Text(text = "${post.likes.size + if (liked && !post.likes.contains(userId)) 1 
                else 0} likes")
            }

            // Comments
            post.comments.forEach {
                val displayName = if (post.anonymous) "Anonymous"
                else it.username
                Text(text = "$displayName: ${it.text}")
            }

            // Add Comment
            Row {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Add a comment...") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    if (commentText.isNotBlank()) {
                        addComment(post.id, userId, user?.email ?: "Unknown", commentText)
                        commentText = ""
                    }
                }) {
                    Text("Post")
                }
            }
        }
    }
}

fun toggleLike(postId: String, userId: String, liked: Boolean) {
    val postRef = Firebase.firestore
        .collection("posts")
        .document(postId)
    Firebase.firestore.runTransaction { transaction ->
        val snapshot = transaction.get(postRef)
        val currentLikes = snapshot.get("likes") as? List<String> ?: emptyList()
        val updatedLikes = if (liked) currentLikes - userId
        else currentLikes + userId
        transaction.update(postRef, "likes", updatedLikes)
    }
}

fun addComment(postId: String, userId: String, username: String, text: String) {
    val comment = mapOf(
        "userId" to userId,
        "username" to username,
        "text" to text,
        "timestamp" to System.currentTimeMillis() / 1000
    )
    val postRef = Firebase.firestore.collection("posts").document(postId)
    Firebase.firestore.runTransaction { transaction ->
        val snapshot = transaction.get(postRef)
        val currentComments = snapshot.get("comments") as? List<Map<String, Any>> ?: emptyList()
        val updatedComments = currentComments + comment
        transaction.update(postRef, "comments", updatedComments)
    }
}
fun deletePost(post: Post, onComplete: () -> Unit) {
    // 1. Delete image from Storage
    val imageRef = Firebase.storage.getReferenceFromUrl(post.imageUrl)
    imageRef.delete().addOnSuccessListener {
        // 2. Delete Firestore post
        Firebase.firestore.collection("posts").document(post.id).delete()
            .addOnSuccessListener { onComplete() }
    }
}
