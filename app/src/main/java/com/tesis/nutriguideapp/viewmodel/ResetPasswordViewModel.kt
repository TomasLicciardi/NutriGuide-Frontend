package com.tesis.nutriguideapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.api.UserService
import com.tesis.nutriguideapp.model.PasswordResetRequest
import kotlinx.coroutines.launch

class ResetPasswordViewModel : ViewModel() {
    private val _token = mutableStateOf("")
    val token: State<String> = _token

    private val _newPassword = mutableStateOf("")
    val newPassword: State<String> = _newPassword

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _passwordVisible = mutableStateOf(false)
    val passwordVisible: State<Boolean> = _passwordVisible

    private val _confirmPasswordVisible = mutableStateOf(false)
    val confirmPasswordVisible: State<Boolean> = _confirmPasswordVisible

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _success = mutableStateOf<String?>(null)
    val success: State<String?> = _success

    fun setToken(token: String) {
        _token.value = token
    }

    fun onNewPasswordChange(newPassword: String) {
        _newPassword.value = newPassword
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun toggleConfirmPasswordVisibility() {
        _confirmPasswordVisible.value = !_confirmPasswordVisible.value
    }

    fun resetPassword(onPasswordReset: () -> Unit) {
        if (_newPassword.value.isBlank()) {
            _error.value = "Por favor, ingresa una contraseña"
            return
        }

        if (_newPassword.value.length < 6) {
            _error.value = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        if (_newPassword.value != _confirmPassword.value) {
            _error.value = "Las contraseñas no coinciden"
            return
        }

        _loading.value = true
        _error.value = null
        _success.value = null

        viewModelScope.launch {
            try {
                val service = RetrofitInstance.retrofit.create(UserService::class.java)
                val request = PasswordResetRequest(_token.value, _newPassword.value)
                val response = service.resetPassword(request)
                
                if (response.isSuccessful) {
                    _success.value = "Contraseña restablecida correctamente"
                    onPasswordReset()
                } else {
                    _error.value = "Error al restablecer la contraseña"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
