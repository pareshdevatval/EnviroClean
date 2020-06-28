package com.enviroclean.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.enviroclean.R
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityCheckinBinding
import com.enviroclean.model.CheckIngResponse1
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.utils.filePick.FilePickUtils
import com.enviroclean.viewmodel.CheckInViewModel
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*


class CheckInActivity :
    BaseActivity<CheckInViewModel>(CheckInActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener {

    override fun onLeftIconClicked() {
        onBackPressed()
    }

    private lateinit var binding: ActivityCheckinBinding
    private val mViewModel: CheckInViewModel by lazy {
        ViewModelProviders.of(this).get(CheckInViewModel::class.java)
    }

    override fun getViewModel(): CheckInViewModel {
        return mViewModel
    }

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 2000
    private val FASTEST_INTERVAL: Long = 30000
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest
    private var filePickUtils: FilePickUtils? = null

    private val mCommId: String by lazy {
        intent.getStringExtra(AppConstants.COMM_ID)
    }
    private val mCommSchId: String by lazy {
        intent.getStringExtra(AppConstants.COMM_SCH_ID)
    }
    private val mWorkType: String by lazy {
        intent.getStringExtra(AppConstants.WORK_TYPE)
    }
    private val mSelectedDate: String by lazy {
        intent.getStringExtra(AppConstants.SELECTED_DATE)
    }
    private val mWeek: String by lazy {
        intent.getStringExtra(AppConstants.WEEK)
    }
    private val IsCheckIn: Boolean by lazy {
        intent.getBooleanExtra(AppConstants.IS_CHEKIN, false)
    }
    lateinit var prefs: Prefs

    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    companion object {
        fun newInstance(
            context: Context,
            commId: Int,
            commSchId: Int,
            workTypeDay: String,
            selectedDate: String,
            week: String,
            isCheckIn: Boolean
        ): Intent {
            val intent = Intent(context, CheckInActivity::class.java)
            intent.putExtra(AppConstants.COMM_ID, commId.toString())
            intent.putExtra(AppConstants.COMM_SCH_ID, commSchId.toString())
            intent.putExtra(AppConstants.WORK_TYPE, workTypeDay)
            intent.putExtra(AppConstants.SELECTED_DATE, selectedDate)
            intent.putExtra(AppConstants.WEEK, week)
            intent.putExtra(AppConstants.IS_CHEKIN, isCheckIn)

            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_checkin)
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        mViewModel.getResponse().observe(this, observeCheckinData)
        mViewModel.getResponseChekOut().observe(this, observeChekoutData)
        init()

    }

    private val observeChekoutData = Observer<BaseResponse> {
        if (it.status) {

            if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {
                //checkout manager
                prefs.clearCheckinDate()
                AppUtils.webSocket.cancel()
                AppUtils.client!!.dispatcher.executorService.shutdown()
                ManagerHomeActivity.socketConnectedManager = false
                ManagerHomeActivity.currentFragment = 1
                startActivity(ManagerHomeActivity.newInstance(this))
                AppUtils.startFromRightToLeft(this)
                finishAffinity()
            } else {
                //checkout valets
                prefs.clearCheckinDate()
                AppUtils.webSocket.cancel()
                AppUtils.client!!.dispatcher.executorService.shutdown()
                ValetHomeActivity.socketConnectedValets = false
                prefs.violationAreaSize = ""
                ValetHomeActivity.currentFragment = 1
                startActivity(ValetHomeActivity.newInstance(this))
                AppUtils.startFromRightToLeft(this)
                finishAffinity()
            }
        } else {

            AppUtils.showToast(this, it.message)
            finish()
        }
    }
    private val observeCheckinData = Observer<CheckIngResponse1> {
        if (it.status) {
            prefs.checkingResponse = it
            prefs.isCheck = true
            if (mWorkType == "1") {
                //is checking velates
                prefs.isCheckIn = AppConstants.CHACKIN_VALETS
            } else {
                prefs.isCheckIn = AppConstants.CHACKIN_MANAGER
            }
            if (prefs.isLoggedIn) {
                if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {
                    //check in manager
                    startActivity(
                        ManagerHomeActivity.newInstance(
                            this, false,
                            "", "", true
                        )
                    )
                    AppUtils.startFromRightToLeft(this)
                    finishAffinity()
                } else {
                    //check in valets
                    startActivity(
                        ValetHomeActivity.newInstance(
                            this, false,
                            "", "", true
                        )
                    )
                    AppUtils.startFromRightToLeft(this)
                    finishAffinity()
                }
            }
        } else {
            showDialog(it.message, it.code)

        }
    }

    fun init() {
        showProgress()
        mLocationRequest = LocationRequest()
        prefs = Prefs.getInstance(this)!!

        filePickUtils = FilePickUtils(this, mOnFileChoose)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showPermission()
        } else {
            startLocationUpdates()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPermission() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 10)
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {

        // Create the location request to start receiving updates

        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        builder.setAlwaysShow(true)
        val locationSettingsRequest = builder.build()
        val settingsClient = this.let { LocationServices.getSettingsClient(it) }
        settingsClient!!.checkLocationSettings(locationSettingsRequest)

        /*[START]location enable*/
        val googleApiClient = this.let {
            GoogleApiClient.Builder(it)
                .addApi(LocationServices.API).build()
        }
        googleApiClient!!.connect()
        val result1 =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result1.setResultCallback { result ->
            val status = result.status
            if (status.statusCode == LocationSettingsStatusCodes.SUCCESS) {
                Log.e("SUCCESS", "-------->" + status.statusCode)
            }
            if (status.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                try {
                    status.startResolutionForResult(this as Activity?, 0x1)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("SUCCESS", "-------->" + e.message)
                }

            }
            if (status.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE)
                Log.d(
                    "Martin",
                    "Location settings are inadequate, and cannot be fixed here. Dialog " + "not created."
                )

            /*[END]location enable*/

        }

        mFusedLocationProviderClient =
            this?.let { LocationServices.getFusedLocationProviderClient(it) }
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (this?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && this?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {


            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mOnFileChoose = object : FilePickUtils.OnFileChoose {
        override fun onFileChoose(fileUri: String, requestCode: Int) {
            Log.e("TAG", "------>" + fileUri)
            mViewModel.imagePath = fileUri
            AppUtils.loadImages(this@CheckInActivity, binding.ivCheckIn, fileUri)
            if (IsCheckIn) {
                mViewModel.callApi(
                    mCommId,
                    mCommSchId,
                    mLatitude,
                    mLongitude,
                    mWorkType,
                    mSelectedDate,
                    mWeek
                )
            } else {
                mViewModel.callApiChekOut(mCommId, mCommSchId, mLatitude, mLongitude, mWorkType)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                finish()
            }

        } else {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                filePickUtils?.onRequestPermissionsResult(
                    requestCode,
                    permissions as Array<String>,
                    grantResults
                )
            } else {
                finish()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        filePickUtils?.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {

        } else {
            finish()
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // do work here
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }


    fun onLocationChanged(location: Location) {

        mLastLocation = location

        Log.e("TAG", "LATITUDE---->" + mLastLocation.latitude)
        Log.e("TAG", "LONGITUDE---->" + mLastLocation.longitude)

        mLongitude = mLastLocation.longitude.toString()
        mLatitude = mLastLocation.latitude.toString()

        hideProgress()
        filePickUtils!!.requestImageCamera(101, false, false)
        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }

    lateinit var mLatitude: String
    lateinit var mLongitude: String

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }


    fun showDialog(msg: String, code: Int) {
        val alert = AlertDialog.Builder(this)

        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.you_are_not_under_the_radues, null)
        val btnOk = alertLayout.findViewById<AppCompatTextView>(R.id.tvOk)
        val tvMsg = alertLayout.findViewById<AppCompatTextView>(R.id.tvMsg)
        tvMsg.text = msg
        alert.setView(alertLayout)
        alert.setCancelable(false)

        val dialog = alert.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        btnOk.setOnClickListener {
            dialog.dismiss()
            if (code == 70) {
                finish()
            } else {
                finish()
                startActivity(intent)
            }
        }

    }

}
