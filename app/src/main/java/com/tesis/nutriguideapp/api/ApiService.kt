package com.tesis.nutriguideapp.api

import com.tesis.nutriguideapp.model.AuthRequest
import com.tesis.nutriguideapp.model.AuthResponse
import com.tesis.nutriguideapp.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}

