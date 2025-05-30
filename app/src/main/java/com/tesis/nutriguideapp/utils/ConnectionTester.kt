package com.tesis.nutriguideapp.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.tesis.nutriguideapp.api.AuthService
import com.tesis.nutriguideapp.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

/**
 * Utilidad para probar la conexión con el backend.
 * Proporciona métodos para verificar si el backend está accesible.
 */
object ConnectionTester {
    private const val TAG = "ConnectionTester"
    
    /**
     * Prueba la conexión con el backend utilizando un endpoint simple.
     * 
     * @param context El contexto de la aplicación
     * @param showToast Si se debe mostrar un Toast con el resultado
     * @param callback Función que se llamará con el resultado de la prueba (opcional)
     */
    suspend fun testBackendConnection(
        context: Context,
        showToast: Boolean = true,
        callback: ((success: Boolean, message: String) -> Unit)? = null
    ) {
        try {
            withContext(Dispatchers.IO) {
                // Intenta realizar una petición simple al backend
                val client = RetrofitInstance.retrofit.create(AuthService::class.java)
                
                try {
                    // Intenta solo conectarse al servidor sin enviar credenciales
                    val url = RetrofitInstance.BASE_URL
                    Log.d(TAG, "Probando conexión a: $url")
                    
                    val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.requestMethod = "GET"
                    
                    try {
                        val responseCode = connection.responseCode
                        Log.d(TAG, "Código de respuesta: $responseCode")
                        
                        if (responseCode == 200 || responseCode == 404) {
                            // 200 OK o 404 Not Found indica que el servidor está respondiendo
                            val successMessage = "Conexión exitosa al backend (código: $responseCode)"
                            Log.d(TAG, successMessage)
                            
                            withContext(Dispatchers.Main) {
                                if (showToast) {
                                    Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show()
                                }
                                callback?.invoke(true, successMessage)
                            }
                        } else {
                            val errorMessage = "El servidor respondió con código: $responseCode"
                            Log.e(TAG, errorMessage)
                            
                            withContext(Dispatchers.Main) {
                                if (showToast) {
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                                callback?.invoke(false, errorMessage)
                            }
                        }
                    } finally {
                        connection.disconnect()
                    }
                } catch (e: SocketTimeoutException) {
                    val errorMessage = "Tiempo de espera agotado: ${e.message}"
                    Log.e(TAG, errorMessage, e)
                    
                    withContext(Dispatchers.Main) {
                        if (showToast) {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                        callback?.invoke(false, errorMessage)
                    }
                } catch (e: IOException) {
                    val errorMessage = "Error de conexión: ${e.message}"
                    Log.e(TAG, errorMessage, e)
                    
                    withContext(Dispatchers.Main) {
                        if (showToast) {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                        callback?.invoke(false, errorMessage)
                    }
                } catch (e: HttpException) {
                    val errorMessage = "Error HTTP: ${e.code()} - ${e.message()}"
                    Log.e(TAG, errorMessage, e)
                    
                    withContext(Dispatchers.Main) {
                        if (showToast) {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                        callback?.invoke(false, errorMessage)
                    }
                } catch (e: Exception) {
                    val errorMessage = "Error inesperado: ${e.message}"
                    Log.e(TAG, errorMessage, e)
                    
                    withContext(Dispatchers.Main) {
                        if (showToast) {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        }
                        callback?.invoke(false, errorMessage)
                    }
                }
            }
        } catch (e: Exception) {
            val errorMessage = "Error al ejecutar la prueba: ${e.message}"
            Log.e(TAG, errorMessage, e)
            
            withContext(Dispatchers.Main) {
                if (showToast) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
                callback?.invoke(false, errorMessage)
            }
        }
    }
}
