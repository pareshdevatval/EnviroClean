package com.enviroclean.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import org.greenrobot.eventbus.EventBus


/**
 * Created by imobdev-paresh on 23,January,2020
 */
class NetworkChangeReceiver: BroadcastReceiver() {
    var isConnected:Boolean=true
    lateinit var prefs: Prefs
    override fun onReceive(context: Context, intent: Intent) {


            prefs= Prefs.getInstance(context)!!
            if (AppUtils.hasInternet(context))
            {
                if (isConnected){
                    isConnected=false
                    EventBus.getDefault().post("is_connected")
                    Log.e("INTERNET_CONN", "Online Connect Intenet ")
                }
            }
            else
            {
                if (!isConnected){
                    isConnected=true
                    if(prefs.isCheck){
                        AppUtils.client!!.dispatcher.executorService.shutdown()
                    }
                    EventBus.getDefault().post(false)
                    Log.e("INTERNET_CONN", "Conectivity Failure !!! ")
                }
            }
    }

}