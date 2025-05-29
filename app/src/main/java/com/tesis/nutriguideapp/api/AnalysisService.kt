package com.tesis.nutriguideapp.api

import com.tesis.nutriguideapp.model.AnalysisResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AnalysisService {
    @Multipart
    @POST("/analysis/")
    suspend fun analyzeImage(
        @Part image: MultipartBody.Part
    ): Response<AnalysisResponse>
}
