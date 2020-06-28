package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.LoginResponse
import com.enviroclean.model.WorkListResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.utils.validator.ValidationErrorModel
import com.enviroclean.utils.validator.Validator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by imobdev on 19/12/19
 */
class ManagerValetWorkViewModel(application: Application) : BaseViewModel(application) {

    private var subscription: Disposable? = null

    private val context = getApplication<Application>().applicationContext


    private val rememberMe: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getRememberMe(): LiveData<Boolean> {
        return rememberMe
    }

    /*[START] live data of an API response*/
    private val response: MutableLiveData<WorkListResponse> by lazy {
        MutableLiveData<WorkListResponse>()
    }

    fun getResponse(): LiveData<WorkListResponse> {
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

    fun callApi(params: HashMap<String,Any?>, showProgress: Boolean = true) {

        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .getValetWorkListApi(params)
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

        if (AppUtils.hasInternet(getApplication())) {
           /* subscription = getApiService()
                .checkOutApi(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponseCheckout, this::handleError)*/
        } else {
            onInternetError()
        }
    }


    /*[START] handle API response here*/
    private fun handleResponse(response: WorkListResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            this.response.value = response
            //if (it) this.response.value = response
            /*else assign API error message*/
          //  else
            //    apiErrorMessage.value = response.message
        }
    }
    /*[END] handle API response here*/
/*[START] handle API response here*/
    private fun handleResponseCheckout(response: BaseResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it)

                this.responseCheckOut.value = response
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