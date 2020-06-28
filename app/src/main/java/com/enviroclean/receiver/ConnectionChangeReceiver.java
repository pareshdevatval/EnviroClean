package com.enviroclean.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.enviroclean.utils.AppUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by keshu odedara on 13,February,2020
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
    private static boolean firstConnect = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null) {
            if(firstConnect) {
                // do subroutines here
              Log.e("Connection",""+true);
                firstConnect = false;
                EventBus.getDefault().post("is_connected");
            }
        }
        else {
            Log.e("Connection",""+false);
            EventBus.getDefault().post("is_desconnected");
            firstConnect= true;
        }
    }
}