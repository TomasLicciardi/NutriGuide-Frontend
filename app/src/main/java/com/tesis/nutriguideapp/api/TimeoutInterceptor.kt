package com.tesis.nutriguideapp.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Interceptor para manejar y registrar errores de timeout en las peticiones HTTP.
 * Proporciona información detallada sobre timeouts para facilitar la depuración.
 */
class TimeoutInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        
        try {
            Log.d("NetworkInterceptor", "Iniciando petición: ${request.url}")
            val response = chain.proceed(request)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            Log.d("NetworkInterceptor", "Petición completada: ${request.url}, duración: ${duration}ms, código: ${response.code}")
            
            return response
        } catch (e: SocketTimeoutException) {
            Log.e("NetworkInterceptor", "¡TIMEOUT! en la petición a ${request.url}. Duración: ${System.currentTimeMillis() - startTime}ms", e)
            throw IOException("Tiempo de espera agotado al conectar con el servidor. Comprueba tu conexión a Internet y vuelve a intentarlo.", e)
        } catch (e: IOException) {
            Log.e("NetworkInterceptor", "Error de red en la petición a ${request.url}. Duración: ${System.currentTimeMillis() - startTime}ms", e)
            throw e
        } catch (e: Exception) {
            Log.e("NetworkInterceptor", "Error inesperado en la petición a ${request.url}. Duración: ${System.currentTimeMillis() - startTime}ms", e)
            throw e
        }
    }
}
