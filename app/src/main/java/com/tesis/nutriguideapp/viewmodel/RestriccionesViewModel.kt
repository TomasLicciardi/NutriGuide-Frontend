package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.api.UserService
import com.tesis.nutriguideapp.model.UserRestrictionsRequest
import kotlinx.coroutines.launch

class RestriccionesViewModel : ViewModel() {
    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _userRestrictions = mutableStateOf<Set<String>>(emptySet())
    val userRestrictions: State<Set<String>> = _userRestrictions
    private val _availableRestrictions = mutableStateOf(listOf(
        "Sin gluten", "Sin lactosa", "Vegano", "Vegetariano", 
        "Sin frutos secos"
    ))
    val availableRestrictions: State<List<String>> = _availableRestrictions

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _success = mutableStateOf<String?>(null)
    val success: State<String?> = _success

    fun getUserRestrictions(context: Context) {
        _loading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                val service = RetrofitInstance.getAuthenticatedRetrofit(context).create(UserService::class.java)
                val restrictions = service.getUserRestrictions()
                _userRestrictions.value = restrictions.toSet()
            } catch (e: Exception) {
                _error.value = "Error al cargar restricciones: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleRestriction(restriction: String) {
        _userRestrictions.value = if (_userRestrictions.value.contains(restriction)) {
            _userRestrictions.value - restriction
        } else {
            _userRestrictions.value + restriction
        }
    }

    fun saveRestrictions(context: Context, onSuccess: () -> Unit) {
        _loading.value = true
        _error.value = null
        _success.value = null
        
        viewModelScope.launch {
            try {
                val service = RetrofitInstance.getAuthenticatedRetrofit(context).create(UserService::class.java)
                val request = UserRestrictionsRequest(_userRestrictions.value.toList())
                val response = service.updateUserRestrictions(request)
                
                if (response.isSuccessful) {
                    _success.value = "Restricciones guardadas correctamente"
                    onSuccess()
                } else {
                    _error.value = "Error al guardar: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error al guardar restricciones: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
