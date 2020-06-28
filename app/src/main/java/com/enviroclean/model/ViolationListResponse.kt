package com.enviroclean.model


import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

data class ViolationListResponse(
    @SerializedName("result")
    val result: ArrayList<Result?>,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("current_page")
    val currentPage: Int
):BaseResponse(){
    data class Result(
        @SerializedName("vio_id")
        val vioId: Int,
        @SerializedName("vio_comm_name")
        val vioCommName: String,
        @SerializedName("vio_unit_number")
        val vioUnitNumber: String,
        @SerializedName("vio_username")
        val vioUsername: String,
        @SerializedName("vio_bulidname")
        val vioBulidname: String,
        @SerializedName("vio_image")
        val vioImage: String
    )
}