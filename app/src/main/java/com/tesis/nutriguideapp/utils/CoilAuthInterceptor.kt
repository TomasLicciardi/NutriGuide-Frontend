package com.tesis.nutriguideapp.utils

import android.content.Context
import com.tesis.nutriguideapp.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor para agregar autenticación a las solicitudes de Coil
 */
class CoilAuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = TokenManager(context).getToken()
        
        return if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}
