package com.enviroclean.ui.fragment.valet

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.enviroclean.R
import com.enviroclean.adapter.DustBinListAdapter
import com.enviroclean.adapter.ValetsWorkCheckIngUserListAdapter
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseFragment
import com.enviroclean.databinding.FragmentValetWorkCheckingBinding
import com.enviroclean.iterfacea.CheckInValetsClick
import com.enviroclean.iterfacea.MessageInterface
import com.enviroclean.iterfacea.ReconnectSocketInterface
import com.enviroclean.model.AreaDataModel
import com.enviroclean.model.CheckIngResponse1
import com.enviroclean.model.Reminder
import com.enviroclean.ui.activity.*
import com.enviroclean.ui.activity.ValetHomeActivity.Companion.currentFragment
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.ValetWorkViewModel
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set


/**
 * Created by imobdev on 19/12/19
 */
class ValetWorkCheckingFragment : BaseFragment<ValetWorkViewModel>(), View.OnClickListener,
    MessageInterface, ReconnectSocketInterface,
    BaseBindingAdapter.ItemClickListener<AreaDataModel>, CheckInValetsClick {
    override fun setValetClick(
        position: Int,
        item: CheckIngResponse1.Result.CommCheckinValet
    ) {

        startActivity(
            ChatActivity.newInstance(
                context!!,
                item.uFirstName + " " + item.uLastName
                , item.u_channel_id, item.uId.toString(), "enviroclean-"+item.uId, item.uImage, item.uId
            )
        )
    }

    override fun onItemClick(view: View, data: AreaDataModel?, position: Int) {

        if (data!!.qrType == AppConstants.REMAINING) {
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(
                    context as ValetHomeActivity,
                    arrayOf<String>(Manifest.permission.CAMERA),
                    1022
                )
                return
            }
            (context as ValetHomeActivity).startActivityForResult(
                PickUpActivity.newInstance(
                    context!!, data.qrId,
                    prefs.checkingResponse!!.result!!.commId,
                    prefs.checkingResponse!!.result!!.commSchId,
                    data.qrCode,
                    data.qrText
                ), 99
            )
        }
    }

    override fun reconnected() {

    }

    override fun massagesRecived(jsonObject: JSONObject?) {
        Log.e("SOCKET_CODE","--->"+jsonObject!!.getInt("code"))
        EventBus.getDefault().post(jsonObject)
        updateQRCodeStatus(jsonObject)
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.ivCheckOut -> {
                    if(SplashActivity.REMANING_COUNT=="0"){
                        startActivity(CheckInActivity.newInstance(context!!,prefs.checkingResponse!!.result!!.commId,
                            prefs.checkingResponse!!.result!!.commSchId,
                            prefs.checkingResponse!!.result!!.checkinType.toString(),
                            "","",false))
                    }else{
                        showCheckOutButton()
                    }

                }

            }
        }
    }

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 2000
    private val FASTEST_INTERVAL: Long = 1000
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest

    private lateinit var binding: FragmentValetWorkCheckingBinding
    lateinit var prefs: Prefs
    var qrCodesList: ArrayList<CheckIngResponse1.Result.CommQrcode?> = ArrayList()
    lateinit var checkinResponse: CheckIngResponse1.Result
    private val mViewModel: ValetWorkViewModel by lazy {
        ViewModelProviders.of(this).get(ValetWorkViewModel::class.java)
    }
    lateinit var geofencingClient: GeofencingClient
    override fun getViewModel(): ValetWorkViewModel {
        return mViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentValetWorkCheckingBinding.inflate(inflater, container, false)

        return binding.root
    }

    companion object {
      //  var VIO_REASON_ID:String=""
        fun newInstance(): ValetWorkCheckingFragment {
            val bundle = Bundle()
            val fragment = ValetWorkCheckingFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        init()
        setToolbar()
    }

    override fun onResume() {
        super.onResume()
        setNotificationCount(binding.toolbar,prefs.notificationCount!!)

    }
    private fun init() {
        geofencingClient = LocationServices.getGeofencingClient(context!!)

        mLocationRequest = LocationRequest()
        binding.ivMap.visibility=View.GONE
        prefs = Prefs.getInstance(context!!)!!
        checkinResponse = prefs.checkingResponse!!.result!!
        mViewModel.getResponseCheckOut().observe(this, observeCheckOut)
        binding.ivCheckOut.setOnClickListener(this)
        binding.tvViolationCount.setOnClickListener(this)
        setWorkAdapter()
        setHeaderData()
        if (!ValetHomeActivity.socketConnectedValets) {
            socketRegister()
        }
        fabFilter()
        if(prefs.violationAreaSize!!!=checkinResponse.comm_areas.size.toString()){
            geoFencing()
        }
        scanQRUsingDevice()
    }



    @Subscribe
    fun onEvent(isConnected: String) {
        if(isConnected==AppConstants.IS_CONNECTED){
            if (!ValetHomeActivity.socketConnectedValets&&prefs.isCheck) {
                socketRegister()
            }
        }
        if(isConnected==AppConstants.NOTIFICATION_COUNT){
            setNotificationCount(binding.toolbar,prefs.notificationCount!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
            EventBus.getDefault().unregister(this)
    }
    var coutAddDataGeoFanching=0

    private fun geoFencing() {

        val reminder = Reminder(area_id = null, latLng = null, radius = null, message = null)

        val areaList = checkinResponse.comm_areas


        for (i in coutAddDataGeoFanching until areaList.size) {
            coutAddDataGeoFanching += 1
            val latLong = LatLng(
                areaList[i]!!.area_latitude.toDouble(),
                areaList[i]!!.area_longitude.toDouble()
            )

            reminder.area_id=areaList[i]!!.area_id.toString()
            reminder.message=areaList[i]!!.area_name
            reminder.latLng=latLong
            reminder.radius=areaList[i]!!.area_range.toDouble()
            addReminder(reminder)
            Log.e("LATLONG","--->"+latLong)
            Log.e("message","--->"+areaList[i]!!.area_name)
            Log.e("id","--->"+areaList[i]!!.area_id.toString())
            Log.e("radius","--->"+areaList[i]!!.area_range.toDouble())
            break

        }
    }

    private fun addReminder(reminder: Reminder) {
        getRepository().add(reminder,
            success = {
                activity!!.setResult(Activity.RESULT_OK)
                val reminder1 = Reminder(area_id = null, latLng = null, radius = null, message = null)

                val areaList = checkinResponse.comm_areas
                activity!!.setResult(Activity.RESULT_OK)
                prefs.violationAreaSize=checkinResponse.comm_areas.size.toString()
                Log.e("addReminder","success")
                if(coutAddDataGeoFanching!= areaList.size){
                    for (i in coutAddDataGeoFanching until areaList.size) {
                        coutAddDataGeoFanching += 1
                        val latLong = LatLng(
                            areaList[i]!!.area_latitude.toDouble(),
                            areaList[i]!!.area_longitude.toDouble()
                        )

                        reminder1.area_id=areaList[i]!!.area_id.toString()
                        reminder1.message=areaList[i]!!.area_name
                        reminder1.latLng=latLong
                        reminder1.radius=areaList[i]!!.area_range.toDouble()
                        addReminder(reminder1)
                        Log.e("LATLONG","--->"+latLong)
                        Log.e("message","--->"+areaList[i]!!.area_name)
                        Log.e("id","--->"+areaList[i]!!.area_id.toString())
                        Log.e("radius","--->"+areaList[i]!!.area_range.toDouble())
                        break

                    }
                }
                prefs.violationAreaSize=checkinResponse.comm_areas.size.toString()
            },
            failure = {
                Snackbar.make(binding.view, it, Snackbar.LENGTH_LONG).show()
            })
    }
    private fun socketRegister() {
        if(context==null){
            handler.removeCallbacks(checkSocketConnection)
            handler.removeCallbacksAndMessages(null)
            return
        }
        context?.let {
            AppUtils.startSocketConnection("subscribe", checkinResponse.commId.toString(),
                context!!, this@ValetWorkCheckingFragment,this@ValetWorkCheckingFragment)
        }

        val jsonObject = JSONObject()
        try {
            jsonObject.put("command", "register")
            jsonObject.put("userId", prefs.userDataModel!!.logindata.uId)
            AppUtils.webSocket.send(jsonObject.toString())
            Log.e("register_socket", "--->" + jsonObject.toString())
            handler.removeCallbacks(checkSocketConnection)
            handler.removeCallbacksAndMessages(null)

            retryConnectionSocket()
        } catch (e: JSONException) {
            e.printStackTrace()
        }


    }
    val handler = Handler()
    private fun retryConnectionSocket() {
        handler.postDelayed(checkSocketConnection, 5000)
    }


    private val checkSocketConnection = object:Runnable {
        public override fun run() {
            if (!ValetHomeActivity.socketConnectedValets) {
                socketRegister()
            }
            handler.postDelayed(this, 5000)
        }
    }

    var isCheckOut: Boolean = false
    lateinit var mQRCodeScan: String
    private fun scanQRUsingDevice() {
        val imm = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.scanQr, InputMethodManager.SHOW_IMPLICIT)

        binding.scanQr.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                isCheckOut = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    showPermission()
                } else {
                    startLocationUpdates()
                }
                return@OnKeyListener true
            }
            false
        })
        binding.scanQr.isFocusableInTouchMode = true
        binding.scanQr.requestFocus()
        binding.scanQr.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                mQRCodeScan = p0.toString().trim().replace("\\", "")

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.scanQr.visibility == View.GONE
            }
        })
    }
    private val observeCheckOut = Observer<BaseResponse> {
        if (it.status) {
            prefs.clearCheckinDate()
            AppUtils.webSocket.cancel()
            AppUtils.client!!.dispatcher.executorService.shutdown()
            ValetHomeActivity.socketConnectedValets = false
            prefs.violationAreaSize = ""
            context!!.startActivity(ValetHomeActivity.newInstance(context!!,
                true,"","", isDisplayPopUp = false, isDisplayCheckOutPopUp = true
            ))

        }
    }
    val areaList:ArrayList<AreaDataModel?> = ArrayList()
    private fun setHeaderData() {
        AppUtils.countDownStart(prefs.checkingResponse!!.result!!.commOuttime, binding.tvTime,binding.clTime)
        val checkinResult = prefs.checkingResponse!!.result
        binding.tvCommunityName.text = checkinResult!!.commSchName
        binding.tvViolationCount.text = "" + checkinResult!!.commVioCount
        binding.tvCommunityCount.text = "" + checkinResult.commRemainCount
        SplashActivity.REMANING_COUNT =checkinResult.commRemainCount.toString()
    }

    private fun setWorkAdapter() {
        for(i in 0 until checkinResponse.commQrcodes.size){
            if(checkinResponse.commQrcodes[i]!!.qrMandatory==1){
                areaList.add(
                    AreaDataModel(checkinResponse.commQrcodes[i]!!.qrType,
                        checkinResponse.commQrcodes[i]!!.qrId,
                        checkinResponse.commQrcodes[i]!!.qrText
                        ,checkinResponse.commQrcodes[i]!!.qrCode,
                        checkinResponse.commQrcodes[i]!!.username)
                )
            }
        }
        binding.rvUserList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val adapter = ValetsWorkCheckIngUserListAdapter(this)

        binding.rvUserList.adapter = adapter

        binding.rvCommunityBuilding.layoutManager = LinearLayoutManager(context)
        val communityAdapter = DustBinListAdapter()
        communityAdapter.itemClickListener=this
        binding.rvCommunityBuilding.adapter = communityAdapter
        qrCodesList = checkinResponse.commQrcodes

        (binding.rvUserList.adapter as ValetsWorkCheckIngUserListAdapter).setItem(checkinResponse.commCheckinValets)
        (binding.rvUserList.adapter as ValetsWorkCheckIngUserListAdapter).notifyDataSetChanged()

        (binding.rvCommunityBuilding.adapter as DustBinListAdapter).setItem(areaList)
        (binding.rvCommunityBuilding.adapter as DustBinListAdapter).notifyDataSetChanged()
    }

    private fun fabFilter() {
        binding.fbButton.setMenuListener(object : SimpleMenuListenerAdapter() {
            override fun onMenuItemSelected(menuItem: MenuItem?): Boolean {
                when (menuItem!!.title) {
                    "All" -> {
                        Log.e("TAG", "action_all")
                        ValetHomeActivity.FILTER_KEY = AppConstants.FILTER_ALL
                        filterApply()
                    }
                    "Pending" -> {
                        ValetHomeActivity.FILTER_KEY = AppConstants.FILTER_PENDDING
                        Log.e("TAG", "action_pending")
                        filterApply()
                    }
                    "Scanned" -> {
                        ValetHomeActivity.FILTER_KEY = AppConstants.FILTER_SCAN
                        Log.e("TAG", "action_scan")
                        filterApply()
                    }
                    "Violation" -> {
                        ValetHomeActivity.FILTER_KEY = AppConstants.FILTER_VIOLARION
                        Log.e("TAG", "action_violation")

                        filterApply()
                    }
                }

                return false
            }
        })


    }


    fun filterApply(){

        areaList.clear()
        if( ValetHomeActivity.FILTER_KEY==AppConstants.FILTER_ALL){
            for(i in 0 until qrCodesList!!.size){
                if(checkinResponse.commQrcodes[i]!!.qrMandatory==1){
                    areaList.add(
                        AreaDataModel(qrCodesList[i]!!.qrType,qrCodesList[i]!!.qrId,qrCodesList[i]!!.qrText
                            ,qrCodesList[i]!!.qrCode,qrCodesList[i]!!.username)
                    )
                }
            }
        }else if( ValetHomeActivity.FILTER_KEY==AppConstants.FILTER_SCAN){
            for(i in 0 until qrCodesList!!.size){
                if(checkinResponse.commQrcodes[i]!!.qrMandatory==1){
                    if(qrCodesList[i]!!.qrType==2){
                        areaList.add(
                            AreaDataModel(qrCodesList[i]!!.qrType,qrCodesList[i]!!.qrId,qrCodesList[i]!!.qrText
                                ,qrCodesList[i]!!.qrCode,qrCodesList[i]!!.username)
                        )
                    }
                }
            }

        }else if( ValetHomeActivity.FILTER_KEY==AppConstants.FILTER_VIOLARION)
        {
            for(i in 0 until qrCodesList!!.size){
                if(checkinResponse.commQrcodes[i]!!.qrMandatory==1){

                    if(qrCodesList[i]!!.qrType==3){
                        areaList.add(
                            AreaDataModel(qrCodesList[i]!!.qrType,qrCodesList[i]!!.qrId,qrCodesList[i]!!.qrText
                                ,qrCodesList[i]!!.qrCode,qrCodesList[i]!!.username)
                        )
                    }
                }
            }
        }else if( ValetHomeActivity.FILTER_KEY==AppConstants.FILTER_PENDDING){
            for(i in 0 until qrCodesList!!.size){
                if(checkinResponse.commQrcodes[i]!!.qrMandatory==1){
                    if(qrCodesList[i]!!.qrType==1){
                        areaList.add(
                            AreaDataModel(qrCodesList[i]!!.qrType,qrCodesList[i]!!.qrId,qrCodesList[i]!!.qrText
                                ,qrCodesList[i]!!.qrCode,qrCodesList[i]!!.username)
                        )
                    }
                }
            }

        }
        (binding.rvCommunityBuilding.adapter as DustBinListAdapter).setItem(areaList)
        (binding.rvCommunityBuilding.adapter as DustBinListAdapter).notifyDataSetChanged()
        binding.scanQr.visibility=View.VISIBLE
        binding.scanQr.isFocusable=true
        binding.scanQr.isFocusableInTouchMode = true
        binding.scanQr.requestFocus()

    }
    private fun setToolbar() {
        Log.e("TOOLBAR", "-->")
        binding.toolbar.tvToolbarTitle.text = getString(R.string.lbl_valets_work)
        setNotificationCount(binding.toolbar,prefs.notificationCount!!)
        setToolbarRight1MenuIcon(binding.toolbar, R.drawable.ic_notification_white, object :
            BaseActivity.ToolbarRight1MenuClickListener {
            override fun onRight1MenuClicked() {
                context?.let{
                    startActivity(NotificationListActivity.newInstance(context!!))

                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPermission() {
        if (context!!.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 10)
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {

        // Create the location request to start receiving updates

        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        builder.setAlwaysShow(true)
        val locationSettingsRequest = builder.build()
        val settingsClient = context?.let { LocationServices.getSettingsClient(it) }
        settingsClient!!.checkLocationSettings(locationSettingsRequest)

        /*[START]location enable*/
        val googleApiClient = context?.let {
            GoogleApiClient.Builder(it)
                .addApi(LocationServices.API).build()
        }
        googleApiClient!!.connect()
        val result1 =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result1.setResultCallback { result ->
            val status = result.status
            if (status.statusCode == LocationSettingsStatusCodes.SUCCESS) {

            }
            if (status.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                try {
                    status.startResolutionForResult(context as Activity?, 0x1)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("LOCATION_ERROR","---->"+e.message)
                }

            }
            if (status.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE)
                Log.e(
                    "Martin",
                    "Location settings are inadequate, and cannot be fixed here. Dialog " + "not created."
                )

            /*[END]location enable*/

        }

        mFusedLocationProviderClient =
            context?.let { LocationServices.getFusedLocationProviderClient(it) }
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && context?.let {
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
                hideProgress()
            }
        }
    }

    fun showCheckOutButton() {
        val alert = AlertDialog.Builder(context!!)

        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_chekout, null)
        val btnSubmit = alertLayout.findViewById<AppCompatTextView>(R.id.tvOk)
        val btnCancel = alertLayout.findViewById<AppCompatTextView>(R.id.tvCancel)

        alert.setView(alertLayout)
        alert.setCancelable(false)

        val dialog = alert.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        btnSubmit.setOnClickListener {
            dialog.dismiss()
            startActivity(CheckInActivity.newInstance(context!!,prefs.checkingResponse!!.result!!.commId,
                prefs.checkingResponse!!.result!!.commSchId,
                prefs.checkingResponse!!.result!!.checkinType.toString(),
                "","",false))

        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
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

        mLatitude = mLastLocation.latitude.toString()
        mLongitude = mLastLocation.longitude.toString()
        if (isCheckOut) {
            val param: HashMap<String, Any?> = HashMap()
            param[ApiParams.LATITUDE] = mLatitude
            param[ApiParams.LONGITUDE] = mLongitude
            param[ApiParams.COMM_ID] = prefs.checkingResponse!!.result!!.commId
            param[ApiParams.SCH_ID] = prefs.checkingResponse!!.result!!.commSchId
            param[ApiParams.TYPE] = prefs.checkingResponse!!.result!!.checkinType
            param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
            mViewModel.callApiCheckout(param)
            Log.e("CHECK_OUT","->"+param)



        }else{
            val jsonScanQR = JSONObject()
            try {
                jsonScanQR.put("command", "scan")
                jsonScanQR.put("sc_comm_id", prefs.checkingResponse!!.result!!.commId)
                jsonScanQR.put("sc_sch_id", prefs.checkingResponse!!.result!!.commSchId)
                jsonScanQR.put("sc_latitude", mLatitude)
                jsonScanQR.put("sc_longitude", mLongitude)
                jsonScanQR.put("sc_qrcode", mQRCodeScan)
                jsonScanQR.put("type", prefs.checkingResponse!!.result!!.checkinType.toString())
                if (prefs.checkingResponse!!.result!!.checkinType == 2) {
                    jsonScanQR.put("weekno", prefs.checkingResponse!!.result!!.weekno.toString())
                } else {
                    jsonScanQR.put("weekno", "")
                }
                jsonScanQR.put("userId", prefs.userDataModel!!.logindata.uId)
                jsonScanQR.put("scan_device_type",2)
                Log.e("SOCKET_SEND_RESPONSE", "---->" + jsonScanQR.toString())
                AppUtils.webSocket.send(jsonScanQR.toString())

                binding.scanQr.setText("")
                mQRCodeScan=""
                binding.scanQr.visibility == View.VISIBLE

            } catch (e: JSONException) {
                binding.scanQr.setText("")
                binding.scanQr.visibility == View.VISIBLE
                e.printStackTrace()
            }
        }



        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }

    lateinit var mLatitude: String
    lateinit var mLongitude: String

    private fun updateQRCodeStatus(obj: JSONObject?) {
        try {
            if (obj!!.getInt("code") == 200) {


                val mType = obj.getJSONObject("result").getInt("type")

                /*type 1 is valets*/
                if (1 == mType) {

                    /*if mQrType is 2 violation and 1 is Scan*/
                    val mQrType = obj.getJSONObject("result").getInt("qr_type")
                    val prefsCheckingResponse = prefs.checkingResponse
                    if(mQrType==2){
                        prefsCheckingResponse!!.result!!.commVioCount = obj.getJSONObject("result").getInt("comm_vio_count")
                        binding.tvViolationCount.text =
                            obj.getJSONObject("result").getInt("comm_vio_count")
                                .toString()
                        prefs.checkingResponse = prefsCheckingResponse
                        if(currentFragment==1){
                            EventBus.getDefault().post(AppConstants.REFRESH_FREGMENT)
                        }
                    }else{
                        val mQRStatus = obj.getJSONObject("result").getInt("qr_status")
                        val mName = obj.getJSONObject("result").getString("username")
                        val mRemainCount = obj.getJSONObject("result").getInt("comm_remain_count")
                        SplashActivity.REMANING_COUNT =mRemainCount.toString()

                        prefsCheckingResponse!!.result!!.commRemainCount = mRemainCount
                        binding.tvCommunityCount.text = prefsCheckingResponse.result!!.commRemainCount.toString()


                        for (i in 0 until prefsCheckingResponse!!.result!!.commQrcodes.size) {

                            val mQRId = obj.getJSONObject("result").getInt("qr_id")
                            if (prefsCheckingResponse.result.commQrcodes[i]!!.qrId == mQRId) {
                                EventBus.getDefault().post(AppConstants.REFRESH_REMAINING_COUNT)

                                prefsCheckingResponse.result.commQrcodes[i]!!.qrType =
                                    mQRStatus
                                prefsCheckingResponse.result.commQrcodes[i]!!.username =
                                    mName

                                qrCodesList.clear()
                                prefs.checkingResponse = prefsCheckingResponse
                                qrCodesList = prefs.checkingResponse?.result?.commQrcodes!!

                                Log.e("REFRESH_MAIN_ADPTER", "----------->" + qrCodesList)

                                if(currentFragment==1){
                                    EventBus.getDefault().post(AppConstants.REFRESH_FREGMENT)
                                }
                                break
                            }

                        }
                    }
                    }

                }
            else{
                EventBus.getDefault().post(AppConstants.WRONG_QR_CODE)
            }


        } catch (e: Exception) {

            e.printStackTrace()
        }


    }

}