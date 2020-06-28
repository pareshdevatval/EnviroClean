package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.ListOfValetsResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by imobdev on 29/1/20
 */
class ListOfValetsViewModel(application: Application) : BaseViewModel(application) {

    private var subscription: Disposable? = null
    private val context = getApplication<Application>().applicationContext
    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    /*[START] live data of an API response*/
    private val response: MutableLiveData<ListOfValetsResponse> by lazy {
        MutableLiveData<ListOfValetsResponse>()
    }

    fun getResponse(): LiveData<ListOfValetsResponse> {
        return response
    }
    /*[END] live data of an API response*/

    /*[START] live data of an API response*/
    private val valetAssignresponse: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getValetAssignResponse(): LiveData<Boolean> {
        return valetAssignresponse
    }
    /*[END] live data of an API response*/

    fun getValetList(
        comm_id: String,
        sch_id: String,
        page: String,
        searchkey: String,
        showProgress: Boolean = true
    ) {
        val params: HashMap<String, Any?> = HashMap()
        params[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
        params[ApiParams.COMM_ID] = comm_id
      //  params[ApiParams.PAGE] = page
        params[ApiParams.SCH_ID] = sch_id
        if (searchkey.isNotEmpty()) {
            params[ApiParams.SEARCH_KEY] = searchkey
        }

        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .getListOfValets(params)
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
    private fun handleResponse(response: ListOfValetsResponse) {
        /*If response is successful, then assign to the response object*/
        this.response.value = response
    }
    /*[END] handle API response here*/

    fun callAssignValet(
        comm_id: String,
        sch_id: String,
        valetId: String,
        showProgress: Boolean = true
    ) {
        val params: HashMap<String, Any?> = HashMap()
        params[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
        params[ApiParams.COMM_ID] = comm_id
        params[ApiParams.SCH_ID] = sch_id
        params[ApiParams.ASSIGN_U_ID] = valetId


        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .assignValets(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleValetResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    /*[START] handle API response here*/
    private fun handleValetResponse(response: BaseResponse) {
        if (response.status){
            valetAssignresponse.value=response.status
        }
        apiErrorMessage.value = response.message
    }
    /*[END] handle API response here*/

}