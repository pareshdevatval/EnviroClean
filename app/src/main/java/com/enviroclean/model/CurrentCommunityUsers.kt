package com.enviroclean.model


import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

data class CurrentCommunityUsers(
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
        @SerializedName("u_id")
        val uId: Int,
        @SerializedName("u_first_name")
        val uFirstName: String,
        @SerializedName("u_last_name")
        val uLastName: String,
        @SerializedName("u_image")
        val uImage: String,
        @SerializedName("u_channel_id")
        val u_channel_id:String
    )
}