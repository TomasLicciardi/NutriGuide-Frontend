package com.tesis.nutriguideapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.api.UserService
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _success = mutableStateOf<String?>(null)
    val success: State<String?> = _success

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun requestPasswordReset(onEmailSent: (String) -> Unit) {
        if (_email.value.isBlank()) {
            _error.value = "Por favor, ingresa tu correo electrónico"
            return
        }

        if (!isValidEmail(_email.value)) {
            _error.value = "Correo electrónico inválido"
            return
        }

        _loading.value = true
        _error.value = null
        _success.value = null

        viewModelScope.launch {
            try {
                val service = RetrofitInstance.retrofit.create(UserService::class.java)
                val response = service.forgotPassword(mapOf("email" to _email.value))
                
                if (response.isSuccessful) {
                    _success.value = "Se ha enviado un correo con instrucciones para restablecer tu contraseña"
                    // Normalmente aquí recibiríamos un token de restablecimiento
                    // En una aplicación real, este token llegaría por correo
                    val dummyToken = "reset-token-123456" // Esto es solo para demostración
                    onEmailSent(dummyToken)
                } else {
                    _error.value = "Error al enviar el correo de recuperación"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return email.matches(emailRegex)
    }
}
