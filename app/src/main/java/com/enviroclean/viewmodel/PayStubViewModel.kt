package com.enviroclean.viewmodel

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.PayStubResponse
import com.enviroclean.model.UserDetailsResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File

/**
 * Created by imobdev on 23/12/19
 */
class PayStubViewModel(application: Application) : BaseViewModel(application) {
    private var subscription: Disposable? = null

    private val context = getApplication<Application>().applicationContext


    private val rememberMe: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getRememberMe(): LiveData<Boolean> {
        return rememberMe
    }

    /*[START] live data of an API response*/
    private val response: MutableLiveData<PayStubResponse> by lazy {
        MutableLiveData<PayStubResponse>()
    }

    fun getResponse(): LiveData<PayStubResponse> {
        return response
    }
    /*[END] live data of an API response*/


    /*[START] calling mInputCharacter REST API */
    fun callApi(param: HashMap<String, Any?>,showProgress: Boolean = true) {


        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .getPayStub(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    /*[START] handle API response here*/
    private fun handleResponse(response: PayStubResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it)
                this.response.value = response
            /*else assign API error message*/
            else apiErrorMessage.value = response.message
        }
    }
    /*[END] handle API response here*/

    /*[END] calling mInputCharacter REST API */
    fun checkRememberMe(prefs: Prefs) {
        rememberMe.value = prefs.rememberMe
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }


}