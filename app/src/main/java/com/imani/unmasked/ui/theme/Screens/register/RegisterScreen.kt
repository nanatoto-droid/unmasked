package com.imani.unmasked.ui.theme.Screens.register


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Button

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier

import androidx.compose.ui.text.input.PasswordVisualTransformation

import androidx.compose.ui.unit.dp

import androidx.navigation.NavHostController

import com.imani.unmasked.data.AuthViewModel


@Composable
fun SignUpScreen(viewModel: AuthViewModel, navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Sign Up",
            style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier
            .height(16.dp))

        OutlinedTextField(value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation())

        Spacer(modifier = Modifier
            .height(16.dp))
        Button(onClick = {
            viewModel.signUp(email, password) { success ->
                if (success) navController.navigate("feed") { popUpTo("auth") { inclusive = true } }
            }
        }, modifier = Modifier
            .fillMaxWidth()) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { navController.navigate("signin") }) {
            Text("Already have an account? Sign In")
        }
    }
}
