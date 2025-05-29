package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

data class PasswordChangeRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)
