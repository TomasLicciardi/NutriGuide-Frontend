package com.tesis.nutriguideapp.api

import com.tesis.nutriguideapp.model.Product
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ProductService {
    @GET("/products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Product

    @GET("/products/")
    suspend fun getAllProducts(): List<Product>

    @Multipart
    @POST("/products/")
    suspend fun createProduct(
        @Part name: MultipartBody.Part,
        @Part result_json: MultipartBody.Part,
        @Part history_id: MultipartBody.Part,
        @Part image: MultipartBody.Part?
    ): retrofit2.Response<Map<String, Any>>


}