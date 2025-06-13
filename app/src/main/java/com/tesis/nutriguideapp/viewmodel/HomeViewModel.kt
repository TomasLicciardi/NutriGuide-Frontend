package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.UserService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.utils.TokenManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

class HomeViewModel : ViewModel() {

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _userRestrictions = mutableStateOf<List<String>>(emptyList())
    val userRestrictions: State<List<String>> = _userRestrictions

    fun getUserProfile(context: Context) {
        _loading.value = true
        viewModelScope.launch {
            try {
                android.util.Log.d("HomeViewModel", "Obteniendo perfil de usuario")
                val service = RetrofitInstance.getAuthenticatedRetrofit(context).create(UserService::class.java)

                try {
                    android.util.Log.d("HomeViewModel", "Consultando datos de perfil")
                    val profileResponse = service.getUserProfile()
                    
                    com.tesis.nutriguideapp.utils.ApiErrorHandler.processResponse(
                        response = profileResponse,
                        tag = "HomeViewModel",
                        onSuccess = { profile ->
                            _username.value = profile.username
                            android.util.Log.d("HomeViewModel", "Perfil obtenido: ${profile.username}")
                            
                            // Obtener también las restricciones
                            loadUserRestrictions(service)
                        },
                        onError = { errorMessage ->
                            android.util.Log.e("HomeViewModel", "Error al obtener perfil: $errorMessage")
                            // Verificar si es un error de autenticación
                            if (profileResponse.code() == 401) {
                                android.util.Log.w("HomeViewModel", "Token inválido, se limpiará la sesión")
                                TokenManager(context).clearToken()
                            }
                        }
                    )
                } catch (e: Exception) {
                    val errorMessage = com.tesis.nutriguideapp.utils.ApiErrorHandler.handleApiError(e, "HomeViewModel")
                    android.util.Log.e("HomeViewModel", "Error al obtener perfil: $errorMessage", e)
                    
                    // Si es un error de autenticación de la excepción HTTP
                    if (e is HttpException && e.code() == 401) {
                        android.util.Log.w("HomeViewModel", "Token inválido, se limpiará la sesión")
                        TokenManager(context).clearToken()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HomeViewModel", "Error general al obtener perfil", e)
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error de conexión al obtener perfil", e)
            } finally {
                _loading.value = false
            }
        }
    }

    private fun loadUserRestrictions(service: UserService) {
        viewModelScope.launch {
            try {
                android.util.Log.d("HomeViewModel", "Consultando restricciones del usuario")
                val restrictionsResponse = service.getUserRestrictions()
                
                com.tesis.nutriguideapp.utils.ApiErrorHandler.processResponse(
                    response = restrictionsResponse,
                    tag = "HomeViewModel/Restrictions",
                    onSuccess = { restrictions ->
                        _userRestrictions.value = restrictions ?: emptyList()
                        android.util.Log.d("HomeViewModel", "Restricciones obtenidas: ${restrictions?.size ?: 0}")
                    },
                    onError = { errorMessage ->
                        android.util.Log.e("HomeViewModel", "Error al obtener restricciones: $errorMessage")
                        _userRestrictions.value = emptyList()
                    }
                )
            } catch (e: Exception) {
                val errorMessage = com.tesis.nutriguideapp.utils.ApiErrorHandler.handleApiError(e, "HomeViewModel/Restrictions")
                android.util.Log.e("HomeViewModel", "Error al obtener restricciones: $errorMessage", e)
                _userRestrictions.value = emptyList()
            }
        }
    }
    
    fun logout(context: Context, onLogoutSuccess: () -> Unit) {
        try {
            android.util.Log.d("HomeViewModel", "Iniciando proceso de logout")
            TokenManager(context).clearToken()
            android.util.Log.d("HomeViewModel", "Token limpiado correctamente")

            viewModelScope.launch {
                try {
                    delay(300)
                    android.util.Log.d("HomeViewModel", "Llamando al callback de éxito del logout")
                    onLogoutSuccess()
                } catch (e: Exception) {
                    android.util.Log.e("HomeViewModel", "Error en callback de logout", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeViewModel", "Error al limpiar token", e)
        }
    }
}
