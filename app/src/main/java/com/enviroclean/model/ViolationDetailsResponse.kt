package com.enviroclean.model


import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

data class ViolationDetailsResponse(
    @SerializedName("result")
    val result: Result
):BaseResponse(){
    data class Result(
        @SerializedName("vio_id")
        val vioId: Int,
        @SerializedName("vio_unit_number")
        val vioUnitNumber: String,
        @SerializedName("vio_description")
        val vioDescription: String,
        @SerializedName("vio_name")
        val vioName: String,
        @SerializedName("vio_sent_flag")
        val vio_sent_flag: Int,
        @SerializedName("vio_images")
        val vioImages: List<String>
    )
}