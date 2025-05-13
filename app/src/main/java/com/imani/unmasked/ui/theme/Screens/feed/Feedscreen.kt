package com.imani.unmasked.ui.theme.Screens.feed

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.imani.unmasked.R
import com.imani.unmasked.data.AuthViewModel
import com.imani.unmasked.model.Post
import com.imani.unmasked.model.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FeedScreen(
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel,
    navController: NavHostController
) {
    val posts by postViewModel.posts.collectAsState()
    var selectedItem by remember { mutableStateOf("feed") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.um),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unmasked")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.signOut()
                        navController.navigate("auth") {
                            popUpTo("feed") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedItem == "feed",
                    onClick = {
                        selectedItem = "feed"
                        navController.navigate("feed")
                    },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Feed") },
                    label = { Text("Feed") }
                )
                NavigationBarItem(
                    selected = selectedItem == "create",
                    onClick = {
                        selectedItem = "create"
                        navController.navigate("create")
                    },
                    icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Post") },
                    label = { Text("Post") }
                )
                NavigationBarItem(
                    selected = selectedItem == "profile",
                    onClick = {
                        selectedItem = "profile"
                        navController.navigate("profile")
                    },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {

            // Background Image
            Image(
                painter = painterResource(id = R.drawable.back5),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Post list over the background
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(bottom = 60.dp) // for nav bar space
            ) {
                items(posts) { post ->
                    PostItem(post = post)
                }
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
    var likeCount by remember { mutableStateOf(post.likes.size) }

    Card(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (post.anonymous) "Anonymous" else post.username,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (post.anonymous) Color.Black else MaterialTheme.colorScheme.primary
                )

                if (post.userId == userId) {
                    IconButton(onClick = {
                        deletePost(post) { /* Optional completion */ }
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.text)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    toggleLike(post.id, userId, liked)
                    liked = !liked
                    likeCount += if (liked) 1 else -1
                }) {
                    Icon(
                        imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like"
                    )
                }
                Text(text = "$likeCount likes")
            }

            post.comments.forEach {
                val displayName = if (post.anonymous) "Anonymous" else it.username
                Text(text = "$displayName: ${it.text}")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
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
    val postRef = Firebase.firestore.collection("posts").document(postId)
    Firebase.firestore.runTransaction { transaction ->
        val snapshot = transaction.get(postRef)
        val currentLikes = snapshot.get("likes") as? List<String> ?: emptyList()
        val updatedLikes = if (liked) currentLikes - userId else currentLikes + userId
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
    val imageRef = Firebase.storage.getReferenceFromUrl(post.imageUrl)
    imageRef.delete().addOnSuccessListener {
        Firebase.firestore.collection("posts").document(post.id).delete()
            .addOnSuccessListener { onComplete() }
    }
}
