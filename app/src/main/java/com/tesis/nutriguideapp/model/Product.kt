package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("result_json") val resultJson: String,
    @SerializedName("date") val date: String,
    @SerializedName("image_url") val imageUrl: String?
)
