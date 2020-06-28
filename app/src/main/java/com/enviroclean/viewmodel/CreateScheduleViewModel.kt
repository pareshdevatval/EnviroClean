package com.enviroclean.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.R
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseViewModel
import com.enviroclean.model.CommunityListResponse
import com.enviroclean.model.DaysModel
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.validator.Validator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

/**
 * Created by imobdev on 23/12/19
 */
class CreateScheduleViewModel(application: Application) : BaseViewModel(application) {
    private var subscription: Disposable? = null

    private val context = getApplication<Application>().applicationContext
    private val daysList: ArrayList<DaysModel?> = ArrayList()
    private var selectedDays = JSONObject()
    private val validationError: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getValidationError(): LiveData<String> {
        return validationError
    }

    /*[START] live data of an API response*/
    private val response: MutableLiveData<CommunityListResponse> by lazy {
        MutableLiveData<CommunityListResponse>()
    }

    fun getResponse(): LiveData<CommunityListResponse> {
        return response
    }
    /*[END] live data of an API response*/


    private val daysListResponse: MutableLiveData<ArrayList<DaysModel?>> by lazy {
        MutableLiveData<ArrayList<DaysModel?>>()
    }

    fun getDayListResponse(): LiveData<ArrayList<DaysModel?>> {
        return daysListResponse
    }

    private val createScheduleResponse: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getCreateScheduleResponse(): LiveData<Boolean> {
        return createScheduleResponse
    }

    /*[START] calling mInputCharacter REST API */


    fun callApi(showProgress: Boolean = true) {
        val params: HashMap<String ,Any?> = HashMap()
        params[ApiParams.DEVICE_TYPE]= AppConstants.ANDROID_DEVICE_TYPE

        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .getCommunityList(params)
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
    private fun handleResponse(response: CommunityListResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it) this.response.value = response
            /*else assign API error message*/
            else
              //  this.response.value = response
                apiErrorMessage.value = response.message
        }
    }
    /*[END] handle API response here*/


    fun callCreateCommunitySchedule(
        communityID: String,
        scheduleName: String,
        inTime: String,
        outTime: String, days: String, showProgress: Boolean = true
    ) {

        val params: HashMap<String, Any?> = HashMap()
        params[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
        params[ApiParams.COMM_ID] = communityID
        params[ApiParams.SCH_NAME] = scheduleName
        params[ApiParams.SCH_IN_TIME] = inTime
        params[ApiParams.SCH_OUT_TIME] = outTime
        params[ApiParams.SCH_DAYS] = days

        Log.e("IN",inTime)
        Log.e("OUT",outTime)
        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .createCommunitySchedule(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleScheduleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    private fun handleScheduleResponse(response: BaseResponse) {
        /*If response is successful, then assign to the response object*/
        if (response.status) {
            createScheduleResponse.value = response.status
        }
        apiErrorMessage.value = response.message
    }
    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    fun getDaysList() {
        daysList.clear()
        var sun = DaysModel("Sun", false, R.drawable.pink_solid_rounded)
        daysList.add(sun)

        var mon = DaysModel("Mon", false, R.drawable.yellow_solid_rounded)
        daysList.add(mon)

        var tue = DaysModel("Tue", false, R.drawable.bulelight_solid_rounded)
        daysList.add(tue)

        var wed = DaysModel("Wed", false, R.drawable.green_solid_rounded)
        daysList.add(wed)

        var thu = DaysModel("Thu", false, R.drawable.redlight_solid_rounded)
        daysList.add(thu)

        var fri = DaysModel("Fri", false, R.drawable.pink_solid_rounded)
        daysList.add(fri)

        var sat = DaysModel("Sat", false, R.drawable.yellow_solid_rounded)
        daysList.add(sat)

        daysListResponse.value = daysList

    }

    fun checkValidation(
        communityID: String,
        scheduleName: String,
        inTime: String,
        outTime: String
    ) {
        if (Validator.isBlank(communityID)) {
            validationError.value =
                context.resources.getString(R.string.msg_please_select_community)
            return
        }
        if (Validator.isBlank(scheduleName)) {
            validationError.value =
                context.resources.getString(R.string.msg_please_enter_schedule_name)
            return
        }

        if (Validator.isBlank(inTime)) {
            validationError.value = context.resources.getString(R.string.msg_please_select_in_time)
            return
        }

        if (Validator.isBlank(outTime)) {
            validationError.value = context.resources.getString(R.string.msg_please_select_out_time)
            return
        }
        if (!AppUtils.checktimings(inTime, outTime)) {
            validationError.value =
                context.resources.getString(R.string.msg_please_select_valid_time)
            return
        }
        daysSelect()
        if (                                                                                                                                                                                                                                                                                                                                                                                                                                                                                selectedDays.length() == 0) {
            validationError.value = context.resources.getString(R.string.msg_please_select_days)
            return
        }
        callCreateCommunitySchedule(
            communityID,
            scheduleName,
            inTime,
            outTime,
            selectedDays.toString()
        )
    }

    private fun daysSelect() {
        selectedDays = JSONObject()
        for ((index, day) in daysList.withIndex()) {
            if (day!!.isSelected && index == 0) {
                selectedDays.put("Sunday", "1")
            }
            if (day.isSelected && index == 1) {
                selectedDays.put("Monday", "1")
            }
            if (day.isSelected && index == 2) {
                selectedDays.put("Tuesday", "1")
            }
            if (day.isSelected && index == 3) {
                selectedDays.put("Wednesday", "1")
            }
            if (day.isSelected && index == 4) {
                selectedDays.put("Thursday", "1")
            }
            if (day.isSelected && index == 5) {
                selectedDays.put("Friday", "1")
            }
            if (day.isSelected && index == 6) {
                selectedDays.put("Saturday", "1")
            }
        }
        Log.e("DAY", "" + selectedDays.toString())

    }
}