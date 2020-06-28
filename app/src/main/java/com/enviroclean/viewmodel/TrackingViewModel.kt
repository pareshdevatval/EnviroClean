package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.CommunityCheckInListResponse
import com.enviroclean.model.LiveTrackingResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by imobdev on 23/12/19
 */
class TrackingViewModel (application: Application) : BaseViewModel(application){
    private var subscription: Disposable? = null
    private val context = getApplication<Application>().applicationContext
    private lateinit var mCommId:String
    private lateinit var mCommSchId: String
    private lateinit var mSelectedDate: String

    private val response: MutableLiveData<CommunityCheckInListResponse> by lazy {
        MutableLiveData<CommunityCheckInListResponse>()
    }

    fun getCommuityCheckInListResponse(): LiveData<CommunityCheckInListResponse> {
        return response
    }


    private val trackResponse: MutableLiveData<LiveTrackingResponse> by lazy {
        MutableLiveData<LiveTrackingResponse>()
    }

    fun getLiveTrackingResponse(): LiveData<LiveTrackingResponse> {
        return trackResponse
    }
    fun getValetList(
        mCommId: String,
        mCommSchId: String,
        mSelectedDate: String,
        showProgress: Boolean = true
    ) {

        this.mCommId=mCommId
        this.mCommSchId=mCommSchId
        this.mSelectedDate=mSelectedDate

        val params: HashMap<String, Any?> = HashMap()
        params[ApiParams.COMM_ID] = mCommId
        params[ApiParams.SCH_ID] = mCommSchId
        params[ApiParams.TYPE] = AppConstants.WORK_TYPE_DAY
        params[ApiParams.DATE] = mSelectedDate

        params[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .geCommunityCheckInList(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    fun getTrackingData(
        mCommId: String,
        mCommSchId: String,
        date: String,
        trackUserId: String,
        showProgress: Boolean = true
    ) {

        val params: HashMap<String, Any?> = HashMap()
        params[ApiParams.COMM_ID] = mCommId
        params[ApiParams.SCH_ID] = mCommSchId
        params[ApiParams.DATE] = date
        params[ApiParams.TRACKUSERID] = trackUserId
        params[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE

        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .getTrackingList(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleTrackResponse, this::handleError)
        } else {
            onInternetError()
        }

    }

    private fun handleTrackResponse(response: LiveTrackingResponse) {
        /*If response is successful, then assign to the response object*/
        this.trackResponse.value = response
    }

    private fun handleResponse(response: CommunityCheckInListResponse) {
        /*If response is successful, then assign to the response object*/

        this.response.value = response
    }

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}