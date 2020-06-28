package com.enviroclean.model

import com.enviroclean.api.BaseResponse
import com.google.gson.annotations.SerializedName

/**
 * Created by imobdev on 25/2/20
 */
data class ChatAccessTokenResponse(
    @SerializedName("result")val result: AccessToken
):BaseResponse()

data class AccessToken(
    val token: String
)