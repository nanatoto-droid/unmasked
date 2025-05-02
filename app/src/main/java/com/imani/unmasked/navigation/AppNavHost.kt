package com.imani.unmasked.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.imani.unmasked.ui.theme.Screens.Home.homeScreen
import com.imani.unmasked.ui.theme.Screens.login.LoginScreen
import com.imani.unmasked.ui.theme.Screens.register.RegisterScreen

@Composable
fun AppNavHost(modifier: Modifier=Modifier,navController:NavHostController= rememberNavController(),startDestination:String= ROUTE_LOGIN) {

    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination
    ) {
        composable(ROUTE_LOGIN) {
            LoginScreen(navController)
        }
        composable(ROUTE_REGISTER) {
            RegisterScreen(navController)
        }
        composable (ROUTE_HOME ){
            homeScreen(navController)
        }


    }
}