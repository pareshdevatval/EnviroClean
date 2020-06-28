package com.enviroclean.model

import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

/**
 * Created by imobdev on 25/2/20
 */
class CreateChannelResponse(@SerializedName("channel_id")val channel_id: String) : BaseResponse()

