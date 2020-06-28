package com.enviroclean.model


import android.os.Parcelable
import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
open class CheckingResponse():BaseResponse(), Parcelable {

    @SerializedName("result")
    val result: Result? = null

    @Parcelize
    data class Result(
        @SerializedName("checkin_type")
        val checkinType: Int,
        @SerializedName("weekno")
        val weekno: Int,
        @SerializedName("comm_id")
        val commId: Int,
        @SerializedName("comm_sch_id")
        val commSchId: Int,
        @SerializedName("comm_name")
        val commName: String,
        @SerializedName("comm_intime")
        val commIntime: String,
        @SerializedName("comm_outtime")
        val commOuttime: String,
        @Expose
        @SerializedName("comm_remain_count")
        var commRemainCount: Int,
        @Expose
        @SerializedName("comm_vio_count")
        var commVioCount: Int,
        @SerializedName("comm_checkin_valets")
        val commCheckinValets: ArrayList<CommCheckinValet?>,
        @SerializedName("comm_qrcodes")
        val commQrcodes: ArrayList<CommQrcode?>
    ):Parcelable{
        @Parcelize
        data class CommQrcode(
            @SerializedName("area_id")
            val areaId: Int,
            @SerializedName("area_name")
            val areaName: String,
            @Expose
            @SerializedName("area_scan_count")
            var areaScanCount: Int,
            @SerializedName("area_data")
            val areaData: ArrayList<AreaData?>
        ):Parcelable{
            @Parcelize
            data class AreaData(
                @Expose
                @SerializedName("qr_type")
                var qrType: Int,
                @SerializedName("qr_id")
                val qrId: Int,
                @SerializedName("qr_text")
                val qrText: String,
                @SerializedName("qr_code")
                val qrCode: String,
                @SerializedName("area_id")
                val areaId: Int,
                @Expose
                @SerializedName("area_name")
                var areaName: String,
                @Expose
                @SerializedName("username")
                var username: String
            ):Parcelable
        }
        @Parcelize
        data class CommCheckinValet(
            @SerializedName("u_id")
            val uId: Int,
            @SerializedName("u_image")
            val uImage: String,
            @SerializedName("u_first_name")
            val u_first_name: String,
            @SerializedName("u_last_name")
            val u_last_name: String
        ):Parcelable
    }
}