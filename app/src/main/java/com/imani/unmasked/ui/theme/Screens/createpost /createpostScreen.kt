@file:OptIn(ExperimentalMaterial3Api::class)

package com.imani.unmasked.ui.theme.Screens.createpost

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.imani.unmasked.data.AuthViewModel
import java.util.*

@Composable
fun CreatePostScreen(authViewModel: AuthViewModel, navController: NavHostController) {
    var text by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var anonymous by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Post") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (imageUri != null && text.isNotBlank()) {
                        loading = true
                        uploadPost(text, imageUri!!, anonymous) {
                            loading = false
                            navController.popBackStack()
                        }
                    }
                },
                enabled = !loading && imageUri != null && text.isNotBlank()
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Post")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Write a caption...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { launcher.launch("image/*") }) {
                Text("Pick Image")
            }

            imageUri?.let {
                Spacer(modifier = Modifier.height(16.dp))
                AsyncImage(
                    model = it,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = anonymous, onCheckedChange = { anonymous = it })
                Text(text = "Post Anonymously")
            }

            if (loading) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

fun uploadPost(text: String, imageUri: Uri, anonymous: Boolean, onComplete: () -> Unit) {
    val user = Firebase.auth.currentUser
    val username = user?.email ?: "Unknown"
    val userId = user?.uid ?: ""

    val storageRef = Firebase.storage.reference.child("posts/${UUID.randomUUID()}.jpg")
    storageRef.putFile(imageUri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                val post = hashMapOf(
                    "userId" to userId,
                    "username" to username,
                    "imageUrl" to downloadUrl.toString(),
                    "text" to text,
                    "timestamp" to Timestamp.now().seconds,
                    "anonymous" to anonymous,
                    "likes" to emptyList<String>(),
                    "comments" to emptyList<Map<String, Any>>()
                )
                Firebase.firestore.collection("posts").add(post)
                    .addOnSuccessListener { onComplete() }
            }
        }
}
