package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.AnalysisService
import com.tesis.nutriguideapp.api.ProductService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.model.AnalysisResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*

class UploadViewModel : ViewModel() {
    private val _imageUri = mutableStateOf<Uri?>(null)
    val imageUri: State<Uri?> = _imageUri

    private val _productName = mutableStateOf("")
    val productName: State<String> = _productName

    private val _analyzing = mutableStateOf(false)
    val analyzing: State<Boolean> = _analyzing

    private val _uploading = mutableStateOf(false)
    val uploading: State<Boolean> = _uploading

    private val _analysisResponse = mutableStateOf<AnalysisResponse?>(null)
    val analysisResponse: State<AnalysisResponse?> = _analysisResponse

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
        _analysisResponse.value = null
        _error.value = null
    }

    fun setProductName(name: String) {
        _productName.value = name
    }

    fun analyzeImage(context: Context) {
        val uri = _imageUri.value ?: run {
            _error.value = "Por favor, selecciona una imagen"
            return
        }

        _analyzing.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Preparar la imagen para enviar
                android.util.Log.d("UploadViewModel", "Preparando imagen para análisis")
                val file = prepareImageFile(context, uri)
                android.util.Log.d("UploadViewModel", "Imagen preparada: ${file.length()} bytes")
                
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                // Llamada al API para análisis
                android.util.Log.d("UploadViewModel", "Iniciando petición de análisis")
                val service = RetrofitInstance.getAuthenticatedRetrofit(context)
                    .create(AnalysisService::class.java)
                val response = service.analyzeImage(imagePart)

                if (response.isSuccessful) {
                    _analysisResponse.value = response.body()
                    if (_productName.value.isEmpty() && !response.body()?.textDetected.isNullOrEmpty()) {
                        // Si no hay nombre de producto y el OCR detectó algo, usamos eso como nombre
                        _productName.value = response.body()?.textDetected?.lines()?.firstOrNull() ?: ""
                    }
                } else {
                    _error.value = "Error al analizar la imagen: ${response.code()} - ${response.message()}"
                    android.util.Log.e("UploadViewModel", "Error HTTP al analizar imagen: ${response.code()} - ${response.message()}")
                }
            } catch (e: java.net.SocketTimeoutException) {
                _error.value = "Tiempo de espera agotado. La petición tardó demasiado en completarse."
                android.util.Log.e("UploadViewModel", "Timeout al analizar imagen", e)
            } catch (e: java.io.IOException) {
                _error.value = "Error de conexión: ${e.message ?: "Verifica tu conexión a internet"}"
                android.util.Log.e("UploadViewModel", "Error de IO al analizar imagen", e)
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
                android.util.Log.e("UploadViewModel", "Error general al analizar imagen", e)
            } finally {
                _analyzing.value = false
            }
        }
    }

    fun uploadProduct(context: Context, onSuccess: () -> Unit) {
        val uri = _imageUri.value ?: run {
            _error.value = "Por favor, selecciona una imagen"
            return
        }

        if (_productName.value.isBlank()) {
            _error.value = "Por favor, ingresa un nombre para el producto"
            return
        }

        val analysis = _analysisResponse.value ?: run {
            _error.value = "Por favor, analiza la imagen primero"
            return
        }

        _uploading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Preparar la imagen para enviar
                android.util.Log.d("UploadViewModel", "Preparando imagen para subir")
                val file = prepareImageFile(context, uri)
                android.util.Log.d("UploadViewModel", "Imagen preparada para subir: ${file.length()} bytes")
                
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

                // Convertir el análisis a JSON
                val gson = com.google.gson.Gson()
                val resultJson = gson.toJson(analysis)
                android.util.Log.d("UploadViewModel", "JSON generado: ${resultJson.take(100)}...")
                
                // Preparar los datos del producto
                val nameBody = _productName.value.toRequestBody("text/plain".toMediaTypeOrNull())
                val jsonBody = resultJson.toRequestBody("text/plain".toMediaTypeOrNull())
                val historyIdBody = "1".toRequestBody("text/plain".toMediaTypeOrNull()) // Por ahora es estático

                // Llamada al API para subir el producto
                android.util.Log.d("UploadViewModel", "Iniciando petición para subir producto")
                val service = RetrofitInstance.getAuthenticatedRetrofit(context)
                    .create(ProductService::class.java)
                val response = service.createProduct(imagePart, nameBody, jsonBody, historyIdBody)

                if (response.isSuccessful) {
                    onSuccess()
                    resetForm()
                } else {
                    _error.value = "Error al subir el producto: ${response.code()} - ${response.message()}"
                    android.util.Log.e("UploadViewModel", "Error HTTP al subir producto: ${response.code()} - ${response.message()}")
                }
            } catch (e: java.net.SocketTimeoutException) {
                _error.value = "Tiempo de espera agotado. La petición tardó demasiado en completarse."
                android.util.Log.e("UploadViewModel", "Timeout al subir producto", e)
            } catch (e: java.io.IOException) {
                _error.value = "Error de conexión: ${e.message ?: "Verifica tu conexión a internet"}"
                android.util.Log.e("UploadViewModel", "Error de IO al subir producto", e)
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
                android.util.Log.e("UploadViewModel", "Error general al subir producto", e)
            } finally {
                _uploading.value = false
            }
        }
    }    private fun prepareImageFile(context: Context, uri: Uri): File {
        // Intentar optimizar la imagen primero
        val optimizedFile = com.tesis.nutriguideapp.utils.ImageOptimizer.optimizeImage(context, uri)
        
        // Si la optimización fue exitosa, usar el archivo optimizado
        if (optimizedFile != null) {
            android.util.Log.d("UploadViewModel", "Imagen optimizada: ${optimizedFile.length() / 1024} KB")
            return optimizedFile
        }
        
        // Si la optimización falló, usar el método original
        android.util.Log.d("UploadViewModel", "La optimización falló, usando el método original")
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, UUID.randomUUID().toString() + ".jpg")
        file.outputStream().use { inputStream?.copyTo(it) }
        android.util.Log.d("UploadViewModel", "Imagen original: ${file.length() / 1024} KB")
        return file
    }

    private fun resetForm() {
        _imageUri.value = null
        _productName.value = ""
        _analysisResponse.value = null
    }
}
