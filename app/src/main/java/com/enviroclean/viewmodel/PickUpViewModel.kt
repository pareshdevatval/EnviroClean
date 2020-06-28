package com.enviroclean.viewmodel

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.CheckingResponse
import com.enviroclean.model.DustBinScanResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
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
class PickUpViewModel(application: Application) : BaseViewModel(application) {
    private var subscription: Disposable? = null

    private val context = getApplication<Application>().applicationContext


    private val rememberMe: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getRememberMe(): LiveData<Boolean> {
        return rememberMe
    }

    /*[START] live data of an API response*/
    private val response: MutableLiveData<DustBinScanResponse> by lazy {
        MutableLiveData<DustBinScanResponse>()
    }

    fun getResponse(): LiveData<DustBinScanResponse> {
        return response
    }
    /*[END] live data of an API response*/


    /*[START] calling mInputCharacter REST API */
    fun callApi(
        mCommId: String,
        mCommSchId: String,
        latitude: String,
        longitude: String,
        mQrCode: String,
        mQrCodeId: String,
        showProgress: Boolean = true
    ) {
        val params: HashMap<String ,String?> = HashMap()
        params[ApiParams.SC_COMM_ID]=mCommId
        params[ApiParams.SC_SCH_ID]=mCommSchId
        params[ApiParams.SC_LATITUDE]=latitude
        params[ApiParams.SC_LONGITUDE]=longitude
        params[ApiParams.SC_QRCODE]=mQrCode
        params[ApiParams.QR_ID]=mQrCodeId
        if(getPrefs()!!.checkingResponse!!.result!!.checkinType==2){
            params[ApiParams.WEEK_NO]=getPrefs()!!.checkingResponse!!.result!!.weekno.toString()
        }else{
            params[ApiParams.WEEK_NO]=""
        }
        params[ApiParams.TYPE]=getPrefs()!!.checkingResponse!!.result!!.checkinType.toString()
        params[ApiParams.DEVICE_TYPE]= AppConstants.ANDROID_DEVICE_TYPE

        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .scanApi(params)
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
    private fun handleResponse(response: DustBinScanResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it) this.response.value = response
            /*else assign API error message*/
            else
                this.response.value = response
                apiErrorMessage.value = response.message
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