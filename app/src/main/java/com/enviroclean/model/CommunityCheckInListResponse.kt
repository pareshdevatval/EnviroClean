package com.enviroclean.model

import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

/**
 * Created by imobdev on 31/1/20
 */
open class CommunityCheckInListResponse(
    @SerializedName("result") val result: ArrayList<CheckInUser?>
) : BaseResponse()

data class CheckInUser(
    val u_first_name: String,
    val u_id: Int,
    val u_image: String,
    val u_last_name: String,
    var isSelected:Boolean=false
)