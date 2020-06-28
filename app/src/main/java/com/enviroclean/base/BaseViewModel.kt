package com.enviroclean.base

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.enviroclean.utils.Prefs
import com.enviroclean.R
import com.enviroclean.api.ApiClient
import com.enviroclean.api.ApiService


/**
 * Created by Darshna Desai on 19/12/18.
 */
open class BaseViewModel(application: Application) : AndroidViewModel(application) {

    /*Common error message variable*/
    val errorMessage: MutableLiveData<Int> = MutableLiveData()
    /*API error message variable*/
    val apiErrorMessage: MutableLiveData<String> = MutableLiveData()
    /*progressBar visibility variable*/
    val loadingVisibility: MutableLiveData<Boolean> = MutableLiveData()

    /*General method that will call when start calling API
    * So we can do initialization work here globally and no need to do it every
    * time when we call an API*/
    fun onApiStart() {
        // showing progress bar
        loadingVisibility.value = true
        errorMessage.value = null
    }

    /*Internet error method*/
    fun onInternetError() {
        errorMessage.value = R.string.msg_no_internet
    }

    /*General method for API finishing*/
    fun onApiFinish() {
        loadingVisibility.value = false
    }

    /*General method to get ApiService in every viewModel class*/
    fun getApiService(): ApiService {
        return ApiClient.getApiService(getApplication())
    }

    /*General method to get Prefs in every viewModel class*/
    fun getPrefs() : Prefs? {
        return Prefs.getInstance(getApplication())
    }

    /*[START] handle API error, if thrown in executing an API*/
    protected fun handleError(error: Throwable) {
        error.localizedMessage?.let {
            Log.e("handleError", it)
            apiErrorMessage.value = it
        }
    }
    /*[END] handle API error, if thrown in executing an API*/
}