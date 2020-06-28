package com.enviroclean.model.violationreason


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ValetReason(
    @SerializedName("reason_id")
    val reasonId: String,
    @SerializedName("reason_name")
    val reasonName: String
): Parcelable