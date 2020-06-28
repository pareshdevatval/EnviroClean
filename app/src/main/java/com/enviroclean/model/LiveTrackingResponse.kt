package com.enviroclean.model

import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

/**
 * Created by imobdev on 31/1/20
 */
data class LiveTrackingResponse(
    @SerializedName("result") val result: ArrayList<TrackingData?>
):BaseResponse()

data class TrackingData(
    val ur_latitude: String,
    val ur_longitude: String,
    val ur_type: Int,
    val ur_area_name: String,
    val ur_clockin: String,
    val ur_clockout: String
)