package com.tesis.nutriguideapp.api

import android.content.Context
import com.google.gson.GsonBuilder
import com.tesis.nutriguideapp.model.ProductAnalysis
import com.tesis.nutriguideapp.model.ProductAnalysisDeserializer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    const val BASE_URL = "http://10.0.2.2:8000/"

    // Configurar Gson con deserializador personalizado
    private val gson = GsonBuilder()
        .registerTypeAdapter(ProductAnalysis::class.java, ProductAnalysisDeserializer())
        .create()

    // Cliente HTTP para peticiones sin autenticación
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(SafeHttpLoggingInterceptor().apply {
            level = SafeHttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor(TimeoutInterceptor())
        .addInterceptor(RetryInterceptor())
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // Retrofit sin autenticación
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Método para obtener una instancia con autenticación
    fun getAuthenticatedRetrofit(context: Context): Retrofit {
        val safeLoggingInterceptor = SafeHttpLoggingInterceptor().apply {
            level = SafeHttpLoggingInterceptor.Level.BODY
        }

        val authenticatedClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(safeLoggingInterceptor)
            .addInterceptor(TimeoutInterceptor())
            .addInterceptor(RetryInterceptor())
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authenticatedClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
