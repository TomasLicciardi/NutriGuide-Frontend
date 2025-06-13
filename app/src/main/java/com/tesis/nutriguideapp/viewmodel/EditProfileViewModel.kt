package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.api.UserService
import com.tesis.nutriguideapp.model.PasswordChangeRequest
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {
    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _currentPassword = mutableStateOf("")
    val currentPassword: State<String> = _currentPassword

    private val _newPassword = mutableStateOf("")
    val newPassword: State<String> = _newPassword

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _passwordVisible = mutableStateOf(false)
    val passwordVisible: State<Boolean> = _passwordVisible

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _success = mutableStateOf<String?>(null)
    val success: State<String?> = _success

    fun getUserProfile(context: Context) {
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val service = RetrofitInstance.getAuthenticatedRetrofit(context).create(UserService::class.java)
                val response = service.getUserProfile()
                
                com.tesis.nutriguideapp.utils.ApiErrorHandler.processResponse(
                    response = response,
                    tag = "EditProfileViewModel",
                    onSuccess = { profile ->
                        _username.value = profile.username
                        _email.value = profile.email
                    },
                    onError = { errorMessage ->
                        _error.value = errorMessage
                    }
                )
            } catch (e: Exception) {
                val errorMessage = com.tesis.nutriguideapp.utils.ApiErrorHandler.handleApiError(e, "EditProfileViewModel")
                _error.value = errorMessage
            } finally {
                _loading.value = false
            }
        }
    }

    fun onCurrentPasswordChange(password: String) {
        _currentPassword.value = password
    }

    fun onNewPasswordChange(password: String) {
        _newPassword.value = password
    }

    fun onConfirmPasswordChange(password: String) {
        _confirmPassword.value = password
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun changePassword(context: Context, onSuccess: () -> Unit) {
        if (_currentPassword.value.isBlank() || _newPassword.value.isBlank() || _confirmPassword.value.isBlank()) {
            _error.value = "Por favor, completa todos los campos"
            return
        }

        if (_newPassword.value.length < 6) {
            _error.value = "La nueva contraseña debe tener al menos 6 caracteres"
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
                val service = RetrofitInstance.getAuthenticatedRetrofit(context).create(UserService::class.java)
                val request = PasswordChangeRequest(_currentPassword.value, _newPassword.value)
                val response = service.changePassword(request)
                
                com.tesis.nutriguideapp.utils.ApiErrorHandler.processResponse(
                    response = response,
                    tag = "EditProfileViewModel",
                    onSuccess = { _ ->
                        _success.value = "Contraseña cambiada correctamente"
                        // Limpiar campos
                        _currentPassword.value = ""
                        _newPassword.value = ""
                        _confirmPassword.value = ""
                        onSuccess()
                    },
                    onError = { errorMessage ->
                        // Si es un error 422, dar un mensaje más específico
                        if (response.code() == 422) {
                            _error.value = "Contraseña actual incorrecta"
                        } else {
                            _error.value = errorMessage
                        }
                    }
                )
            } catch (e: Exception) {
                val errorMessage = com.tesis.nutriguideapp.utils.ApiErrorHandler.handleApiError(e, "EditProfileViewModel")
                _error.value = errorMessage
            } finally {
                _loading.value = false
            }
        }
    }
}
