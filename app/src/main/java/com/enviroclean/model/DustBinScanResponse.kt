package com.enviroclean.model


import com.google.gson.annotations.SerializedName

data class DustBinScanResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Boolean,
    @SerializedName("code")
    val code: Int
)