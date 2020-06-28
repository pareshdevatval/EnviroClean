package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.base.BaseViewModel
import com.enviroclean.utils.Prefs

/**
 * Created by imobdev on 18/12/19
 */
class SplashViewModel(application: Application) : BaseViewModel(application) {
    private lateinit var pref: Prefs
    private val context = getApplication<Application>().applicationContext

    private val setUserSession: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

}