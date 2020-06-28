package com.enviroclean.iterfacea

import android.util.Log
import com.twilio.chat.ErrorInfo
import com.twilio.chat.StatusListener

/**
 * Created by imobdev on 27/2/20
 */
internal class TwilioFcmStatusListener(private val okText: String, private val errorText: String) :
    StatusListener() {
    override fun onSuccess() {
        Log.e("FCM TWILIO", okText)
    }

    override fun onError(errorInfo: ErrorInfo?) {
        Log.e("FCM TWILIO error", errorText)
    }
}