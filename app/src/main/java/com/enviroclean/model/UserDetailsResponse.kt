package com.enviroclean.model


import android.annotation.SuppressLint
import android.os.Parcelable
import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserDetailsResponse(
    @SerializedName("result")
    val result: Result
):BaseResponse(), Parcelable {
    @Parcelize
    data class Result(
        @SerializedName("u_id")
        val uId: Int,
        @SerializedName("u_first_name")
        val uFirstName: String,
        @SerializedName("u_last_name")
        val uLastName: String,
        @SerializedName("u_email")
        val uEmail: String,
        @SerializedName("u_mobile_number")
        val uMobileNumber: String,
        @SerializedName("u_dob")
        val uDob: String,
        @SerializedName("u_gender")
        val uGender: Int,
        @SerializedName("u_type")
        val uType: Int,
        @SerializedName("u_total_earn")
        val uTotalEarn: Int,
        @SerializedName("u_total_hrs")
        val uTotalHrs: Int,
        @SerializedName("u_avg_days")
        val uAvgDays: Int,
        @SerializedName("u_total_days")
        val uTotalDays: Int,
        @SerializedName("u_image")
        val uImage: String,
        @SerializedName("u_location")
        val u_location: String,
        @SerializedName("u_paystub_type")
        val u_paystub_type: Int
    ):Parcelable
}