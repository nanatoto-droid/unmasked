package com.imani.unmasked.ui.theme.Screens.Profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.imani.unmasked.R
import com.imani.unmasked.data.AuthViewModel
import com.imani.unmasked.model.Post
import com.imani.unmasked.ui.theme.Screens.feed.PostItem
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(authViewModel: AuthViewModel, navController: NavHostController) {
    val user = Firebase.auth.currentUser
    val userId = user?.uid.orEmpty()

    var name by remember { mutableStateOf(user?.displayName ?: "") }
    var bio by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }
    var posts by remember { mutableStateOf(emptyList<Post>()) }

    val email = user?.email ?: "Unknown"
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val shouldReload = currentBackStackEntry?.savedStateHandle?.get<Boolean>("shouldReload") ?: false

    var reloadKey by remember { mutableStateOf(0) }

    // Fetch user profile info
    LaunchedEffect(userId, reloadKey) {
        if (userId.isNotEmpty()) {
            val doc = Firebase.firestore.collection("users").document(userId).get().await()
            name = doc.getString("name") ?: name
            bio = doc.getString("bio") ?: ""
            profileImageUrl = doc.getString("profileImageUrl") ?: ""
        }
    }

    // Observe posts
    DisposableEffect(userId) {
        var registration: ListenerRegistration? = null
        if (userId.isNotEmpty()) {
            registration = Firebase.firestore.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        posts = snapshot.documents.mapNotNull {
                            it.toObject(Post::class.java)?.copy(id = it.id)
                        }
                    }
                }
        }

        onDispose {
            registration?.remove()
        }
    }

    if (shouldReload) {
        reloadKey++
        currentBackStackEntry?.savedStateHandle?.set("shouldReload", false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("feed") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel.signOut()
                        navController.navigate("auth") {
                            popUpTo("feed") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {
            // Background
            Image(
                painter = painterResource(R.drawable.bg1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().zIndex(-1f)
            )

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (profileImageUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(profileImageUrl),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = name,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "Logged in as: $email",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (bio.isNotEmpty()) bio else "No bio available.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("shouldReload", false)
                                navController.navigate("edit_profile")
                            }) {
                                Text("Edit Profile")
                            }
                        }
                    }
                }

                items(posts) { post ->
                    PostItem(post = post)
                }
            }
        }
    }
}
