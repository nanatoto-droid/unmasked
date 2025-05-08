package com.imani.unmasked.ui.theme.Screens.Profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.imani.unmasked.data.AuthViewModel
import com.imani.unmasked.model.Post
import com.imani.unmasked.ui.theme.Screens.feed.PostItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(authViewModel: AuthViewModel, navController: NavHostController) {
    val user = Firebase.auth.currentUser
    val email = user?.email ?: "Unknown"
    val userId = user?.uid ?: ""

    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var name by remember { mutableStateOf(user?.displayName ?: "") } // Fetch displayName directly
    var bio by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }

    // Load additional user profile info from Firestore (if needed)
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            Firebase.firestore.collection("users").document(userId).get().addOnSuccessListener { doc ->
                // Only overwrite name if Firestore name exists
                name = doc.getString("name") ?: name
                bio = doc.getString("bio") ?: ""
                profileImageUrl = doc.getString("profileImageUrl") ?: ""
            }
        }
    }

    // Load user posts
    LaunchedEffect(userId) {
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
                    IconButton(onClick = {
                        authViewModel.signOut()
                        navController.navigate("auth") {
                            popUpTo("feed") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign out")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Image and Info
            if (profileImageUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(profileImageUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = name.ifEmpty { "No Name" },
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Logged in as: $email",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = bio.ifEmpty { "No bio available." },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Profile Button
            Button(
                onClick = { navController.navigate("edit_profile") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Edit Profile")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User's Posts
            LazyColumn {
                items(posts.size) { index ->
                    PostItem(post = posts[index])
                }
            }
        }
    }
}
