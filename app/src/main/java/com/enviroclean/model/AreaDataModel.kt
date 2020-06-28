package com.enviroclean.model

/**
 * Created by keshu odedara on 29,January,2020
 */

data class AreaDataModel(
    var qrType: Int,
    val qrId: Int,
    val qrText: String,
    val qrCode: String,
    var username: String
)