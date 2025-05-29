package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.ProductService
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
                val productService = RetrofitInstance.getAuthenticatedRetrofit(context).create(ProductService::class.java)
                val productData = productService.getProductById(productId)
                _product.value = productData
                
                // Analizar el resultJson para extraer información estructurada
                try {
                    // Usar los métodos del modelo Product para extraer datos
                    val resultMap = mutableMapOf<String, Any>()
                    
                    // Extraer información usando los métodos auxiliares de Product
                    resultMap["Ingredientes"] = productData.getIngredients()
                    resultMap["Restricciones Detectadas"] = productData.getRestrictionsDetected()
                    resultMap["Texto Detectado"] = productData.getTextDetected()
                    
                    // Mantener compatibilidad con el código existente
                    productData.resultJson.let { json ->
                        if (json.contains("gluten", ignoreCase = true)) {
                            resultMap["Contiene Gluten"] = true
                        }
                        if (json.contains("lactosa", ignoreCase = true)) {
                            resultMap["Contiene Lactosa"] = true
                        }
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
        
        // Verificar si hay alguna restricción del usuario en los ingredientes detectados
        val isProductSuitable = detectedRestrictions.none { detectedRestriction ->
            userRestrictions.any { userRestriction ->
                detectedRestriction.contains(userRestriction, ignoreCase = true)
            }
        }
        
        // Actualizar el mapa de análisis con esta información
        val updatedMap = _analysisDetails.value.toMutableMap()
        updatedMap["Apto para usuario"] = isProductSuitable
        _analysisDetails.value = updatedMap
    }
}
