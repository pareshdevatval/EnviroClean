package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseViewModel
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.validator.ValidationErrorModel
import com.enviroclean.utils.validator.Validator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by imobdev on 19/12/19
 */
class ForgotPasswordViewModel(application: Application) : BaseViewModel(application) {

    private var subscription: Disposable? = null

    private val validationError: MutableLiveData<ValidationErrorModel> by lazy {
        MutableLiveData<ValidationErrorModel>()
    }

    fun getValidationError(): LiveData<ValidationErrorModel> {
        return validationError
    }

    /*[START] check validation for login*/
    fun validateUserInputs(email: String?): Boolean {
        Validator.validateEmailAddressSecond(email)?.let {
            validationError.value = it
            return false
        }
        callApi(email)
        return true
    }
    /*[END] check validation for login*/
    /*[START] live data of an API response*/
    private val response: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getResponse(): LiveData<BaseResponse> {
        return response
    }
    /*[END] live data of an API response*/

    /*[START] calling mInputCharacter REST API */
    fun callApi(email: String?, showProgress: Boolean = true) {
        val params: HashMap<String, String?> = HashMap()
        params[ApiParams.U_EMAIL] = email
        params[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE

        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .forgotPasswordApi(params)
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
    private fun handleResponse(response: BaseResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it) this.response.value = response
            /*else assign API error message*/
            else apiErrorMessage.value = response.message
        }
    }
    /*[END] handle API response here*/

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }
}