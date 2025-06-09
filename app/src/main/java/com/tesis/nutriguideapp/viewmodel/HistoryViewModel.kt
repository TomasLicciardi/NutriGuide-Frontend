package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.HistoryService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.model.HistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val _products = MutableStateFlow<List<HistoryItem>>(emptyList())
    val products: StateFlow<List<HistoryItem>> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var _lastContext: Context? = null

    fun loadHistory(context: Context?) {
        if (context == null && _lastContext == null) {
            _error.value = "Se requiere el contexto para cargar el historial"
            return
        }
        context?.let { _lastContext = it }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val historyService = _lastContext?.let {
                    RetrofitInstance.getAuthenticatedRetrofit(it).create(HistoryService::class.java)
                }
                if (historyService == null) {
                    _error.value = "No se pudo inicializar el servicio de historial"
                    return@launch
                }

                val allProducts = historyService.getUserHistory()
                _products.value = allProducts
            } catch (e: Exception) {
                _error.value = "Error al cargar el historial: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteProduct(productId: Int, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val historyService = _lastContext?.let {
                    RetrofitInstance.getAuthenticatedRetrofit(it).create(HistoryService::class.java)
                }
                if (historyService == null) {
                    onError("No se pudo inicializar el servicio de historial")
                    return@launch
                }

                val response = historyService.deleteHistoryProduct(productId)
                if (response.isSuccessful) {
                    // Actualizar la lista local eliminando el producto
                    _products.value = _products.value.filter { it.id != productId }
                    onSuccess("Producto eliminado exitosamente")
                } else {
                    onError("Error al eliminar el producto: ${response.message()}")
                }
            } catch (e: Exception) {
                onError("Error al eliminar el producto: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAllHistory(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val historyService = _lastContext?.let {
                    RetrofitInstance.getAuthenticatedRetrofit(it).create(HistoryService::class.java)
                }
                if (historyService == null) {
                    onError("No se pudo inicializar el servicio de historial")
                    return@launch
                }

                val response = historyService.clearHistory()
                if (response.isSuccessful) {
                    // Limpiar la lista local
                    _products.value = emptyList()
                    onSuccess("Historial eliminado exitosamente")
                } else {
                    onError("Error al eliminar el historial: ${response.message()}")
                }
            } catch (e: Exception) {
                onError("Error al eliminar el historial: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
