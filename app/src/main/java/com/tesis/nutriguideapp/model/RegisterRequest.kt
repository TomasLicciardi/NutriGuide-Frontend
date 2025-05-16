package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("usuario")
    val usuario: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("contrasena")
    val contrasena: String,

    @SerializedName("restricciones")
    val restricciones: List<String>
)
