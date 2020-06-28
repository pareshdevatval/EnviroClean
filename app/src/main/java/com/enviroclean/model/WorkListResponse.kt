package com.enviroclean.model


import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

data class WorkListResponse(
    @SerializedName("result")
    val result: ArrayList<Result>,
    @SerializedName("communities")
    val communities: ArrayList<Communities>,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("comm_vio_count")
    val comm_vio_count: Int,
    @SerializedName("checkin_sch_id")
    val checkin_sch_id: String,
    @SerializedName("checkin_sch_name")
    val checkin_sch_name: String
) : BaseResponse() {
    data class Result(
        @SerializedName("comm_id")
        val commId: Int,
        @SerializedName("comm_name")
        val commName: String,
        @SerializedName("comm_latitude")
        val commLatitude: String,
        @SerializedName("comm_longitude")
        val commLongitude: String,
        @SerializedName("comm_sch_id")
        val commSchId: Int,
        @SerializedName("comm_intime")
        val commIntime: String,
        @SerializedName("comm_outtime")
        val commOuttime: String,
        @SerializedName("comm_sch_name")
        val commSchName: String,
        @SerializedName("comm_remain_count")
        val commRemainCount: Int
    )

    data class Communities(
        @SerializedName("comm_id")
        val commId: Int,
        @SerializedName("comm_name")
        val commName: String
    )
}