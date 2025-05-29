package com.tesis.nutriguideapp.model

import com.google.gson.annotations.SerializedName

data class UserRestrictionsRequest(
    @SerializedName("restrictions") val restrictions: List<String>
)
