package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.CommunityListResponse
import com.enviroclean.model.ListOfScheduleResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by imobdev on 28/1/20
 */
class ListOfScheduleViewModel(application: Application) : BaseViewModel(application) {
    private var subscription: Disposable? = null
    private val context = getApplication<Application>().applicationContext
    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }


    /*[START] live data of an API response*/
    private val response: MutableLiveData<ListOfScheduleResponse> by lazy {
        MutableLiveData<ListOfScheduleResponse>()
    }

    fun getResponse(): LiveData<ListOfScheduleResponse> {
        return response
    }
    /*[END] live data of an API response*/

    fun getScheduleListData(page:Int,commId :String,showProgress: Boolean = true) {
        val params: HashMap<String ,Any?> = HashMap()
        params[ApiParams.DEVICE_TYPE]= AppConstants.ANDROID_DEVICE_TYPE
        params[ApiParams.COMM_ID]= commId
        params[ApiParams.PAGE]= page


        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .getListOfScheduleList(params)
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
    private fun handleResponse(response: ListOfScheduleResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            this.response.value = response

        }
    }
    /*[END] handle API response here*/

}