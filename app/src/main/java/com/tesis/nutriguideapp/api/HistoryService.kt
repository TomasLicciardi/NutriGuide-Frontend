package com.tesis.nutriguideapp.api

import com.tesis.nutriguideapp.model.Product
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HistoryService {
    @GET("/history/")
    suspend fun getUserHistory(): List<Product>
    
    @GET("/history/")
    suspend fun getFilteredHistory(@Query("restrictions") restrictions: String): List<Product>
    
    @GET("/history/product/{id}")
    suspend fun getHistoryProductDetail(@Path("id") id: Int): Product
    
    @DELETE("/history/product/{id}")
    suspend fun deleteHistoryProduct(@Path("id") id: Int): Response<Map<String, Any>>
    
    @DELETE("/history/")
    suspend fun clearHistory(): Response<Map<String, Any>>
}
