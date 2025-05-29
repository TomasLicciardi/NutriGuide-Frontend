package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

data class PasswordResetRequest(
    @SerializedName("token") val token: String,
    @SerializedName("new_password") val newPassword: String
)
