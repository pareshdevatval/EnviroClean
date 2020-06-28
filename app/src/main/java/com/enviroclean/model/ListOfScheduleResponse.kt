package com.enviroclean.model

import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

/**
 * Created by imobdev on 29/1/20
 */
data class ListOfScheduleResponse(
    @SerializedName("current_page")val current_page: Int,
    @SerializedName("per_page")val per_page: Int,
    @SerializedName("result")val result: ArrayList<ScheduleList?>,
    @SerializedName("communities")val communities: ArrayList<Communities?>,
    @SerializedName("total_pages")val total_pages: Int
):BaseResponse()

data class ScheduleList(
    val sch_comm_id: Int,
    val sch_comm_name: String,
    val sch_days: ArrayList<String?>,
    val sch_id: Int,
    val sch_intime: String,
    val sch_name: String,
    val sch_outtime: String,
    val sch_valets: ArrayList<SchValet?>
)
data class Communities(
    val comm_id: Any?,
    val comm_name: String
)

data class SchValet(
    val u_first_name: String,
    var u_id: Int,
    val u_image: String,
    val u_last_name: String
)