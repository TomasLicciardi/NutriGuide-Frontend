package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.HistoryService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.model.Product
import com.tesis.nutriguideapp.utils.RestrictionMapper
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

    private val _analysisDetails = MutableStateFlow<Map<String, Any>>(emptyMap())
    val analysisDetails: StateFlow<Map<String, Any>> = _analysisDetails
    
    private val _imageFile = MutableStateFlow<File?>(null)
    val imageFile: StateFlow<File?> = _imageFile

    fun loadProduct(context: Context, productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                android.util.Log.d("ProductDetailVM", "Cargando producto ID: $productId")
                val historyService = RetrofitInstance.getAuthenticatedRetrofit(context).create(HistoryService::class.java)
                
                val productData = historyService.getHistoryProductDetail(productId)
                android.util.Log.d("ProductDetailVM", "Producto obtenido: ${productData.id}")
                _product.value = productData
                
                if (productData.imageUrl != null) {
                    try {
                        val imageResponse = historyService.getProductImage(productId)
                        if (imageResponse.isSuccessful && imageResponse.body() != null) {
                            val imageBytes = imageResponse.body()!!.bytes()
                            val tempFile = File(context.cacheDir, "product_image_$productId.jpg")
                            tempFile.writeBytes(imageBytes)
                            _imageFile.value = tempFile
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ProductDetailVM", "Error descargando imagen: ${e.message}", e)
                    }
                }

                try {
                    val resultMap = mutableMapOf<String, Any>()
                    
                    val ingredients = productData.getIngredientNames()
                    if (ingredients.isNotEmpty()) {
                        resultMap["Ingredientes"] = ingredients
                    }
                    
                    val restrictions = productData.getRestrictions()
                    if (restrictions.isNotEmpty()) {
                        restrictions.forEach { (apiName, restriction) ->
                            val displayName = RestrictionMapper.toDisplayName(apiName)
                            val label = if (restriction.apto) "Apto — $displayName" else "No apto — $displayName"
                            resultMap[label] = !restriction.apto
                        }
                    }
                    
                    _analysisDetails.value = resultMap
                } catch (e: Exception) {
                    android.util.Log.e("ProductDetailVM", "Error analizando detalles: ${e.message}", e)
                    _analysisDetails.value = mapOf(
                        "Error" to "No se pudieron cargar los detalles del análisis"
                    )
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ProductDetailVM", "Error al cargar producto: ${e.message}", e)
                _error.value = "Error al cargar el producto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkSuitabilityForUser(userRestrictions: List<String>) {
        try {
            val product = _product.value ?: return
            
            val isProductSuitable = if (userRestrictions.isEmpty()) {
                true
            } else {
                val productRestrictions = product.getRestrictions()
                if (productRestrictions.isNotEmpty()) {
                    val apiUserRestrictions = RestrictionMapper.toApiNames(userRestrictions)
                    apiUserRestrictions.all { apiName ->
                        productRestrictions[apiName]?.apto ?: true
                    }
                } else {
                    product.isSuitable
                }
            }
            
            val updatedMap = _analysisDetails.value.toMutableMap()
            updatedMap["Apto para usuario"] = isProductSuitable
            _analysisDetails.value = updatedMap
        } catch (e: Exception) {
            android.util.Log.e("ProductDetailVM", "Error verificando compatibilidad: ${e.message}", e)
            val updatedMap = _analysisDetails.value.toMutableMap()
            updatedMap["Apto para usuario"] = userRestrictions.isEmpty()
            _analysisDetails.value = updatedMap
        }
    }
}
