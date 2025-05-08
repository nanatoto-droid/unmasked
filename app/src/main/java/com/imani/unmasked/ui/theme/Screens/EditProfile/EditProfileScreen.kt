package com.imani.unmasked.ui.theme.Screens.EditProfile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController) {
    val userId = Firebase.auth.currentUser?.uid ?: ""

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var bio by remember { mutableStateOf(TextFieldValue("")) }
    var profileImageUrl by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Load existing profile data
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            Firebase.firestore.collection("users").document(userId).get().addOnSuccessListener { doc ->
                name = TextFieldValue(doc.getString("name") ?: "")
                bio = TextFieldValue(doc.getString("bio") ?: "")
                profileImageUrl = doc.getString("profileImageUrl") ?: ""
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Profile Image
            val imagePainter = rememberAsyncImagePainter(
                selectedImageUri ?: profileImageUrl
            )
            Image(
                painter = imagePainter,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pick Image Button
            Button(onClick = {
                imagePickerLauncher.launch("image/*")
            }) {
                Text("Pick Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name Field
            Text("Name:")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bio Field
            Text("Bio:")
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (selectedImageUri != null) {
                        isUploading = true
                        // Upload image first
                        uploadImageToFirebase(selectedImageUri!!, userId) { downloadUrl ->
                            profileImageUrl = downloadUrl
                            saveProfile(userId, name.text, bio.text, profileImageUrl)
                            isUploading = false
                            navController.popBackStack()
                        }
                    } else {
                        // No new image selected, just save other fields
                        saveProfile(userId, name.text, bio.text, profileImageUrl)
                        navController.popBackStack()
                    }
                },
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isUploading) "Uploading..." else "Save Changes")
            }
        }
    }
}

fun saveProfile(userId: String, name: String, bio: String, profileImageUrl: String) {
    Firebase.firestore.collection("users").document(userId).update(
        mapOf(
            "name" to name,
            "bio" to bio,
            "profileImageUrl" to profileImageUrl
        )
    )
}

fun uploadImageToFirebase(imageUri: Uri, userId: String, onSuccess: (String) -> Unit) {
    val storageRef = Firebase.storage.reference.child("profile_images/$userId.jpg")
    storageRef.putFile(imageUri)
        .continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageRef.downloadUrl
        }
        .addOnSuccessListener { uri ->
            onSuccess(uri.toString())
        }
}
