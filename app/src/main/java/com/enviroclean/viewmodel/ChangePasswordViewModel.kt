package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.PayStubResponse
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.utils.validator.ValidationErrorModel
import com.enviroclean.utils.validator.Validator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by imobdev on 23/12/19
 */
class ChangePasswordViewModel (application: Application) : BaseViewModel(application){

    private var subscription: Disposable? = null

    private val validationError: MutableLiveData<ValidationErrorModel> by lazy {
        MutableLiveData<ValidationErrorModel>()
    }

    fun getValidationError(): LiveData<ValidationErrorModel> {
        return validationError
    }

    private val changePwdSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getChangePwdResponse(): LiveData<Boolean> {
        return changePwdSuccess
    }

    /*[START] live data of an API response*/
    private val response: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getResponse(): LiveData<BaseResponse> {
        return response
    }
    /*[END] live data of an API response*/


    fun checkValidation(
        currentPassword: String?,
        newPassword: String?,
        confirmPassword: String?
    ) {
        Validator.validateCurrentPassword(currentPassword)?.let {
            validationError.value = it
            return
        }
        Validator.validateNewPassword(newPassword)?.let {
            validationError.value = it
            return
        }
        Validator.validateConfirmPassword(newPassword, confirmPassword)?.let {
            validationError.value = it
            return
        }
        changePwdSuccess.value = true
    }



    /*[START] calling mInputCharacter REST API */
    fun callApi(param: HashMap<String, Any?>,showProgress: Boolean = true) {


        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .changePassword(param)
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
            if (it)
                this.response.value = response
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