package com.enviroclean.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.CurrentCommunityUsers
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by imobdev on 20/12/19
 */
class ManagerCommuntyChatViewModel(application: Application) : BaseViewModel(application)  {
    private var subscription: Disposable? = null

    private val context = getApplication<Application>().applicationContext


    private val rememberMe: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getRememberMe(): LiveData<Boolean> {
        return rememberMe
    }

    /*[START] live data of an API response*/
    private val response: MutableLiveData<CurrentCommunityUsers> by lazy {
        MutableLiveData<CurrentCommunityUsers>()
    }

    fun getResponse(): LiveData<CurrentCommunityUsers> {
        return response
    }
    /*[END] live data of an API response*/


    /*[START] calling mInputCharacter REST API */
    fun callApi(params: HashMap<String,Any?>, showProgress: Boolean = true) {

        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .getCurrentCommunityUsers(params)
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
    private fun handleResponse(response: CurrentCommunityUsers) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it)
                this.response.value = response
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