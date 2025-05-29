package com.tesis.nutriguideapp.api

import com.tesis.nutriguideapp.model.PasswordChangeRequest
import com.tesis.nutriguideapp.model.PasswordResetRequest
import com.tesis.nutriguideapp.model.UserProfileResponse
import com.tesis.nutriguideapp.model.UserRestrictionsRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserService {
    @GET("/user/profile")
    suspend fun getUserProfile(): UserProfileResponse
    
    @GET("/user/restrictions")
    suspend fun getUserRestrictions(): List<String>
    
    @PUT("/user/restrictions")
    suspend fun updateUserRestrictions(@Body request: UserRestrictionsRequest): Response<Map<String, Any>>
    
    @PUT("/user/change-password")
    suspend fun changePassword(@Body request: PasswordChangeRequest): Response<Map<String, Any>>
    
    @POST("/user/forgot-password")
    suspend fun forgotPassword(@Body email: Map<String, String>): Response<Map<String, Any>>
    
    @POST("/user/reset-password")
    suspend fun resetPassword(@Body request: PasswordResetRequest): Response<Map<String, Any>>
}
