package com.imani.unmasked.data

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.imani.unmasked.navigation.ROUTE_HOME
import com.imani.unmasked.navigation.ROUTE_LOGIN
import com.imani.unmasked.navigation.ROUTE_REGISTER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch




class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser

    fun signUp(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            _currentUser.value = auth.currentUser
            onResult(it.isSuccessful)
        }
    }

    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            _currentUser.value = auth.currentUser
            onResult(it.isSuccessful)
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }
}
