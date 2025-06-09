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
    
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    fun clearImage() {
        _imageUri.value = null
        _analyzeSuccess.value = false
        _productId.value = null
        _error.value = null
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

        viewModelScope.launch {
            try {                // Obtener el archivo real a partir de la URI
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
                    android.util.Log.d("CameraViewModel", "Análisis exitoso para producto: ${analysisResult.productId}")
                    
                    // Guardar el ID del producto para navegación
                    _productId.value = analysisResult.productId
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Error en la respuesta: ${response.code()} - ${response.message()}"
                    _error.value = if (errorBody != null) "$errorMessage\n$errorBody" else errorMessage
                    android.util.Log.e("CameraViewModel", "Error HTTP: ${response.code()} - ${response.message()}")
                    android.util.Log.e("CameraViewModel", "Error body: $errorBody")
                }
            } catch (e: Exception) {
                _error.value = "Error al analizar la imagen: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
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
