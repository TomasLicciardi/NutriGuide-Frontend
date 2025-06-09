package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.HistoryService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductDetailViewModel : ViewModel() {
    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Mapa para almacenar detalles analizados del resultado JSON
    private val _analysisDetails = MutableStateFlow<Map<String, Any>>(emptyMap())
    val analysisDetails: StateFlow<Map<String, Any>> = _analysisDetails

    fun loadProduct(context: Context, productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val historyService = RetrofitInstance.getAuthenticatedRetrofit(context).create(HistoryService::class.java)
                val productData = historyService.getHistoryProductDetail(productId)
                _product.value = productData
                
                // Analizar el resultJson para extraer información estructurada
                try {
                    // Usar los métodos del modelo Product para extraer datos
                    val resultMap = mutableMapOf<String, Any>()
                    
                    // Extraer información usando los métodos auxiliares de Product
                    resultMap["Ingredientes"] = productData.getIngredients()
                    resultMap["Restricciones Detectadas"] = productData.getRestrictionsDetected()
                    resultMap["Texto Detectado"] = productData.getTextDetected()
                      // Analizar restricciones específicas
                    val restrictions = productData.getRestrictionsDetected()
                    // Las restricciones vienen como un mapa, donde la clave es el nombre de la restricción
                    productData.resultJson.clasificacion.forEach { (restrictionName, restriction) ->
                        val isRestricted = !restriction.apto
                        resultMap["Contiene $restrictionName"] = isRestricted
                    }
                    
                    _analysisDetails.value = resultMap
                } catch (e: Exception) {
                    _error.value = "Error al analizar detalles del producto: ${e.message}"
                }
                
            } catch (e: Exception) {
                _error.value = "Error al cargar el producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }    // Método para verificar si un producto es adecuado para el usuario basado en sus restricciones
    fun checkSuitabilityForUser(userRestrictions: List<String>) {
        val product = _product.value ?: return
        val detectedRestrictions = product.getRestrictionsDetected()
        
        // Verificar si hay alguna restricción del usuario que no sea apta en el producto
        // Como no tenemos el nombre de la restricción directamente, verificamos si hay restricciones no aptas
        val isProductSuitable = detectedRestrictions.all { restriction ->
            restriction.apto
        }
        
        // Actualizar el mapa de análisis con esta información
        val updatedMap = _analysisDetails.value.toMutableMap()
        updatedMap["Apto para usuario"] = isProductSuitable
        _analysisDetails.value = updatedMap
    }
}
