package com.enviroclean.model


import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ViolationList(
    @SerializedName("result")
    val result: ArrayList<Result?>,
    @SerializedName("areas")
    val areas: ArrayList<Areas?>,
    @SerializedName("vio_client_flag")
    val vioClientFlag: Int
  /*  @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("current_page")
    val currentPage: Int*/
):BaseResponse(){
    data class Areas(@SerializedName("area_id")
                      val area_id: Int,
                      @SerializedName("area_name")
                      val area_name: String)
    data class Result(
        @SerializedName("vio_id")
        val vioId: Int,
        @SerializedName("vio_reason")
        val vioReason: Int,
        @SerializedName("vio_comm_name")
        val vioCommName: String,
        @SerializedName("vio_unit_number")
        val vioUnitNumber: String,
        @SerializedName("vio_username")
        val vioUsername: String,
        @SerializedName("vio_bulidname")
        val vioBulidname: String,
        @SerializedName("vio_description")
        val vioDescription: String,
        @Expose
        var isCheck: Boolean=false,
        @SerializedName("vio_images")
        val vioImages: ArrayList<VioImage?>,
        @Expose
        var slectedList: ArrayList<String?>? = null
    ){

        data class VioImage(
            @SerializedName("vio_image_url")
            val vioImageUrl: String,
            @SerializedName("vio_image_id")
            val vioImageId: Int,
            @Expose
            var isCheck: Boolean=false
        )
    }
}