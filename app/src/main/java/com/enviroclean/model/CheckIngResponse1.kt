package com.enviroclean.model


import android.os.Parcelable
import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize

open class CheckIngResponse1(): BaseResponse(), Parcelable {

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
        @SerializedName("comm_sch_name")
        val commSchName: String,
        @Expose
        @SerializedName("comm_remain_count")
        var commRemainCount: Int,
        @Expose
        @SerializedName("comm_vio_count")
        var commVioCount: Int,
        @SerializedName("comm_checkin_valets")
        val commCheckinValets: ArrayList<CommCheckinValet?>,
        @SerializedName("comm_qrcodes")
        val commQrcodes: ArrayList<CommQrcode?>,
        @SerializedName("comm_areas")
        val comm_areas: ArrayList<CommAreas?>
    ):Parcelable{
        @Parcelize
        data class CommCheckinValet(
            @SerializedName("u_id")
            val uId: Int,
            @SerializedName("u_image")
            val uImage: String,
            @SerializedName("u_first_name")
            val uFirstName: String,
            @SerializedName("u_last_name")
            val uLastName: String,
            @SerializedName("u_channel_id")
            val u_channel_id: String
        ):Parcelable

        @Parcelize
        data class CommQrcode(
            @Expose
            @SerializedName("qr_type")
            var qrType: Int,
            @SerializedName("qr_id")
            val qrId: Int,
            @SerializedName("qr_code")
            val qrCode: String,
            @SerializedName("qr_text")
            val qrText: String,
            @Expose
            @SerializedName("username")
            var username: String,
            @SerializedName("qr_mandatory")
            var qrMandatory: Int
        ):Parcelable
        @Parcelize
        data class CommAreas(
            @Expose
            @SerializedName("area_id")
            var area_id: Int,
            @SerializedName("area_name")
            val area_name: String,
            @SerializedName("area_latitude")
            val area_latitude: String,
            @SerializedName("area_longitude")
            val area_longitude: String,
            @Expose
            @SerializedName("area_range")
            var area_range: String
        ):Parcelable

    }
}