package com.enviroclean.base

import android.app.Application
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.net.ConnectivityManager
import com.enviroclean.broadcastreciver.ReminderRepository
import com.enviroclean.receiver.ConnectionChangeReceiver


/**
 * Created by imobdev-paresh on 23,January,2020
 */

class EnviroClean : Application() {
    private var mNetworkReceiver: BroadcastReceiver? = null
    private lateinit var repository: ReminderRepository

    override fun onCreate() {
        super.onCreate()
        //Fabric.with(this, Crashlytics())
        repository = ReminderRepository(this)
        mNetworkReceiver = ConnectionChangeReceiver()
        registerNetworkBroadcastForNougat()
    }

    private fun registerNetworkBroadcastForNougat() {
        registerReceiver(
            mNetworkReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    protected fun unregisterNetworkChanges() {
        try {
            unregisterReceiver(mNetworkReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

    }
    fun getRepository() = repository
    override fun onTerminate() {
        super.onTerminate()
        unregisterNetworkChanges()
    }
}