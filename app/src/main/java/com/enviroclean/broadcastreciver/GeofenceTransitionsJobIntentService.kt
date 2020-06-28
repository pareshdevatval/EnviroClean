

package com.enviroclean.broadcastreciver

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.api.ApiClient
import com.enviroclean.api.ApiService
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.EnviroClean
import com.enviroclean.model.Reminder
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.AppUtils.sendNotification
import com.enviroclean.utils.Prefs
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class GeofenceTransitionsJobIntentService : JobIntentService() {
    private var subscription: Disposable? = null
    lateinit var prefs: Prefs

    companion object {
        private const val LOG_TAG = "GeoTrIntentService"

        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(
                this,
                geofencingEvent.errorCode
            )
            AppUtils.loge(errorMessage)
            return
        }

        handleEvent(geofencingEvent)
    }

    private fun handleEvent(event: GeofencingEvent) {

        val param: HashMap<String, Any?> = HashMap()
        prefs = Prefs.getInstance(this)!!
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val reminder = getFirstReminder(event.triggeringGeofences)
            val message = reminder?.message
            val latLng = reminder?.latLng
            if (prefs.isCheck) {
                if (message != null && latLng != null) {
                    sendNotification(this, message+" enter")
                    prefs.violationAreaId = reminder.area_id.toString()
                    param[ApiParams.COMM_ID] = prefs.checkingResponse!!.result!!.commId
                    param[ApiParams.SCH_ID] = prefs.checkingResponse!!.result!!.commSchId
                    param[ApiParams.AREA_ID] = reminder.area_id.toString()
                    param[ApiParams.TYPE] = 1
                    param[ApiParams.LATITUDE] = event.triggeringLocation.latitude
                    param[ApiParams.LONGITUDE] = event.triggeringLocation.longitude
                    param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
                    callApi(param)
                    Log.e("TESTING", "----------->" + message + " enter")
                }
            }


        }
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val reminder = getFirstReminder(event.triggeringGeofences)

            val message = reminder?.message
            val latLng = reminder?.latLng
            if (prefs.isCheck) {
                if (message != null && latLng != null) {
                    sendNotification(this, message+" exit")
                    param[ApiParams.COMM_ID] = prefs.checkingResponse!!.result!!.commId
                    param[ApiParams.SCH_ID] = prefs.checkingResponse!!.result!!.commSchId
                    param[ApiParams.AREA_ID] = reminder.area_id.toString()
                    param[ApiParams.TYPE] = 2
                    param[ApiParams.LATITUDE] = event.triggeringLocation.latitude
                    param[ApiParams.LONGITUDE] = event.triggeringLocation.longitude
                    param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
                    callApi(param)
                    if(reminder.area_id.toString()== prefs.violationAreaId){
                        prefs.violationAreaId = ""
                    }
                    Log.e("TESTING", "----------->" + message + " exit")
                }
            }
        }
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL){
            sendNotification(this, "GEOFENCE_TRANSITION_DWELL")
        }
    }

    private fun getFirstReminder(triggeringGeofences: List<Geofence>): Reminder? {
        val firstGeofence = triggeringGeofences[0]
        return (application as EnviroClean).getRepository().get(firstGeofence.requestId)
    }


    private val response: MutableLiveData<BaseResponse> by lazy {
        MutableLiveData<BaseResponse>()
    }

    fun getResponse(): LiveData<BaseResponse> {
        return response
    }

    /*[START] calling mInputCharacter REST API */
    fun callApi(param: HashMap<String, Any?>) {


        if (AppUtils.hasInternet(getApplication())) {
            subscription = getApiService()
                .clockInAndOutApiCall(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {}
                .doOnTerminate {}
                .subscribe(this::handleResponse, this::handleError)
        }
    }

    /*[START] handle API response here*/
    private fun handleResponse(response: BaseResponse) {
        /*If response is successful, then assign to the response object*/
        response.status?.let {
            if (it)

                this.response.value = response
        }
    }
    /*[END] handle API response here*/

    /*General method to get ApiService in every viewModel class*/
    fun getApiService(): ApiService {
        return ApiClient.getApiService(getApplication())
    }

    /*General method to get Prefs in every viewModel class*/

    /*[START] handle API error, if thrown in executing an API*/
    protected fun handleError(error: Throwable) {
        error.localizedMessage?.let {
            Log.e("handleError", it)
        }
    }
}