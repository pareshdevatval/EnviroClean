package com.enviroclean.model

import com.google.gson.annotations.SerializedName

data class Banner(
    @SerializedName("image") val image: String,
    @SerializedName("url") val url: String
)
