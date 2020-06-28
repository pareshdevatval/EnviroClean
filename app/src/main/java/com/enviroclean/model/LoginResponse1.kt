package com.enviroclean.model


import com.enviroclean.model.violationreason.ManagerReason
import com.enviroclean.model.violationreason.ValetReason
import com.google.gson.annotations.SerializedName

data class LoginResponse1(
    @SerializedName("logindata")
    val logindata: Logindata,
    @SerializedName("valet_reason")
val valetReason: List<ValetReason>,
@SerializedName("manager_reason")
val managerReason: List<ManagerReason>
):CheckIngResponse1(){
    data class Logindata(
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
        @SerializedName("u_mailing_address")
        val uMailingAddress: String,
        @SerializedName("u_gender")
        val uGender: Int,
        @SerializedName("u_type")
        val uType: Int,
        @SerializedName("u_image")
        val uImage: String,
        @SerializedName("u_last_login")
        val uLastLogin: String,
        @SerializedName("u_status")
        val uStatus: Int,
        @SerializedName("u_created_at")
        val uCreatedAt: String,
        @SerializedName("u_updated_at")
        val uUpdatedAt: String,
        @SerializedName("u_deleted_at")
        val uDeletedAt: String,
        @SerializedName("checkin_flag")
        val checkinFlag: Int,
        @SerializedName("token")
        val token: String,
        @SerializedName("unread_noti_count")
        var unreadNotificationCount: Int
    )
}