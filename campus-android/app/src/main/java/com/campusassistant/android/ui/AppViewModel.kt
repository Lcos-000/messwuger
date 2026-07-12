package com.campusassistant.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.campusassistant.android.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Checking : AuthState
    data object LoggedOut : AuthState
    data object LoggedIn : AuthState
}

data class AppUiState(
    val authState: AuthState = AuthState.Checking,
    val loginLoading: Boolean = false,
    val loginError: String? = null,
    val tokenExists: Boolean = false
)

class AppViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.tokenFlow.collect { token ->
                val loggedIn = token.isNullOrBlank().not()
                _uiState.update {
                    it.copy(
                        authState = if (loggedIn) AuthState.LoggedIn else AuthState.LoggedOut,
                        tokenExists = loggedIn,
                        loginLoading = false,
                        loginError = null
                    )
                }
            }
        }
    }

    fun login(studentId: String, password: String) {
        if (_uiState.value.loginLoading) return
        if (studentId.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(loginError = "请填写学号和密码") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loginLoading = true, loginError = null) }
            authRepository.login(studentId.trim(), password)
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            loginLoading = false,
                            loginError = throwable.message ?: "登录失败"
                        )
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(loginLoading = false, loginError = null) }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                it.copy(
                    loginLoading = false,
                    loginError = null
                )
            }
        }
    }
}

class AppViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            return AppViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
