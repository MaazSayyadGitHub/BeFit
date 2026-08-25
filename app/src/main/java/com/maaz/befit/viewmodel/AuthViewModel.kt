package com.maaz.befit.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.maaz.befit.data.database.HealthDatabase
import com.maaz.befit.data.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth : FirebaseAuth,
    private val healthRepository : HealthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        _user.value = firebaseAuth.currentUser
        firebaseAuth.addAuthStateListener { auth ->
            val newUser = auth.currentUser
            _user.value = newUser

            // Handle user authentication state changes
            if (newUser != null) {
                // User signed in - sync local data to Firebase
                viewModelScope.launch {
                    try {
                        healthRepository.syncLocalDataToFirebase()
                    } catch (e: Exception) {
                        // Handle sync error silently
                    }
                }
            } else {
                // User signed out - clear database instance
                HealthDatabase.clearInstance()
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Sign in failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Sign up failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                // Sign out from Firebase (data remains in Firebase for future login)
                firebaseAuth.signOut()

                // Clear database instance to ensure fresh database for next user
                HealthDatabase.clearInstance()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Sign out failed"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}