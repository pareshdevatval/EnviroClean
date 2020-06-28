package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.ChatAccessTokenResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by imobdev on 20/12/19
 */
class ManagerChatViewModel(application: Application) : BaseViewModel(application) {
    private var subscription: Disposable? = null
    private val response: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getTwilioAccessResponse(): LiveData<String> {
        return response
    }

    fun callGetTwilioAccessTokenAPI(userName: String, showProgress: Boolean = true) {
        if (AppUtils.hasInternet(getApplication())) {

            val params: HashMap<String, Any?> = HashMap()
            params[ApiParams.KEY_IDENTITY] = userName
            params[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
            subscription = getApiService()
                .getTwilioAccessToken(params)
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
    private fun handleResponse(response: ChatAccessTokenResponse) {
        /*If response is successful, then assign to the response object*/
        if (response.status){
            this.response.value=response.result.token
        }else{
            apiErrorMessage.value = response.message
        }
    }
    /*[END] handle API response here*/

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}