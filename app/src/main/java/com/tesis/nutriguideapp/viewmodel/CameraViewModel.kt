package com.tesis.nutriguideapp.viewmodel

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tesis.nutriguideapp.api.AnalysisService
import com.tesis.nutriguideapp.api.RetrofitInstance
import com.tesis.nutriguideapp.model.AnalysisResult
import com.tesis.nutriguideapp.model.BackendErrorResponse
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraViewModel : ViewModel() {
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _analyzeSuccess = MutableStateFlow(false)
    val analyzeSuccess: StateFlow<Boolean> = _analyzeSuccess
    
    private val _productId = MutableStateFlow<Int?>(null)
    val productId: StateFlow<Int?> = _productId
    
    // 🆕 NUEVO SISTEMA DE MANEJO DE ERRORES
    private val _analysisState = MutableStateFlow<AnalysisResult>(AnalysisResult.Loading)
    val analysisState: StateFlow<AnalysisResult> = _analysisState
    
    private val _showErrorModal = MutableStateFlow(false)
    val showErrorModal: StateFlow<Boolean> = _showErrorModal
    
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    fun clearImage() {
        _imageUri.value = null
        _analyzeSuccess.value = false
        _productId.value = null
        _error.value = null
        _analysisState.value = AnalysisResult.Loading
        _showErrorModal.value = false
    }
    
    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }
    
    fun takePicture(
        imageCapture: ImageCapture,
        outputDirectory: File,
        executor: Executor
    ) {
        _isLoading.value = true
        _error.value = null
        
        // Crear archivo de salida
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
                .format(System.currentTimeMillis()) + ".jpg"
        )
        
        // Configurar opciones de salida
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        
        // Capturar la imagen
        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    _imageUri.value = savedUri
                    _isLoading.value = false
                }
                
                override fun onError(exception: ImageCaptureException) {
                    _error.value = "Error al capturar imagen: ${exception.message}"
                    _isLoading.value = false
                }
            }
        )
    }

    fun analyzeImage(context: Context) {
        val currentImageUri = _imageUri.value ?: run {
            _error.value = "No hay imagen para analizar"
            return
        }

        _isLoading.value = true
        _error.value = null
        _analyzeSuccess.value = false
        _analysisState.value = AnalysisResult.Loading
        _showErrorModal.value = false

        viewModelScope.launch {
            try {
                // Obtener el archivo real a partir de la URI
                val file = getFileFromUri(context, currentImageUri)

                // Crear el cuerpo de la petición multipart
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                // Llamar al servicio de análisis
                val analysisService = RetrofitInstance.getAuthenticatedRetrofit(context).create(AnalysisService::class.java)
                val response = analysisService.analyzeImage(imagePart)
                
                if (response.isSuccessful && response.body() != null) {
                    val analysisResult = response.body()!!
                    // El análisis fue exitoso
                    _analyzeSuccess.value = true
                    _analysisState.value = AnalysisResult.Success(analysisResult)
                    android.util.Log.d("CameraViewModel", "Análisis exitoso para producto: ${analysisResult.productId}")
                    
                    // Guardar el ID del producto para navegación
                    _productId.value = analysisResult.productId
                } else {
                    // 🆕 MANEJO MEJORADO DE ERRORES
                    android.util.Log.d("CameraViewModel", "Error HTTP: ${response.code()} - ${response.message()}")
                    val errorBodyString = response.errorBody()?.string()
                    android.util.Log.d("CameraViewModel", "Error body: $errorBodyString")
                    handleErrorResponse(response.code(), errorBodyString)
                }
            } catch (e: Exception) {
                _error.value = "Error al analizar la imagen: ${e.message}"
                _analysisState.value = AnalysisResult.NetworkError(
                    message = "Error de conexión: ${e.message}",
                    instructions = "Verifica tu conexión a internet"
                )
                _showErrorModal.value = true
                android.util.Log.e("CameraViewModel", "Error general: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🆕 FUNCIÓN PARA MANEJAR ERRORES ESPECÍFICOS (COPIADA DEL UPLOADVIEWMODEL)
    private fun handleErrorResponse(statusCode: Int, errorBody: String?) {
        android.util.Log.e("CameraViewModel", "Error HTTP $statusCode: $errorBody")
        
        try {
            val gson = Gson()
            val errorResponse = gson.fromJson(errorBody, BackendErrorResponse::class.java)
            android.util.Log.d("CameraViewModel", "Error response parseado: $errorResponse")
            
            val errorDetail = errorResponse.getErrorData()
            android.util.Log.d("CameraViewModel", "Error detail: $errorDetail")
            
            when (statusCode) {
                400 -> {
                    android.util.Log.d("CameraViewModel", "Error 400 detectado - Imagen inválida")
                    _analysisState.value = AnalysisResult.ImageError(
                        errorType = errorDetail.error,
                        message = errorDetail.message,
                        instructions = errorDetail.instructions
                    )
                    _error.value = errorDetail.message
                }
                422 -> {
                    android.util.Log.d("CameraViewModel", "Error 422 detectado - Confianza insuficiente")
                    android.util.Log.d("CameraViewModel", "Mensaje: ${errorDetail.message}")
                    android.util.Log.d("CameraViewModel", "Instrucciones: ${errorDetail.instructions}")
                    _analysisState.value = AnalysisResult.LowConfidenceError(
                        message = errorDetail.message,
                        instructions = errorDetail.instructions
                    )
                    _error.value = errorDetail.message
                    android.util.Log.d("CameraViewModel", "Estado cambiado a LowConfidenceError: ${errorDetail.message}")
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
                    message = "No se puede visualizar correctamente la imagen",
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
        
        android.util.Log.d("CameraViewModel", "Activando modal de error")
        _showErrorModal.value = true
        android.util.Log.d("CameraViewModel", "Estado final - analysisState: ${_analysisState.value}, showErrorModal: ${_showErrorModal.value}")
    }

    // 🆕 FUNCIONES PARA CONTROLAR MODALES
    fun dismissErrorModal() {
        _showErrorModal.value = false
    }

    fun clearAnalysisAndRetakePhoto() {
        _analysisState.value = AnalysisResult.Loading
        _error.value = null
        _showErrorModal.value = false
        _analyzeSuccess.value = false
        _productId.value = null
        // Mantener la URI para permitir reanalizar la misma imagen si es necesario
    }

    private fun getFileFromUri(context: Context, uri: Uri): File {
        // Obtener el archivo temporal
        val tempFile = File(context.cacheDir, "temp_image.jpg")
        
        // Copiar el contenido de la URI al archivo
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        return tempFile
    }
    
    fun getCameraProvider(context: Context, callback: (ProcessCameraProvider) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            callback(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(context))
    }
    
    override fun onCleared() {
        super.onCleared()
        cameraExecutor.shutdown()
    }
}
