package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("restrictions") val restrictions: List<String>
)
