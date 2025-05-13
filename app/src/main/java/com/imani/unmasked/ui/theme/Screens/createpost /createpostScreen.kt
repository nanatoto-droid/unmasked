package com.imani.unmasked.ui.theme.Screens.createpost

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(authViewModel: AuthViewModel, navController: NavHostController) {
    var text by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var anonymous by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0.0) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Post") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("feed") {
                            popUpTo("profile") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to feed")
                    }
                })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!loading && text.isNotBlank()) {
                        loading = true
                        uploadPost(
                            text = text,
                            imageUri = imageUri,
                            anonymous = anonymous,
                            onProgress = { progress -> uploadProgress = progress },
                            onComplete = {
                                loading = false
                                navController.popBackStack()
                            }
                        )
                    }
                },
                containerColor = if (!loading && text.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Post"
                )
            }
        }
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(id = R.drawable.bg1),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {

                    Column(modifier = Modifier.padding(16.dp)) {

                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text("Write your post...") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Pick Image (Optional)")
                        }

                        imageUri?.let {
                            Spacer(modifier = Modifier.height(16.dp))
                            AsyncImage(
                                model = it,
                                contentDescription = "Selected image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { imageUri = null },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Image",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Remove Image", color = Color.White)
                            }
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
                            LinearProgressIndicator(
                                progress = uploadProgress.toFloat() / 100,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(text = "Uploading: ${uploadProgress.toInt()}%")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Tip: You can post only text without picking an image!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

fun uploadPost(
    text: String,
    imageUri: Uri?,
    anonymous: Boolean,
    onProgress: (Double) -> Unit,
    onComplete: () -> Unit
) {
    val user = Firebase.auth.currentUser
    val username = user?.email ?: "Unknown"
    val userId = user?.uid ?: ""

    if (imageUri != null) {
        val storageRef = Firebase.storage.reference.child("posts/${UUID.randomUUID()}.jpg")
        val uploadTask = storageRef.putFile(imageUri)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
            onProgress(progress)
        }.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                savePostToFirestore(
                    userId, username, text, downloadUrl.toString(), anonymous, onComplete
                )
            }
        }
    } else {
        savePostToFirestore(userId, username, text, null, anonymous, onComplete)
    }
}

private fun savePostToFirestore(
    userId: String,
    username: String,
    text: String,
    imageUrl: String?,
    anonymous: Boolean,
    onComplete: () -> Unit
) {
    val post = hashMapOf(
        "userId" to userId,
        "username" to username,
        "imageUrl" to imageUrl,
        "text" to text,
        "timestamp" to System.currentTimeMillis() / 1000,
        "anonymous" to anonymous,
        "likes" to emptyList<String>(),
        "comments" to emptyList<Map<String, Any>>()
    )
    val postsRef = Firebase.firestore.collection("posts")
    postsRef.add(post).addOnSuccessListener { docRef ->
        docRef.update("id", docRef.id)
        onComplete()
    }
}
