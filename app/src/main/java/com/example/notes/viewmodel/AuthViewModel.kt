package com.example.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.AuthRepository
import com.example.notes.data.Partnership
import com.example.notes.data.PartnershipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isPaired: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val partnership: Partnership? = null,
    val currentUserGender: String = "",
    val currentUserName: String = "",
    val pairingCode: String? = null,
    val needsGenderSelection: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val authRepo = AuthRepository()
    private val partnershipRepo = PartnershipRepository()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val user = authRepo.currentUser
        if (user != null) {
            _authState.value = _authState.value.copy(isLoggedIn = true, isLoading = true)
            viewModelScope.launch {
                val profile = authRepo.getUserProfile(user.uid)
                val gender = profile?.get("gender") as? String ?: ""
                val name = profile?.get("name") as? String ?: ""
                val partnership = partnershipRepo.getPartnershipForUser(user.uid)
                val isPaired = partnership != null &&
                        partnership.partner1Uid.isNotEmpty() &&
                        partnership.partner2Uid.isNotEmpty()
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isPaired = isPaired,
                    partnership = partnership,
                    currentUserGender = gender,
                    currentUserName = name
                )
            }
        }
    }

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authRepo.signIn(email, password)
            result.onSuccess { user ->
                val profile = authRepo.getUserProfile(user.uid)
                val gender = profile?.get("gender") as? String ?: ""
                val name = profile?.get("name") as? String ?: ""
                val partnership = partnershipRepo.getPartnershipForUser(user.uid)
                val isPaired = partnership != null &&
                        partnership.partner1Uid.isNotEmpty() &&
                        partnership.partner2Uid.isNotEmpty()
                _authState.value = _authState.value.copy(
                    isLoggedIn = true,
                    isLoading = false,
                    isPaired = isPaired,
                    partnership = partnership,
                    currentUserGender = gender,
                    currentUserName = name
                )
                onSuccess()
            }.onFailure { e ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Sign in failed"
                )
            }
        }
    }

    fun signUp(email: String, password: String, name: String, gender: String, onSuccess: () -> Unit) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authRepo.signUp(email, password, name, gender)
            result.onSuccess {
                _authState.value = _authState.value.copy(
                    isLoggedIn = true,
                    isLoading = false,
                    currentUserGender = gender,
                    currentUserName = name
                )
                onSuccess()
            }.onFailure { e ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Sign up failed"
                )
            }
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = authRepo.signInWithGoogle(idToken)
            result.onSuccess { user ->
                val profile = authRepo.getUserProfile(user.uid)
                val gender = profile?.get("gender") as? String ?: ""
                val name = profile?.get("name") as? String ?: user.displayName ?: ""
                val partnership = partnershipRepo.getPartnershipForUser(user.uid)
                val isPaired = partnership != null &&
                        partnership.partner1Uid.isNotEmpty() &&
                        partnership.partner2Uid.isNotEmpty()
                _authState.value = _authState.value.copy(
                    isLoggedIn = true,
                    isLoading = false,
                    isPaired = isPaired,
                    partnership = partnership,
                    currentUserGender = gender,
                    currentUserName = name,
                    needsGenderSelection = gender.isEmpty()
                )
                if (gender.isNotEmpty()) {
                    onSuccess()
                }
                // If gender is empty, the UI will show the gender selection dialog
            }.onFailure { e ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Google sign-in failed"
                )
            }
        }
    }

    fun completeGenderSelection(name: String, gender: String, onSuccess: () -> Unit) {
        val user = authRepo.currentUser ?: return
        _authState.value = _authState.value.copy(isLoading = true)
        viewModelScope.launch {
            authRepo.updateUserProfile(user.uid, name, gender)
            _authState.value = _authState.value.copy(
                isLoading = false,
                currentUserGender = gender,
                currentUserName = name,
                needsGenderSelection = false
            )
            onSuccess()
        }
    }

    fun generatePairingCode() {
        val user = authRepo.currentUser ?: return
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = partnershipRepo.createPartnership(
                uid = user.uid,
                name = _authState.value.currentUserName,
                gender = _authState.value.currentUserGender
            )
            result.onSuccess { code ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    pairingCode = code
                )
            }.onFailure { e ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to generate code"
                )
            }
        }
    }

    fun joinWithCode(code: String, onSuccess: () -> Unit) {
        val user = authRepo.currentUser ?: return
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = partnershipRepo.joinPartnership(
                uid = user.uid,
                name = _authState.value.currentUserName,
                gender = _authState.value.currentUserGender,
                code = code
            )
            result.onSuccess {
                val partnership = partnershipRepo.getPartnershipForUser(user.uid)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isPaired = true,
                    partnership = partnership
                )
                onSuccess()
            }.onFailure { e ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Invalid code"
                )
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun signOut() {
        authRepo.signOut()
        _authState.value = AuthState()
    }
}
