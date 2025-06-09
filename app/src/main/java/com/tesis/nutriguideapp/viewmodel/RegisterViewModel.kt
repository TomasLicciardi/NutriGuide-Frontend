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
        "Sin gluten", "Sin lactosa", "Vegano", "Vegetariano", 
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
                val service = RetrofitInstance.retrofit.create(AuthService::class.java)
                val request = RegisterRequest(
                    username = _username.value,
                    email = _email.value,
                    password = _password.value,
                    restrictions = _selectedRestrictions.value
                )
                try {
                    android.util.Log.d("RegisterViewModel", "Enviando solicitud de registro")
                    val registerResponse = service.register(request)
                    android.util.Log.d("RegisterViewModel", "Registro exitoso: $registerResponse")
                    
                    // Inmediatamente después del registro exitoso, hacer login automático
                    try {
                        android.util.Log.d("RegisterViewModel", "Iniciando login automático")
                        val loginResponse = service.login(AuthRequest(_email.value, _password.value))
                        android.util.Log.d("RegisterViewModel", "Login automático exitoso")
                        
                        // Guardar token
                        TokenManager(context).saveToken(loginResponse.accessToken)
                        
                        // Ir directamente a la aplicación
                        onSuccess()
                    } catch (e: Exception) {
                        android.util.Log.e("RegisterViewModel", "Error en login automático", e)
                        onError("Registro exitoso, pero error al iniciar sesión automáticamente. Por favor, inicia sesión manualmente.")
                    }
                    
                } catch (e: retrofit2.HttpException) {
                    // Captura específicamente errores HTTP
                    android.util.Log.e("RegisterViewModel", "Error HTTP: ${e.code()}", e)
                    if (e.code() == 409) {
                        onError("El correo electrónico ya está registrado. Por favor, usa otro correo.")
                    } else {
                        onError("Error del servidor: ${e.message()} (${e.code()})")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RegisterViewModel", "Error general en registro", e)
                    onError("Error: ${e.message ?: "Error al registrar. Inténtalo de nuevo."}")
                }
            } catch (e: Exception) {
                android.util.Log.e("RegisterViewModel", "Error de conexión", e)
                onError("Error de conexión: ${e.message ?: "Verifica tu conexión a internet"}")
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
