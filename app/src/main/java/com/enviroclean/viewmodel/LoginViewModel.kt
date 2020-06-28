package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.LoginResponse
import com.enviroclean.model.LoginResponse1
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.utils.validator.ValidationErrorModel
import com.enviroclean.utils.validator.Validator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by imobdev on 18/12/19
 */
class LoginViewModel(application: Application) : BaseViewModel(application) {

    private var subscription: Disposable? = null

    private val context = getApplication<Application>().applicationContext
    private val validationError: MutableLiveData<ValidationErrorModel> by lazy {
        MutableLiveData<ValidationErrorModel>()
    }

    fun getValidationError(): LiveData<ValidationErrorModel> {
        return validationError
    }

    private val loginSuccess: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getLoginResponse(): LiveData<Boolean> {
        return loginSuccess
    }


    private val rememberMe: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getRememberMe(): LiveData<Boolean> {
        return rememberMe
    }

    /*[START] live data of an API response*/
    private val response: MutableLiveData<LoginResponse1> by lazy {
        MutableLiveData<LoginResponse1>()
    }

    fun getResponse(): LiveData<LoginResponse1> {
        return response
    }
    /*[END] live data of an API response*/

    /*[START] check validation for login*/
    fun validateUserInputs(
        prefs: Prefs,
        emailOrCellPhone: String?,
        password: String?,
        isRememberMe: Boolean
    ) {
        val number = emailOrCellPhone!!.toDoubleOrNull()
        val isInteger = number != null
        if (isInteger) {
            Validator.validateEmailOrCellPhone(emailOrCellPhone)?.let {
                validationError.value = it
                return
            }
        } else {
            Validator.validateEmail(emailOrCellPhone)?.let {
                validationError.value = it
                return
            }
        }
        Validator.validatePassword(password)?.let {
            validationError.value = it
            return
        }

        prefs.rememberMe = isRememberMe
        prefs.emailOrPhone = emailOrCellPhone
        prefs.password = password
        callApi(emailOrCellPhone,password,prefs.firebaseToken)
    }
    /*[END] check validation for login*/

    /*[START] calling mInputCharacter REST API */
    fun callApi(emailOrCellPhone: String?, password: String?,token: String?, showProgress: Boolean = true) {
        val params: HashMap<String, String?> = HashMap()
        params[ApiParams.U_USERNAME] = emailOrCellPhone
        params[ApiParams.U_PASSWORD] = password
        params[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
        params[ApiParams.DEVICE_TOKEN] = token
        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .loginApi(params)
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
    private fun handleResponse(response: LoginResponse1) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it) this.response.value = response
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