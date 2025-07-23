package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.AuthService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.model.RegisterRequest
import com.tesis.nutriguideapp.model.AuthRequest
import com.tesis.nutriguideapp.utils.TokenManager
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _selectedRestrictions = mutableStateOf<List<String>>(emptyList())
    val selectedRestrictions: State<List<String>> = _selectedRestrictions
    private val _availableRestrictions = mutableStateOf(listOf(
        "Sin gluten", "Sin lactosa", "Vegano", 
        "Sin frutos secos"
    ))
    val availableRestrictions: State<List<String>> = _availableRestrictions

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _passwordVisible = mutableStateOf(false)
    val passwordVisible: State<Boolean> = _passwordVisible

    private val _confirmPasswordVisible = mutableStateOf(false)
    val confirmPasswordVisible: State<Boolean> = _confirmPasswordVisible

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun toggleConfirmPasswordVisibility() {
        _confirmPasswordVisible.value = !_confirmPasswordVisible.value
    }

    fun toggleRestriction(restriction: String) {
        _selectedRestrictions.value = if (_selectedRestrictions.value.contains(restriction)) {
            _selectedRestrictions.value - restriction
        } else {
            _selectedRestrictions.value + restriction
        }
    }    fun register(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_username.value.isBlank() || _email.value.isBlank() || _password.value.isBlank()) {
            onError("Por favor, completa todos los campos obligatorios")
            return
        }

        if (_password.value != _confirmPassword.value) {
            onError("Las contraseñas no coinciden")
            return
        }

        if (!isValidEmail(_email.value)) {
            onError("El correo electrónico no es válido")
            return
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                android.util.Log.d("RegisterViewModel", "Iniciando registro: ${_email.value}")
                val service = RetrofitInstance.authRetrofit.create(AuthService::class.java)
                val request = RegisterRequest(
                    username = _username.value,
                    email = _email.value,
                    password = _password.value,
                    restrictions = _selectedRestrictions.value
                )
                try {
                    android.util.Log.d("RegisterViewModel", "Enviando solicitud de registro")
                    val registerResponse = service.register(request)
                    
                    // Usar ApiErrorHandler para procesar la respuesta de registro
                    com.tesis.nutriguideapp.utils.ApiErrorHandler.processResponse(
                        response = registerResponse,
                        tag = "RegisterViewModel",
                        onSuccess = { authResponse ->  // Ahora recibimos directamente el token
                            android.util.Log.d("RegisterViewModel", "Registro exitoso con token")
                            
                            // Guardar token directamente
                            TokenManager(context).saveToken(authResponse.accessToken)
                            
                            // Ir directamente a la aplicación
                            onSuccess()
                        },
                        onError = { errorMessage ->
                            onError(errorMessage)
                        }
                    )
                      } catch (e: Exception) {
                    // Usar nuestro ApiErrorHandler para manejar cualquier tipo de error
                    val errorMessage = com.tesis.nutriguideapp.utils.ApiErrorHandler.handleApiError(e, "RegisterViewModel")
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                val errorMessage = com.tesis.nutriguideapp.utils.ApiErrorHandler.handleApiError(e, "RegisterViewModel")
                onError(errorMessage)
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
