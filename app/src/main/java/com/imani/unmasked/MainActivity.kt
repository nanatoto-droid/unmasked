package com.imani.unmasked

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.imani.unmasked.data.AuthViewModel
import com.imani.unmasked.model.PostViewModel
import com.imani.unmasked.ui.theme.Screens.Profile.ProfileScreen
import com.imani.unmasked.ui.theme.Screens.createpost.CreatePostScreen
import com.imani.unmasked.ui.theme.Screens.feed.FeedScreen
import com.imani.unmasked.ui.theme.Screens.login.SignInScreen
import com.imani.unmasked.ui.theme.Screens.register.SignUpScreen
import com.imani.unmasked.ui.theme.Screens.EditProfile.EditProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val navController = rememberNavController()

            // 🔥 Use currentUser instead of userState
            val user = authViewModel.currentUser.collectAsState().value

            val postViewModel = PostViewModel()

            NavHost(
                navController = navController,
                startDestination = if (user != null) "feed" else "auth"
            ) {
                navigation(startDestination = "signin", route = "auth") {
                    composable("signin") { SignInScreen(authViewModel, navController) }
                    composable("signup") { SignUpScreen(authViewModel, navController) }
                }

                composable("feed") { FeedScreen(authViewModel, postViewModel, navController) }
                composable("create") { CreatePostScreen(authViewModel, navController) }
                composable("profile") { ProfileScreen(authViewModel, navController) }
                composable("edit_profile") { EditProfileScreen(navController) }
            }
        }
    }
}
