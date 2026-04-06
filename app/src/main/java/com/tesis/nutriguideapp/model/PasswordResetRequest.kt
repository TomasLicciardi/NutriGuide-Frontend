package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

data class PasswordResetRequest(
    @SerializedName("reset_token") val token: String,
    @SerializedName("new_password") val newPassword: String
)
