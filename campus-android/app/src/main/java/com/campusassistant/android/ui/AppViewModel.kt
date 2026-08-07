package com.campusassistant.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.campusassistant.android.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthState {
    Checking,
    LoggedOut,
    LoggedIn
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
            val hasToken = authRepository.hasToken()
            _uiState.update {
                it.copy(
                    authState = if (hasToken) AuthState.LoggedIn else AuthState.LoggedOut,
                    tokenExists = hasToken
                )
            }
        }
    }

    fun login(studentId: String, password: String) {
        if (_uiState.value.loginLoading) return
        if (studentId.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(loginError = "请输入学号和密码") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loginLoading = true, loginError = null) }
            authRepository.loginOrRegister(studentId.trim(), password)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            authState = AuthState.LoggedIn,
                            loginLoading = false,
                            loginError = null,
                            tokenExists = true
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            authState = AuthState.LoggedOut,
                            loginLoading = false,
                            loginError = throwable.message ?: "登录失败",
                            tokenExists = false
                        )
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                it.copy(
                    authState = AuthState.LoggedOut,
                    loginLoading = false,
                    tokenExists = false
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
