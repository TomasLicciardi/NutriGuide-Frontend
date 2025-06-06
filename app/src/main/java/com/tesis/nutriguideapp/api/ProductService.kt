package com.tesis.nutriguideapp.api

import com.tesis.nutriguideapp.model.Product
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ProductService {
    @GET("/products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Product

    @GET("/products/")
    suspend fun getAllProducts(): List<Product>    @Multipart
    @POST("/products/")
    suspend fun createProduct(
        @Part image: MultipartBody.Part?, // ok
        @Part("result_json") resultJson: RequestBody,
        @Part("history_id") historyId: RequestBody,
        @Part("is_suitable") isSuitable: RequestBody
    ): Response<Map<String, Any>> // usa el Response correcto de Retrofit
}
