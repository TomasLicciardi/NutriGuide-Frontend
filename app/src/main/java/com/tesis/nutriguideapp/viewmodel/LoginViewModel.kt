package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.AuthService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.model.AuthRequest
import com.tesis.nutriguideapp.utils.TokenManager
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _passwordVisible = mutableStateOf(false)
    val passwordVisible: State<Boolean> = _passwordVisible

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun login(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            onError("Por favor, completa todos los campos")
            return
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                val service = RetrofitInstance.retrofit.create(AuthService::class.java)
                val response = service.login(AuthRequest(_email.value, _password.value))
                TokenManager(context).saveToken(response.accessToken)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error al iniciar sesión. Verifica tus credenciales.")
            } finally {
                _loading.value = false
            }
        }
    }
}
