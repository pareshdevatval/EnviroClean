package com.enviroclean.model


import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("n_id")
    val nId: Int,
    @SerializedName("n_content")
    val nContent: String,
    @SerializedName("n_created_at")
    val nCreatedAt: String,
    @SerializedName("n_readflag")
    val nReadflag: Int
)