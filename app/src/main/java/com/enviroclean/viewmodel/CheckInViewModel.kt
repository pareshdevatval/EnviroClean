package com.enviroclean.viewmodel

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.CheckIngResponse1
import com.enviroclean.model.CheckingResponse
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
class CheckInViewModel (application: Application) : BaseViewModel(application){

    private var subscription: Disposable? = null
    var imagePath: String? = ""

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
    private val responseChekOut: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getResponseChekOut(): LiveData<BaseResponse> {
        return responseChekOut
    }
    /*[END] live data of an API response*/


    /*[START] calling mInputCharacter REST API */
    fun callApi(
        mCommId: String,
        mCommSchId: String,
        latitude: String,
        longitude: String,
        mWorkType: String,
        mSelectedDate: String,
        mWeek: String,
        showProgress: Boolean = true
    ) {
        val params: HashMap<String ,String?> = HashMap()
        params[ApiParams.COMM_ID]=mCommId
        params[ApiParams.SCH_ID]=mCommSchId
        params[ApiParams.LATITUDE]=latitude
        params[ApiParams.LONGITUDE]=longitude
        params[ApiParams.TYPE]=mWorkType
        if(mWorkType==AppConstants.WORK_TYPE_DAY){
            params[ApiParams.DATE]=mSelectedDate
        }else{
            params[ApiParams.WEEK_NO]=mWeek
            params[ApiParams.MONTH]=mSelectedDate
        }
        params[ApiParams.DEVICE_TYPE]= AppConstants.ANDROID_DEVICE_TYPE
        val parameters = getParamsRequestBody(params)
        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .checkinApi(parameters)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    fun callApiChekOut(
        mCommId: String,
        mCommSchId: String,
        latitude: String,
        longitude: String,
        mWorkType: String,
        showProgress: Boolean = true
    ) {
        val param: HashMap<String ,String?> = HashMap()
        param[ApiParams.LATITUDE] = latitude
        param[ApiParams.LONGITUDE] = longitude
        param[ApiParams.COMM_ID] = mCommId
        param[ApiParams.SCH_ID] = mCommSchId
        param[ApiParams.TYPE] = mWorkType
        param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE

        val parameters = getParamsRequestBody(param)
        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .checkOutApi(parameters)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponseChekOut, this::handleError)
        } else {
            onInternetError()
        }
    }

    /*[START] handle API response here*/
    private fun handleResponse(response: CheckIngResponse1) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it) this.response.value = response
            /*else assign API error message*/
            else{
                this.response.value = response
             //   apiErrorMessage.value = response.message
            }

        }
    }

    private fun handleResponseChekOut(response: BaseResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it) this.responseChekOut.value = response
            /*else assign API error message*/
            else{
                this.responseChekOut.value = response
               // apiErrorMessage.value = response.message
            }

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

    private fun getParamsRequestBody(params: HashMap<String, String?>): HashMap<String, RequestBody> {
        val resultParams = HashMap<String, RequestBody>()

        for ((key, value) in params) {
            val body = value?.let { RequestBody.create("text/plain".toMediaTypeOrNull(), it) }
            body?.let { resultParams.put(key, it) }
        }

        if (!TextUtils.isEmpty(imagePath)) {
            val file = File(imagePath)
            val reqFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
            val imageParams = ApiParams.USER_IMGES + "\";filename=\"${file.name}\""
            resultParams[imageParams] = reqFile
        }
        return resultParams
    }
}