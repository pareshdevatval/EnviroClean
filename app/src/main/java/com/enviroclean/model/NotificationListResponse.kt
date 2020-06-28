package com.enviroclean.model


import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

data class NotificationListResponse(
    @SerializedName("result")
    val result: ArrayList<Result?>,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("unread_count")
    val unreadCount: Int
):BaseResponse(){
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
}