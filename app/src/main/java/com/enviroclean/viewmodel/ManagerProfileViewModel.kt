package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.CurrentCommunityUsers
import com.enviroclean.model.UserDetailsResponse
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
class ManagerProfileViewModel(application: Application) : BaseViewModel(application) {
    private var subscription: Disposable? = null

    private val context = getApplication<Application>().applicationContext


    private val rememberMe: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getRememberMe(): LiveData<Boolean> {
        return rememberMe
    }

    /*[START] live data of an API response*/
    private val response: MutableLiveData<UserDetailsResponse> by lazy {
        MutableLiveData<UserDetailsResponse>()
    }

    fun getResponse(): LiveData<UserDetailsResponse> {
        return response
    }
    private val responseLogout: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getResponseLogout(): LiveData<BaseResponse> {
        return responseLogout
    }
    /*[END] live data of an API response*/


    /*[START] calling mInputCharacter REST API */
    fun callApi(showProgress: Boolean = true) {
        val param: HashMap<String, Any?> = HashMap()

        param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE

        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .getUserDetails(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }
    fun logOutApi(showProgress: Boolean = true) {
        val param: HashMap<String, Any?> = HashMap()

        param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE

        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .logOutApi()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponseLogOut, this::handleError)
        } else {
            onInternetError()
        }
    }

    /*[START] handle API response here*/
    private fun handleResponse(response: UserDetailsResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it)
                this.response.value = response
            /*else assign API error message*/
            else apiErrorMessage.value = response.message
        }
    }

    private fun handleResponseLogOut(response: BaseResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it)
                this.responseLogout.value = response
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