package com.enviroclean.model

import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

/**
 * Created by imobdev on 29/1/20
 */
data class ListOfValetsResponse(
    @SerializedName("current_page")val current_page: Int,
    @SerializedName("per_page")val per_page: Int,
    @SerializedName("result")val result: ArrayList<Valets?>,
    @SerializedName("total_pages") val total_pages: Int
):BaseResponse()

data class Valets(
    val u_comm_count: Int,
    val u_comm_flag: Int,
    val u_first_name: String,
    val u_id: Int,
    val u_image: String,
    val u_last_name: String,
    var isSelecte:Boolean=false
)