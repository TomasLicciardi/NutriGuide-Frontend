package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.api.UserService
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _restrictions = mutableStateOf<List<String>>(emptyList())
    val restrictions: State<List<String>> = _restrictions

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun getUserProfile(context: Context) {
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val service = RetrofitInstance.getAuthenticatedRetrofit(context).create(UserService::class.java)
                val response = service.getUserProfile()
                
                com.tesis.nutriguideapp.utils.ApiErrorHandler.processResponse(
                    response = response,
                    tag = "ProfileViewModel",
                    onSuccess = { profile ->
                        _username.value = profile.username
                        _email.value = profile.email
                        _restrictions.value = profile.restrictions
                        
                        android.util.Log.d("ProfileViewModel", "Perfil cargado: ${profile.username}, restricciones: ${profile.restrictions.size}")
                    },
                    onError = { errorMessage ->
                        android.util.Log.e("ProfileViewModel", "Error en respuesta: $errorMessage")
                        _error.value = errorMessage
                    }
                )
            } catch (e: Exception) {
                val errorMessage = com.tesis.nutriguideapp.utils.ApiErrorHandler.handleApiError(e, "ProfileViewModel")
                android.util.Log.e("ProfileViewModel", "Error al cargar perfil", e)
                _error.value = errorMessage
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
