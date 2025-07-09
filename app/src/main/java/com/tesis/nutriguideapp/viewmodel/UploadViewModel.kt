package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.tesis.nutriguideapp.api.AnalysisService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.model.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*

class UploadViewModel : ViewModel() {
    
    private val _imageUri = mutableStateOf<Uri?>(null)
    val imageUri: State<Uri?> = _imageUri

    // 🆕 CAMBIO: Usar AnalysisResult en lugar de estados separados
    private val _analysisState = mutableStateOf<AnalysisResult>(AnalysisResult.Loading)
    val analysisState: State<AnalysisResult> = _analysisState

    private val _uploading = mutableStateOf(false)
    val uploading: State<Boolean> = _uploading

    // 🆕 ESTADO PARA CONTROLAR MODALES
    private val _showErrorModal = mutableStateOf(false)
    val showErrorModal: State<Boolean> = _showErrorModal

    // 🆕 MANTENER COMPATIBILIDAD CON CÓDIGO EXISTENTE
    private val _analysisResponse = mutableStateOf<AnalysisResponse?>(null)
    val analysisResponse: State<AnalysisResponse?> = _analysisResponse

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _analyzing = mutableStateOf(false)
    val analyzing: State<Boolean> = _analyzing
    
    fun setImageUri(uri: Uri?) {
        _imageUri.value = uri
        _analysisResponse.value = null
        _analysisState.value = AnalysisResult.Loading
        _error.value = null
        _showErrorModal.value = false
    }

    // 🆕 FUNCIÓN MEJORADA PARA ANALIZAR IMAGEN
    fun analyzeImage(context: Context) {
        val uri = _imageUri.value ?: run {
            _error.value = "Por favor, selecciona una imagen"
            return
        }

        _analyzing.value = true
        _analysisState.value = AnalysisResult.Loading
        _error.value = null
        _showErrorModal.value = false

        viewModelScope.launch {
            try {
                // Preparar la imagen para enviar
                android.util.Log.d("UploadViewModel", "Preparando imagen para análisis")
                val file = prepareImageFile(context, uri)
                android.util.Log.d("UploadViewModel", "Imagen preparada: ${file.length()} bytes")
                
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // Llamada al API para análisis
                android.util.Log.d("UploadViewModel", "Iniciando petición de análisis")
                val service = RetrofitInstance.getAuthenticatedRetrofit(context)
                    .create(AnalysisService::class.java)
                val response = service.analyzeImage(imagePart)

                // 🆕 MANEJO MEJORADO DE RESPUESTAS
                if (response.isSuccessful) {
                    val analysisResult = response.body()
                    if (analysisResult != null) {
                        _analysisResponse.value = analysisResult
                        _analysisState.value = AnalysisResult.Success(analysisResult)
                        android.util.Log.d("UploadViewModel", "Análisis exitoso: ${analysisResult.productId}")
                    } else {
                        val errorMsg = "La respuesta del servidor está vacía"
                        _error.value = errorMsg
                        _analysisState.value = AnalysisResult.ServerError(errorMsg)
                        _showErrorModal.value = true
                    }
                } else {
                    // 🆕 MANEJO ESPECÍFICO POR CÓDIGO DE ERROR
                    handleErrorResponse(response.code(), response.errorBody()?.string())
                }
            } catch (e: java.net.SocketTimeoutException) {
                val errorMsg = "Tiempo de espera agotado"
                _error.value = errorMsg
                _analysisState.value = AnalysisResult.NetworkError(
                    message = errorMsg,
                    instructions = "La imagen puede ser demasiado grande. Intenta con una imagen más pequeña."
                )
                _showErrorModal.value = true
                android.util.Log.e("UploadViewModel", "Timeout al analizar imagen", e)
            } catch (e: java.io.IOException) {
                val errorMsg = "Error de conexión: ${e.message ?: "Verifica tu conexión a internet"}"
                _error.value = errorMsg
                _analysisState.value = AnalysisResult.NetworkError(errorMsg)
                _showErrorModal.value = true
                android.util.Log.e("UploadViewModel", "Error de IO al analizar imagen", e)
            } catch (e: Exception) {
                val errorMsg = "Error inesperado: ${e.message}"
                _error.value = errorMsg
                _analysisState.value = AnalysisResult.ServerError(errorMsg)
                _showErrorModal.value = true
                android.util.Log.e("UploadViewModel", "Error general al analizar imagen", e)
            } finally {
                _analyzing.value = false
            }
        }
    }

    // 🆕 FUNCIÓN PARA MANEJAR ERRORES ESPECÍFICOS
    private fun handleErrorResponse(statusCode: Int, errorBody: String?) {
        android.util.Log.e("UploadViewModel", "Error HTTP $statusCode: $errorBody")
        
        try {
            val gson = Gson()
            val errorResponse = gson.fromJson(errorBody, BackendErrorResponse::class.java)
            val errorDetail = errorResponse.getErrorData() // 🆕 Usar función helper
            
            when (statusCode) {
                400 -> {
                    _analysisState.value = AnalysisResult.ImageError(
                        errorType = errorDetail.error,
                        message = errorDetail.message,
                        instructions = errorDetail.instructions
                    )
                    _error.value = errorDetail.message
                }
                422 -> {
                    android.util.Log.d("UploadViewModel", "Error 422 detectado - Confianza insuficiente")
                    _analysisState.value = AnalysisResult.LowConfidenceError(
                        message = errorDetail.message,
                        instructions = errorDetail.instructions
                    )
                    _error.value = errorDetail.message
                    android.util.Log.d("UploadViewModel", "Estado cambiado a LowConfidenceError: ${errorDetail.message}")
                }
                429 -> {
                    _analysisState.value = AnalysisResult.RateLimitError()
                    _error.value = "Demasiadas solicitudes"
                }
                500 -> {
                    _analysisState.value = AnalysisResult.ServerError(
                        message = errorDetail.message,
                        instructions = errorDetail.instructions
                    )
                    _error.value = errorDetail.message
                }
                else -> {
                    _analysisState.value = AnalysisResult.ServerError("Error desconocido: $statusCode")
                    _error.value = "Error desconocido: $statusCode"
                }
            }
        } catch (e: Exception) {
            // Si no se puede parsear el error, usar manejo genérico
            val genericError = when (statusCode) {
                400 -> AnalysisResult.ImageError(
                    errorType = "invalid_image",
                    message = "Imagen no válida",
                    instructions = "Toma una foto de la etiqueta nutricional del producto"
                )
                422 -> AnalysisResult.LowConfidenceError(
                    message = "Análisis con baja confianza",
                    instructions = "Toma una foto más clara de la etiqueta"
                )
                429 -> AnalysisResult.RateLimitError()
                500 -> AnalysisResult.ServerError()
                else -> AnalysisResult.ServerError("Error HTTP: $statusCode")
            }
            
            _analysisState.value = genericError
            _error.value = when (genericError) {
                is AnalysisResult.ImageError -> genericError.message
                is AnalysisResult.LowConfidenceError -> genericError.message
                is AnalysisResult.RateLimitError -> genericError.message
                is AnalysisResult.ServerError -> genericError.message
                else -> "Error desconocido"
            }
        }
        
        _showErrorModal.value = true
        android.util.Log.d("UploadViewModel", "Modal de error activado - Estado: ${_analysisState.value}")
    }

    // 🆕 FUNCIONES PARA CONTROLAR MODALES
    fun dismissErrorModal() {
        _showErrorModal.value = false
    }

    fun retryAnalysis(context: Context) {
        dismissErrorModal()
        analyzeImage(context)
    }

    fun clearAnalysisAndRetakePhoto() {
        _analysisState.value = AnalysisResult.Loading
        _analysisResponse.value = null
        _error.value = null
        _showErrorModal.value = false
        // Mantener la URI para permitir reanalizar la misma imagen si es necesario
    }

    fun uploadProduct(context: Context, onSuccess: () -> Unit) {
        // Como el backend ya guarda el producto durante el análisis,
        // solo necesitamos navegar de vuelta al home
        onSuccess()
        resetForm()
    }
    
    private fun prepareImageFile(context: Context, uri: Uri): File {
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
      // Resetear el formulario
    private fun resetForm() {
        _imageUri.value = null
        _analysisResponse.value = null
        _analyzing.value = false
        _uploading.value = false
        _error.value = null
    }
    
    // Limpiar análisis para analizar otro producto
    fun clearAnalysis() {
        _analysisResponse.value = null
        _imageUri.value = null
        _error.value = null
    }
}