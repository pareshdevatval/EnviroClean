package com.enviroclean.viewmodel

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.ViolationResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.validator.ValidationErrorModel
import com.enviroclean.utils.validator.Validator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File

/**
 * Created by imobdev on 20/12/19
 */
class ManagerViolationViewModel(application: Application) : BaseViewModel(application) {

    private var subscription: Disposable? = null
    var imageZipPath: String? = ""
    /*[START] live data of an API response*/
    private val response: MutableLiveData<ViolationResponse> by lazy {
        MutableLiveData<ViolationResponse>()
    }

    fun getResponse(): LiveData<ViolationResponse> {
        return response
    }
    /*[END] live data of an API response*/

    /*[START] calling mInputCharacter REST API */
    fun callApi(params: HashMap<String, String?>, showProgress: Boolean = true) {

        val parameters = getParamsRequestBody(params)
        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .violationApi(parameters)
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
    private fun handleResponse(response: ViolationResponse) {
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

        if (!TextUtils.isEmpty(imageZipPath)) {
            val file = File(imageZipPath)
            val reqFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
            val imageParams = ApiParams.VIO_IMAGES + "\";filename=\"${file.name}\""
            resultParams[imageParams] = reqFile
        }
        return resultParams
    }
}