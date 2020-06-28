package com.enviroclean.iterfacea

import com.enviroclean.model.CheckIngResponse1

/**
 * Created by imobdev on 29/2/20
 */
interface CheckInValetsClick {
    fun setValetClick(
        position: Int,
        item: CheckIngResponse1.Result.CommCheckinValet
    )
}