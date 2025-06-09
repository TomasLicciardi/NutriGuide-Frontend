package com.tesis.nutriguideapp.utils

import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import okhttp3.OkHttpClient

/**
 * Utilidades para configurar Coil con autenticación
 */
object CoilUtils {
    
    /**
     * Crea un ImageLoader configurado con autenticación
     */
    fun createAuthenticatedImageLoader(context: Context): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(CoilAuthInterceptor(context))
            .build()
        
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .build()
    }
    
    /**
     * Crea un ImageRequest con autenticación para una URL específica
     */
    fun createAuthenticatedImageRequest(context: Context, url: String): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .build()
    }
}
