package com.enviroclean.model


import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

data class PayStubResponse(
    @SerializedName("result")
    val result: ArrayList<Result>,
    @SerializedName("u_total_earn")
    val uTotalEarn: Int,
    @SerializedName("u_total_hrs")
    val uTotalHrs: Int,
    @SerializedName("u_avg_days")
    val uAvgDays: Int,
    @SerializedName("u_total_days")
    val uTotalDays: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("current_page")
    val currentPage: Int
):BaseResponse(){
    data class Result(
        @SerializedName("up_id")
        val upId: Int,
        @SerializedName("up_inv_no")
        val upInvNo: Int,
        @SerializedName("up_start_date")
        val upStartDate: String,
        @SerializedName("up_end_date")
        val upEndDate: String,
        @SerializedName("up_amount")
        val upAmount: String,
        @SerializedName("up_hrs")
        val upHrs: String,
        @SerializedName("up_pdf_url")
        val up_pdf_url: String
    )
}