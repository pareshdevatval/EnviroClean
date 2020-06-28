package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.NotificationListResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.validator.ValidationErrorModel
import com.enviroclean.utils.validator.Validator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by imobdev on 20/12/19
 */
class NotificationListViewModel(application: Application) : BaseViewModel(application) {
    private var subscription: Disposable? = null


    /*[START] live data of an API response*/
    private val response: MutableLiveData<NotificationListResponse> by lazy {
        MutableLiveData<NotificationListResponse>()
    }

    fun getResponse(): LiveData<NotificationListResponse> {
        return response
    }
    private val responseClearAll: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getResponseClearAll(): LiveData<BaseResponse> {
        return responseClearAll
    }
    /*[END] live data of an API response*/

    /*[START] calling mInputCharacter REST API */
    fun callApi(params: HashMap<String, Any?>, showProgress: Boolean = true) {

        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .getNotificationList(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    fun callApiClearAll(showProgress: Boolean = true) {

        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .clearAllNotification()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponseClearAll, this::handleError)
        } else {
            onInternetError()
        }
    }

    /*[START] handle API response here*/
    private fun handleResponse(response: NotificationListResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it) this.response.value = response
            /*else assign API error message*/
            else
                this.response.value = response
            /*apiErrorMessage.value = response.message*/
        }
    }
    private fun handleResponseClearAll(response: BaseResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it) this.responseClearAll.value = response
            /*else assign API error message*/
            else
                this.responseClearAll.value = response
            /*apiErrorMessage.value = response.message*/
        }
    }
    /*[END] handle API response here*/

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}