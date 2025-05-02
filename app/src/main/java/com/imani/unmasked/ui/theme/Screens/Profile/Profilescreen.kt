package com.imani.unmasked.ui.theme.Screens.Profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.imani.unmasked.ui.theme.Screens.Home.PostItem

@Composable
fun ProfileScreen(authViewModel: AuthViewModel, navController: NavHostController) {
    val user = Firebase.auth.currentUser
    val email = user?.email ?: "Unknown"
    val userId = user?.uid ?: ""

    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }

    LaunchedEffect(true) {
        Firebase.firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    posts = snapshot.documents.mapNotNull { it.toObject(Post::class.java)?.copy(id = it.id) }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton (onClick = {
                        authViewModel.logout()
                        navController.navigate("auth") {
                            popUpTo("feed") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign out")
                    }
                }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            Text(text = "Logged in as: $email", style = MaterialTheme.typography, modifier = Modifier.padding(16.dp))

            LazyColumn {
                items(posts) { post ->
                    PostItem(post = post)
                }
            }
        }
    }
}
