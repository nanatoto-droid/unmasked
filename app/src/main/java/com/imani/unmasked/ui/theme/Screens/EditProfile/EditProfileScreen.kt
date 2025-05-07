package com.imani.unmasked.ui.theme.Screens.EditProfile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController) {
    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var bio by remember { mutableStateOf(TextFieldValue("")) }
    var profileImageUrl by remember { mutableStateOf(TextFieldValue("")) }

    // Load existing profile data
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            Firebase.firestore.collection("users").document(userId).get().addOnSuccessListener { doc ->
                name = TextFieldValue(doc.getString("name") ?: "")
                bio = TextFieldValue(doc.getString("bio") ?: "")
                profileImageUrl = TextFieldValue(doc.getString("profileImageUrl") ?: "")
            }
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

        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)) {

            // Name Field
            Text("Name:")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bio Field
            Text("Bio:")
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Profile Image URL Field
            Text("Profile Image URL:")
            OutlinedTextField(
                value = profileImageUrl,
                onValueChange = { profileImageUrl = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    Firebase.firestore.collection("users").document(userId).update(
                        mapOf(
                            "name" to name.text,
                            "bio" to bio.text,
                            "profileImageUrl" to profileImageUrl.text
                        )
                    )
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}
