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
import java.io.File

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
    
    // Para manejar la imagen descargada
    private val _imageFile = MutableStateFlow<File?>(null)
    val imageFile: StateFlow<File?> = _imageFile
    fun loadProduct(context: Context, productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                android.util.Log.d("ProductDetailViewModel", "Iniciando carga del producto ID: $productId")
                val historyService = RetrofitInstance.getAuthenticatedRetrofit(context).create(HistoryService::class.java)
                
                android.util.Log.d("ProductDetailViewModel", "Obteniendo detalles del producto...")
                val productData = historyService.getHistoryProductDetail(productId)
                android.util.Log.d("ProductDetailViewModel", "Producto obtenido exitosamente: ${productData.id}")
                _product.value = productData
                
                // Intentar descargar la imagen si existe
                if (productData.imageUrl != null) {
                    try {
                        android.util.Log.d("ProductDetailViewModel", "Descargando imagen desde: ${productData.imageUrl}")
                        val imageResponse = historyService.getProductImage(productId)
                        if (imageResponse.isSuccessful && imageResponse.body() != null) {
                            // Guardar la imagen en un archivo temporal
                            val imageBytes = imageResponse.body()!!.bytes()
                            val tempFile = File(context.cacheDir, "product_image_$productId.jpg")
                            tempFile.writeBytes(imageBytes)
                            _imageFile.value = tempFile
                            android.util.Log.d("ProductDetailViewModel", "Imagen descargada exitosamente: ${tempFile.absolutePath}")
                        } else {
                            android.util.Log.e("ProductDetailViewModel", "Error al descargar imagen: ${imageResponse.code()} - ${imageResponse.message()}")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ProductDetailViewModel", "Error descargando imagen: ${e.message}", e)
                        // No fallar si no se puede descargar la imagen
                    }
                } else {
                    android.util.Log.d("ProductDetailViewModel", "No hay imagen URL para este producto")
                }
                  // Analizar el resultJson para extraer información estructurada
                try {
                    android.util.Log.d("ProductDetailViewModel", "Analizando datos del producto...")
                    // Usar los métodos del modelo Product para extraer datos
                    val resultMap = mutableMapOf<String, Any>()
                    
                    try {
                        // Extraer información usando los métodos auxiliares de Product
                        val ingredientes = productData.getIngredients()
                        resultMap["Ingredientes"] = ingredientes
                        android.util.Log.d("ProductDetailViewModel", "Ingredientes procesados: ${ingredientes.size}")
                    } catch (e: Exception) {
                        android.util.Log.e("ProductDetailViewModel", "Error al procesar ingredientes: ${e.message}", e)
                        resultMap["Error en ingredientes"] = true
                    }
                      try {
                        val restricciones = productData.getRestrictionsDetected()
                        resultMap["Restricciones Detectadas"] = restricciones
                        android.util.Log.d("ProductDetailViewModel", "Restricciones procesadas: ${restricciones.size}")
                    } catch (e: Exception) {
                        android.util.Log.e("ProductDetailViewModel", "Error al procesar restricciones: ${e.message}", e)
                        resultMap["Error en restricciones"] = true
                    }
                    
                    // Analizar restricciones específicas
                    try {
                        // Verificar que clasificacion no sea null
                        if (productData.resultJson.clasificacion.isNotEmpty()) {
                            android.util.Log.d("ProductDetailViewModel", "Analizando clasificaciones: ${productData.resultJson.clasificacion.keys}")
                            productData.resultJson.clasificacion.forEach { (restrictionName, restriction) ->
                                try {
                                    val isRestricted = !restriction.apto
                                    resultMap["Contiene $restrictionName"] = isRestricted
                                } catch (e: Exception) {
                                    android.util.Log.e("ProductDetailViewModel", "Error al procesar restricción $restrictionName: ${e.message}", e)
                                }
                            }
                        } else {
                            android.util.Log.w("ProductDetailViewModel", "No hay clasificaciones disponibles en el producto")
                            resultMap["Sin restricciones analizadas"] = true
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ProductDetailViewModel", "Error al procesar clasificaciones: ${e.message}", e)
                        resultMap["Error al procesar clasificaciones"] = true
                    }
                    
                    _analysisDetails.value = resultMap
                    android.util.Log.d("ProductDetailViewModel", "Análisis de datos completado con ${resultMap.size} elementos")
                } catch (e: Exception) {
                    android.util.Log.e("ProductDetailViewModel", "Error al analizar detalles del producto: ${e.message}", e)
                    _error.value = "Error al analizar detalles del producto: ${e.message}"
                    // Agregar un análisis mínimo para evitar pantalla en blanco
                    _analysisDetails.value = mapOf(
                        "Error" to "No se pudieron cargar los detalles del análisis",
                        "Mensaje" to (e.message ?: "Error desconocido")
                    )
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ProductDetailViewModel", "Error al cargar el producto: ${e.message}", e)
                _error.value = "Error al cargar el producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }    // Método para verificar si un producto es adecuado para el usuario basado en sus restricciones
    fun checkSuitabilityForUser(userRestrictions: List<String>) {
        try {
            android.util.Log.d("ProductDetailViewModel", "Verificando compatibilidad con restricciones del usuario: $userRestrictions")
            val product = _product.value
            if (product == null) {
                android.util.Log.e("ProductDetailViewModel", "No hay producto para verificar compatibilidad")
                return
            }
            
            // Si el usuario no tiene restricciones, el producto es automáticamente apto
            val isProductSuitable = if (userRestrictions.isEmpty()) {
                android.util.Log.d("ProductDetailViewModel", "Usuario sin restricciones: producto automáticamente apto")
                true
            } else {
                try {
                    val detectedRestrictions = product.getRestrictionsDetected()
                    android.util.Log.d("ProductDetailViewModel", "Restricciones detectadas: ${detectedRestrictions.size}")
                    
                    // Verificar si hay alguna restricción del usuario que no sea apta en el producto
                    if (detectedRestrictions.isNotEmpty()) {
                        val suitable = detectedRestrictions.all { restriction ->
                            restriction.apto
                        }
                        android.util.Log.d("ProductDetailViewModel", "Producto es apto: $suitable")
                        suitable
                    } else {
                        android.util.Log.w("ProductDetailViewModel", "No hay restricciones detectadas, se considera apto por defecto")
                        true
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ProductDetailViewModel", "Error al verificar restricciones: ${e.message}", e)
                    // Si el usuario no tiene restricciones, asumir que es apto
                    true
                }
            }
            
            // Actualizar el mapa de análisis con esta información
            val updatedMap = _analysisDetails.value.toMutableMap()
            updatedMap["Apto para usuario"] = isProductSuitable
            _analysisDetails.value = updatedMap
            
            android.util.Log.d("ProductDetailViewModel", "Verificación de compatibilidad completada: $isProductSuitable")
        } catch (e: Exception) {
            android.util.Log.e("ProductDetailViewModel", "Error general al verificar compatibilidad: ${e.message}", e)
            // Si hay error pero el usuario no tiene restricciones, considerarlo apto
            val updatedMap = _analysisDetails.value.toMutableMap()
            updatedMap["Apto para usuario"] = userRestrictions.isEmpty()
            updatedMap["Error en verificación"] = !userRestrictions.isEmpty()
            _analysisDetails.value = updatedMap
        }
    }
}
