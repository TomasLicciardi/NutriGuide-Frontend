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
    // ─────────────────────────────────────────────────────────────
    // URL del backend NutriGuide.
    //
    // Cambiá la línea activa según el entorno donde estés probando:
    //
    //   • Emulador Android Studio       → http://10.0.2.2:8000/
    //   • Dispositivo físico (WiFi)     → http://<IP-de-tu-PC>:8000/
    //                                      (ej: http://192.168.1.42:8000/)
    //   • UM Cloud por ZeroTier / VPN   → http://10.201.1.25:<NODEPORT>/
    //   • UM Cloud con Ingress          → http://nutriguide.um.local/
    //                                      (configurar /etc/hosts o DNS)
    //
    // El network_security_config.xml ya tiene whitelisted estas IPs.
    // Si agregás otra, también actualizá ese archivo.
    // ─────────────────────────────────────────────────────────────
    const val BASE_URL = "http://10.0.2.2:8000/"
    // const val BASE_URL = "http://10.201.1.25:30080/"   // ← UM Cloud NodePort (ajustar puerto)
    // const val BASE_URL = "http://192.168.1.42:8000/"   // ← LAN local (poner tu IP)

    // Configurar Gson con deserializador personalizado
    private val gson = GsonBuilder()
        .registerTypeAdapter(ProductAnalysis::class.java, ProductAnalysisDeserializer())
        .create()

    // Cliente HTTP optimizado para autenticación (login/register)
    private val authOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)   // Más agresivo
        .readTimeout(10, TimeUnit.SECONDS)     // Más agresivo 
        .writeTimeout(8, TimeUnit.SECONDS)     // Más agresivo
        .retryOnConnectionFailure(false)       // Sin reintentos para auth
        .build()

    // Cliente HTTP para peticiones sin autenticación (análisis de imágenes, etc.)
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

    // Retrofit para autenticación (sin interceptores pesados)
    val authRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(authOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
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
