package com.enviroclean.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.base.BaseViewModel
import com.enviroclean.utils.AppConstants
import com.koushikdutta.ion.Ion
import com.twilio.voice.ConnectOptions
import com.twilio.voice.Voice

/**
 * Created by imobdev on 2/3/20
 */
class VoiceVewModel(application: Application) : BaseViewModel(application) {

    private val sendAccessToken: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }


    fun getAccessToken(): LiveData<String> {
        return sendAccessToken
    }
    fun createAccessToken(context:Context,toCall:String,fromCall:String){
        loadingVisibility.value=true
        val from=  fromCall.replace("\\s".toRegex(), "")
        val to=toCall.replace("\\s".toRegex(), "")
        Ion.with(context).load("${AppConstants.VOICE_ACCESS_TOKEN_URL}?identity=$from").asString()
            .setCallback { e, accessToken ->
                if (e == null) {
                    Log.e("Access token", "Access token: $accessToken")
                    sendAccessToken.value= accessToken
                    loadingVisibility.value=false
                } else {
                    loadingVisibility.value=false
                }
            }
    }
}