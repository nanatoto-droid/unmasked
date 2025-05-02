package com.imani.unmasked.ui.theme.Screens.Home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
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
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.imani.unmasked.data.AuthViewModel
import com.imani.unmasked.model.Post
import com.imani.unmasked.model.PostViewModel
import com.imani.unmasked.ui.theme.Screens.login.LoginScreen

@Composable
fun homeScreen(authViewModel: AuthViewModel, postViewModel: PostViewModel, navController: NavHostController) {
    val posts by postViewModel.posts.collectAsState()

    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    selected = true,
                    onClick = { navController.navigate("feed") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Feed") },
                    label = { Text("Feed") }
                )
                BottomNavigationItem(
                    selected = false,
                    onClick = { navController.navigate("create") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Post") },
                    label = { Text("Post") }
                )
                BottomNavigationItem(
                    selected = false,
                    onClick = { navController.navigate("profile") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(posts) { post ->
                PostItem(post = post)
            }
        }
    }
}


@Composable
fun homepage() {
    homeScreen(rememberNavController())
}

fun PostItem(post: Post) {
    Card(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = if (post.anonymous) "Anonymous" else post.username,
                style = MaterialTheme.typography
            )
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
        }
    }
}
