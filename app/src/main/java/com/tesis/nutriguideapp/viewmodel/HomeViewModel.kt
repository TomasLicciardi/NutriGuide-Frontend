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
                val service = RetrofitInstance.getAuthenticatedRetrofit(context).create(UserService::class.java)
                val profile = service.getUserProfile()
                _username.value = profile.username
                
                // Obtenemos también las restricciones
                val restrictions = service.getUserRestrictions()
                _userRestrictions.value = restrictions
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout(context: Context, onLogoutSuccess: () -> Unit) {
        TokenManager(context).clearToken()
        onLogoutSuccess()
    }
}
