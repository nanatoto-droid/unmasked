package com.imani.unmasked.data


import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow




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
