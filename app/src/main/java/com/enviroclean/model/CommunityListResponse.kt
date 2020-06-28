package com.enviroclean.model


import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

data class CommunityListResponse(
    @SerializedName("result")
    val result: ArrayList<Result>
):BaseResponse(){
    data class Result(
        @SerializedName("comm_id")
        val commId: Int,
        @SerializedName("comm_name")
        val commName: String
    )
}