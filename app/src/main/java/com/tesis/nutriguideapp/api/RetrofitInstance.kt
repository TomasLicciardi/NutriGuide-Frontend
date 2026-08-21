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
    // URL del backend NutriGuide — CONFIGURABLE EN RUNTIME.
    //
    // Por defecto apunta al emulador (10.0.2.2). Desde la pantalla de login
    // ("Servidor") se puede cambiar a la URL que se necesite y queda guardada:
    //   • Emulador                → http://10.0.2.2:8000/
    //   • PC en la misma WiFi     → http://<IP-de-tu-PC>:8000/
    //   • ngrok (túnel público)   → https://xxxx.ngrok.io/
    //   • UM Cloud (NodePort)     → http://10.201.1.25:30080/
    //
    // network_security_config.xml ya permite cleartext (base-config), así que
    // cualquier host http funciona.
    // ─────────────────────────────────────────────────────────────
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"
    private const val PREFS = "nutriguide_prefs"
    private const val KEY_BASE_URL = "base_url"

    @Volatile
    var BASE_URL: String = DEFAULT_BASE_URL
        private set

    /** Carga la URL guardada. Llamar una vez al arrancar (MainActivity.onCreate). */
    fun init(context: Context) {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        BASE_URL = sp.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }

    /** Cambia la URL del backend en runtime y la persiste. */
    fun setBaseUrl(context: Context, url: String) {
        var u = url.trim()
        if (u.isEmpty()) return
        if (!u.startsWith("http://") && !u.startsWith("https://")) u = "http://$u"
        if (!u.endsWith("/")) u = "$u/"
        BASE_URL = u
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_BASE_URL, u).apply()
    }

    // Gson con deserializador personalizado
    private val gson = GsonBuilder()
        .registerTypeAdapter(ProductAnalysis::class.java, ProductAnalysisDeserializer())
        .create()

    // Cliente HTTP para autenticación (login/register) — timeouts agresivos
    private val authOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .build()

    // Cliente HTTP para el resto (análisis, etc.)
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

    // Computadas: se reconstruyen con la BASE_URL vigente en cada acceso,
    // así un cambio de servidor en runtime toma efecto sin reiniciar la app.
    val authRetrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(authOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    // Retrofit autenticado (agrega el token). También usa la BASE_URL vigente.
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
