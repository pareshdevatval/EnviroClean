package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.CheckIngResponse1
import com.enviroclean.model.CheckingResponse
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by imobdev on 19/12/19
 */
class ManagerVelatesWorkCheckingViewModel(application: Application) : BaseViewModel(application) {

    private var subscription: Disposable? = null

    private val context = getApplication<Application>().applicationContext


    private val rememberMe: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getRememberMe(): LiveData<Boolean> {
        return rememberMe
    }

    /*[START] live data of an API response*/
    private val response: MutableLiveData<CheckIngResponse1> by lazy {
        MutableLiveData<CheckIngResponse1>()
    }

    fun getResponse(): LiveData<CheckIngResponse1> {
        return response
    }
    /*[END] live data of an API response*/

    /*[START] live data of an API response checkout*/
    private val responseCheckOut: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getResponseCheckOut(): LiveData<BaseResponse> {
        return responseCheckOut
    }
    /*[END] live data of an API response checkout*/


    fun callApi(params: HashMap<String, Any?>, showProgress: Boolean = true) {

        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .getCheckInCommunityWorkListApi(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    fun callApiCheckout(params: HashMap<String, Any?>, showProgress: Boolean = true) {

    }

    /*[START] handle API response here*/
    private fun handleResponse(response: CheckIngResponse1) {
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