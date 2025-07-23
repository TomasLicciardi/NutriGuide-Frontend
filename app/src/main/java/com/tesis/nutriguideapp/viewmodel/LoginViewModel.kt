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
import java.io.IOException
import okio.Buffer

class LoginViewModel : ViewModel() {
    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _passwordVisible = mutableStateOf(false)
    val passwordVisible: State<Boolean> = _passwordVisible

    private var _loginProcessed = false

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
                android.util.Log.d("LoginViewModel", "Iniciando login: ${_email.value}")
                try {
                    val service = RetrofitInstance.authRetrofit.create(AuthService::class.java)

                    try {
                        android.util.Log.d("LoginViewModel", "Enviando solicitud de login")
                        val request = AuthRequest(_email.value, _password.value)
                        val response = service.login(request)

                        if (response.isSuccessful && response.body() != null) {
                            try {
                                val authResponse = response.body()!!
                                android.util.Log.d("LoginViewModel", "Login exitoso: ${authResponse.accessToken?.take(10) ?: ""}...")

                                TokenManager(context).saveToken(authResponse.accessToken)
                                onSuccess()
                            } catch (e: IllegalStateException) {
                                android.util.Log.e("LoginViewModel", "Error al procesar respuesta exitosa", e)

                                if (e.message?.contains("closed") == true) {
                                    val tokenManager = TokenManager(context)
                                    if (tokenManager.getToken()?.isNotEmpty() == true) {
                                        android.util.Log.d("LoginViewModel", "Login exitoso a pesar del error 'closed'")
                                        onSuccess()
                                    } else {
                                        onError("Error al procesar la respuesta del servidor. Por favor, intenta de nuevo.")
                                    }
                                } else {
                                    onError("Error interno: ${e.message}")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("LoginViewModel", "Error general al procesar respuesta exitosa", e)
                                onError("Error al procesar la respuesta: ${e.message}")
                            }
                        } else {
                            val errorCode = response.code()
                            val errorMsg = when (errorCode) {
                                401 -> {
                                    android.util.Log.d("LoginViewModel", "Error de autenticación (401)")
                                    "Email o contraseña incorrectos. Por favor, verifica tus datos."
                                }
                                422 -> {
                                    android.util.Log.d("LoginViewModel", "Error de validación (422)")
                                    "Datos de entrada inválidos. Por favor, verifica los campos."
                                }
                                else -> {
                                    try {
                                        val errorBody = try {
                                            val source = response.errorBody()?.source()
                                            if (source != null) {
                                                val buffer = Buffer()
                                                buffer.writeAll(source)
                                                buffer.readUtf8()
                                            } else {
                                                null
                                            }
                                        } catch (ex: Exception) {
                                            android.util.Log.e("LoginViewModel", "Error al leer errorBody", ex)
                                            null
                                        }

                                        val extractedMsg = com.tesis.nutriguideapp.utils.ApiErrorHandler.parseErrorBody(errorBody)
                                        extractedMsg ?: com.tesis.nutriguideapp.utils.ApiErrorHandler.getHttpErrorMessage(errorCode, response.message())
                                    } catch (e: Exception) {
                                        android.util.Log.e("LoginViewModel", "Error al procesar cuerpo de error", e)
                                        com.tesis.nutriguideapp.utils.ApiErrorHandler.getHttpErrorMessage(errorCode, response.message())
                                    }
                                }
                            }

                            android.util.Log.e("LoginViewModel", "Error en respuesta: Código $errorCode, Mensaje: $errorMsg")
                            onError(errorMsg)
                        }
                    } catch (e: IllegalStateException) {
                        android.util.Log.e("LoginViewModel", "IllegalStateException en login", e)

                        val tokenManager = TokenManager(context)
                        if (e.message?.contains("closed") == true && tokenManager.getToken()?.isNotEmpty() == true) {
                            android.util.Log.d("LoginViewModel", "Token ya guardado, procediendo con login exitoso")
                            onSuccess()
                        } else if (e.message?.contains("closed") == true) {
                            onError("Se produjo un error de conexión. Por favor, intenta de nuevo.")
                        } else {
                            onError("Error interno: ${e.message}")
                        }
                    } catch (e: IOException) {
                        android.util.Log.e("LoginViewModel", "Error de IO en login", e)
                        onError("Error de conexión. Verifica tu conexión a internet.")
                    } catch (e: Exception) {
                        android.util.Log.e("LoginViewModel", "Error en login", e)
                        val errorMessage = com.tesis.nutriguideapp.utils.ApiErrorHandler.handleApiError(e, "LoginViewModel")
                        onError(errorMessage)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LoginViewModel", "Error al crear servicio", e)
                    val errorMessage = com.tesis.nutriguideapp.utils.ApiErrorHandler.handleApiError(e, "LoginViewModel")
                    onError(errorMessage)
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Error general en login", e)
                val errorMessage = com.tesis.nutriguideapp.utils.ApiErrorHandler.handleApiError(e, "LoginViewModel")
                onError(errorMessage)
            } finally {
                _loading.value = false
            }
        }
    }

    fun testConnection(context: Context, onResult: (Boolean, String) -> Unit) {
        _loading.value = true
        viewModelScope.launch {
            try {
                com.tesis.nutriguideapp.utils.ConnectionTester.testBackendConnection(
                    context,
                    false,
                    onResult
                )
            } catch (e: Exception) {
                android.util.Log.e("LoginViewModel", "Error al probar conexión", e)
                onResult(false, "Error al probar conexión: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun testErrorHandling(errorType: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val errorMessage = com.tesis.nutriguideapp.utils.ApiErrorHandler.testErrorHandling(errorType)
                onError(errorMessage)
            } catch (e: Exception) {
                val errorMessage = "Error al probar manejo de errores: ${e.message}"
                android.util.Log.e("LoginViewModel", errorMessage, e)
                onError(errorMessage)
            }
        }
    }

    private fun isSuccessfulLoginResponse(response: retrofit2.Response<*>): Boolean {
        return response.isSuccessful && response.code() == 200
    }

    private fun processSuccessfulLogin(context: Context, token: String?): Boolean {
        if (token != null && token.isNotEmpty()) {
            android.util.Log.d("LoginViewModel", "Guardando token: ${token.take(10)}...")
            TokenManager(context).saveToken(token)
            _loginProcessed = true
            return true
        }
        return false
    }
}
