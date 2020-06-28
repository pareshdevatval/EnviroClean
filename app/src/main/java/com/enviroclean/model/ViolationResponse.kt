package com.enviroclean.model


import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

data class ViolationResponse(
    @SerializedName("result")
    val result: Result
):BaseResponse(){
    data class Result(
        @SerializedName("vio_id")
        val vioId: Int
    )
}