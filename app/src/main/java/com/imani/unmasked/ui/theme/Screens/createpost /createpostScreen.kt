package com.imani.unmasked.ui.theme.Screens.createpost

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
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
        topBar = { TopAppBar(title = { Text("New Post") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (imageUri != null) {
                        loading = true
                        uploadPost(text, imageUri!!, anonymous) {
                            loading = false
                            navController.popBackStack() // Go back to feed
                        }
                    }
                },
                enabled = !loading
            ) {
                Icon(Icons.Default.Check, contentDescription = "Post")
            }
        }
    ) {
        Column(modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()) {

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
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = anonymous, onCheckedChange = { anonymous = it })
                Text(text = "Post Anonymously")
            }
        }
    }
}

fun uploadPost(text: String, imageUri: Uri, anonymous: Boolean, onComplete: () -> Unit) {
    val user = Firebase.auth.currentUser
    val username = user?.email ?: "Unknown"
    val userId = user?.uid ?: ""

    val storageRef = Firebase.storage.reference.child("posts/${UUID.randomUUID()}.jpg")
    storageRef.putFile(imageUri).addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
            val post = hashMapOf(
                "userId" to userId,
                "username" to username,
                "imageUrl" to downloadUrl.toString(),
                "text" to text,
                "timestamp" to Timestamp.now().seconds,
                "anonymous" to anonymous
            )
            Firebase.firestore.collection("posts").add(post).addOnSuccessListener {
                onComplete()
            }
        }
    }
}
