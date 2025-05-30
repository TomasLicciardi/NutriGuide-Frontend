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
    }    fun login(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            onError("Por favor, completa todos los campos")
            return
        }

        _loading.value = true
        viewModelScope.launch {
            try {
                android.util.Log.d("LoginViewModel", "Iniciando login: ${_email.value}")
                val service = RetrofitInstance.retrofit.create(AuthService::class.java)
                
                try {
                    android.util.Log.d("LoginViewModel", "Enviando solicitud de login")
                    val response = service.login(AuthRequest(_email.value, _password.value))
                    android.util.Log.d("LoginViewModel", "Login exitoso: ${response.accessToken.take(10)}...")
                    
                    // Guardar token
                    TokenManager(context).saveToken(response.accessToken)
                    
                    // Pequeña pausa antes de navegar
                    kotlinx.coroutines.delay(500)
                    
                    // Llamar al callback de éxito
                    onSuccess()                } catch (e: retrofit2.HttpException) {
                    android.util.Log.e("LoginViewModel", "Error HTTP en login: ${e.code()}", e)
                    if (e.code() == 401) {
                        onError("Email o contraseña incorrectos. Por favor, verifica tus datos.")
                    } else {
                        onError("Error del servidor: ${e.message()} (${e.code()})")
                    }
                } catch (e: java.net.SocketTimeoutException) {
                    android.util.Log.e("LoginViewModel", "Timeout en login", e)
                    onError("Tiempo de espera agotado. Inténtalo de nuevo más tarde.")
                } catch (e: java.io.IOException) {
                    android.util.Log.e("LoginViewModel", "Error de IO en login", e)
                    onError("Error de conexión: ${e.message ?: "Verifica tu conexión a internet"}")
                } catch (e: Exception) {
                    android.util.Log.e("LoginViewModel", "Error general en login", e)
                    onError("Email o contraseña incorrectos. Verifica tus credenciales.")
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Error de conexión en login", e)
                onError("Error de conexión: ${e.message ?: "Verifica tu conexión a internet"}")
            } finally {
                _loading.value = false
            }
        }    }
    
    // Método para probar la conexión con el backend
    fun testConnection(context: Context, onResult: (Boolean, String) -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            try {
                com.tesis.nutriguideapp.utils.ConnectionTester.testBackendConnection(
                    context,
                    false, // No mostrar Toast
                    onResult // Pasar el callback de resultado
                )
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Error al probar conexión", e)
                onResult(false, "Error al probar conexión: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }
}
