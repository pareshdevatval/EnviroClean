package com.enviroclean.ui.fragment.manager

import android.Manifest
import android.app.Activity
import android.content.*
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
import com.enviroclean.adapter.*
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseFragment
import com.enviroclean.databinding.FragmentManagerMyWorkCheckingBinding
import com.enviroclean.iterfacea.CheckInValetsClick
import com.enviroclean.iterfacea.MessageInterface
import com.enviroclean.iterfacea.ReconnectSocketInterface
import com.enviroclean.model.AreaDataModel
import com.enviroclean.model.CheckIngResponse1
import com.enviroclean.ui.activity.*
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.ManagerMyWorkCheckingViewModel
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
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
class ManagerMyWorkCheckingFragment : BaseFragment<ManagerMyWorkCheckingViewModel>(),MessageInterface,
    View.OnClickListener, ReconnectSocketInterface,
    BaseBindingAdapter.ItemClickListener<AreaDataModel>, CheckInValetsClick {
    override fun setValetClick(position: Int, item: CheckIngResponse1.Result.CommCheckinValet) {

    }

    override fun onItemClick(view: View, data: AreaDataModel?, position: Int) {
        if(data!!.qrType== AppConstants.REMAINING){
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(context as ManagerHomeActivity, arrayOf<String>(
                    Manifest.permission.CAMERA), 1022)
                return
            }
            (context as ManagerHomeActivity).startActivityForResult(
                PickUpActivity.newInstance(
                context!!, data.qrId,
                prefs.checkingResponse!!.result!!.commId,
                prefs.checkingResponse!!.result!!.commSchId, data.qrCode, data.qrText
            ), 99)
    }
    }

    override fun reconnected() {

    }

    override fun massagesRecived(s: JSONObject?) {
        updateQRCodeStatus(s)
    }

    override fun onClick(p0: View?) {
        p0?.let {
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
                R.id.ivMap->{
                    val instance = Calendar.getInstance()
                    val mCurrentDate =
                        (instance.get(Calendar.DATE).toString() + "-" + (instance.get(Calendar.MONTH) + 1) + "-" + instance.get(
                            Calendar.YEAR
                        ).toString())
                    startActivity(
                        LiveTrackingActivity.newInstance(
                            context!!,
                            prefs.checkingResponse!!.result!!.commId.toString(),
                            prefs.checkingResponse!!.result!!.commSchId.toString(),
                            mCurrentDate
                        )
                    )
                    AppUtils.startFromRightToLeft(context!!)
                }
            }
        }
    }

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 2000
    private val FASTEST_INTERVAL: Long = 1000
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest


    private lateinit var binding: FragmentManagerMyWorkCheckingBinding

    private val mViewModel: ManagerMyWorkCheckingViewModel by lazy {
        ViewModelProviders.of(this).get(ManagerMyWorkCheckingViewModel::class.java)
    }

    override fun getViewModel(): ManagerMyWorkCheckingViewModel {
        return mViewModel
    }

    lateinit var prefs: Prefs
    lateinit var checkinResponse: CheckIngResponse1.Result


    var qrCodesList: ArrayList<CheckIngResponse1.Result.CommQrcode?> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentManagerMyWorkCheckingBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        fun newInstance(): ManagerMyWorkCheckingFragment {
            val bundle = Bundle()
            val fragment = ManagerMyWorkCheckingFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        prefs = Prefs.getInstance(context!!)!!

        init()

    }
    override fun onResume() {
        super.onResume()
        setNotificationCount(binding.toolbar,prefs.notificationCount!!)

         view!!.setFocusableInTouchMode(true);
        view!!.requestFocus();
        view!!.setOnKeyListener(object :View.OnKeyListener{
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (event!!.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    replaceFragment(
                        R.id.frame_container,
                        ManagerWorkAssignmentFragment.newInstance(),
                        false
                    )
                    return true;
                }
                return false;
            }
        })
    }

    private fun init() {
        mLocationRequest = LocationRequest()

        checkinResponse = prefs.checkingResponse!!.result!!
        mViewModel.getResponse().observe(this, observeCheckOutData)
        binding.ivCheckOut.setOnClickListener(this)
        binding.ivMap.setOnClickListener(this)
        binding.tvViolationCount.setOnClickListener(this)
        setWorkAdapter()
        setToolbar()
        setHeaderData()

        if(!ManagerHomeActivity.socketConnectedManager){
            socketRegister()
        }
        fabFilter()

        scanQRUsingDevice()
    }

    @Subscribe
    fun onEvent(isConnected: String) {
        if(isConnected==AppConstants.IS_CONNECTED){
            if(!ManagerHomeActivity.socketConnectedManager){
                if (prefs.isCheck) {
                    AppUtils.loge("CONNECTED DATA")
                    socketRegister()
                }
            }
        }
        if(isConnected==AppConstants.NOTIFICATION_COUNT){
            setNotificationCount(binding.toolbar,prefs.notificationCount!!)
        }
    }
    private fun socketRegister() {
        if(context==null){
            handler.removeCallbacks(checkSocketConnection)
            handler.removeCallbacksAndMessages(null)
            return
        }
        AppUtils.startSocketConnection("subscribe",checkinResponse.commId.toString(),context!!,this@ManagerMyWorkCheckingFragment,this@ManagerMyWorkCheckingFragment)

        val jsonObject = JSONObject()
        try {
            jsonObject.put("command", "register")
            jsonObject.put("userId", prefs.userDataModel!!.logindata.uId)
            AppUtils.webSocket.send(jsonObject.toString())
            Log.e("register_socket","--->"+jsonObject.toString())
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
            if (!ManagerHomeActivity.socketConnectedManager) {
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
        binding.scanQr.setFocusableInTouchMode(true);
        binding.scanQr.requestFocus();
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

    private fun updateQRCodeStatus(obj: JSONObject?) {
        try {
            if (obj!!.getInt("code") == 200) {


                val mType = obj.getJSONObject("result").getInt("type")

                /*type 2 is manages*/

                if (2 == mType) {

                    val prefsCheckingResponse = prefs.checkingResponse

                    // val mAreaId = obj.getJSONObject("result").getInt("area_id")
                    /*if mQrType is 2 violation and 1 is Scan*/
                    val mQrType = obj.getJSONObject("result").getInt("qr_type")
                  //  val prefsCheckingResponse = prefs.checkingResponse
                    if(mQrType==2){
                        prefsCheckingResponse!!.result!!.commVioCount = obj.getJSONObject("result").getInt("comm_vio_count")
                        binding.tvViolationCount.text =
                            obj.getJSONObject("result").getInt("comm_vio_count")
                                .toString()
                        prefs.checkingResponse = prefsCheckingResponse
                        if(ManagerHomeActivity.currentFragment ==1){
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
                                prefsCheckingResponse.result.commVioCount =
                                    obj.getJSONObject("result").getInt("comm_vio_count")
                                binding.tvViolationCount.text =
                                    obj.getJSONObject("result").getInt("comm_vio_count")
                                        .toString()

                                qrCodesList.clear()
                                prefs.checkingResponse = prefsCheckingResponse
                                qrCodesList = prefs.checkingResponse?.result?.commQrcodes!!


                               // filterApply()
                                Log.e("REFRESH_MAIN_ADPTER", "----------->" + qrCodesList)

                                if(ManagerHomeActivity.currentFragment ==1){
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



    private val observeCheckOutData = Observer<BaseResponse> {
        if (it.status) {
            prefs.clearCheckinDate()
            AppUtils.webSocket.cancel()
            AppUtils.client!!.dispatcher.executorService.shutdown()
            ManagerHomeActivity.socketConnectedManager =false
            context!!.startActivity(ManagerHomeActivity.newInstance(context!!,true,
                "","",false,true))
            (context!! as ManagerHomeActivity).finishAffinity()
        }
    }

    private fun fabFilter() {
        binding.fbButton.setMenuListener(object : SimpleMenuListenerAdapter() {
            override fun onMenuItemSelected(menuItem: MenuItem?): Boolean {
                when ( menuItem!!.title){
                    "All" -> {
                        Log.e("TAG", "action_all")
                        ManagerHomeActivity.FILTER_KEY =AppConstants.FILTER_ALL
                        filterApply()
                    }
                    "Pending" -> {
                        ManagerHomeActivity.FILTER_KEY =AppConstants.FILTER_PENDDING
                        Log.e("TAG", "action_pending")
                        filterApply()
                    }
                    "Scanned" -> {
                        ManagerHomeActivity.FILTER_KEY =AppConstants.FILTER_SCAN
                        Log.e("TAG", "action_scan")
                        filterApply()
                    }
                    "Violation" -> {
                        ManagerHomeActivity.FILTER_KEY =AppConstants.FILTER_VIOLARION
                        Log.e("TAG", "action_violation")
                        filterApply()
                    }
                }

                return false
            }
        })

    }

    val areaList:ArrayList<AreaDataModel?> = ArrayList()
    fun filterApply(){

        areaList.clear()
        if( ManagerHomeActivity.FILTER_KEY==AppConstants.FILTER_ALL){
            for(i in 0 until qrCodesList!!.size){
                if(checkinResponse.commQrcodes[i]!!.qrMandatory==1){
                    areaList.add(
                        AreaDataModel(qrCodesList[i]!!.qrType,qrCodesList[i]!!.qrId,qrCodesList[i]!!.qrText
                            ,qrCodesList[i]!!.qrCode,qrCodesList[i]!!.username)
                    )
                }
            }
        }else if( ManagerHomeActivity.FILTER_KEY==AppConstants.FILTER_SCAN){
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

        }else if( ManagerHomeActivity.FILTER_KEY==AppConstants.FILTER_VIOLARION)
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
        }else if( ManagerHomeActivity.FILTER_KEY==AppConstants.FILTER_PENDDING){
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

        val adapter = ValetsWorkCheckIngUserListAdapter(this@ManagerMyWorkCheckingFragment)
        binding.rvUserList.adapter = adapter

        binding.rvCommunityBuilding.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val communityAdapter = DustBinListAdapter()

        communityAdapter.itemClickListener=this

        (binding.rvUserList.adapter as ValetsWorkCheckIngUserListAdapter).setItem(checkinResponse.commCheckinValets)
        (binding.rvUserList.adapter as ValetsWorkCheckIngUserListAdapter).notifyDataSetChanged()

        // [START] QR Code list
        binding.rvCommunityBuilding.adapter = communityAdapter

        qrCodesList = checkinResponse.commQrcodes

        (binding.rvCommunityBuilding.adapter as DustBinListAdapter).setItem(
            areaList
        )
        (binding.rvCommunityBuilding.adapter as DustBinListAdapter).notifyDataSetChanged()
    }

    private fun setHeaderData() {
        val checkinResult = prefs.checkingResponse!!.result
        binding.tvCommunityName.text = checkinResult!!.commSchName
        binding.tvViolationCount.text = "" + checkinResult.commVioCount
        binding.tvCommunityCount.text = "" + checkinResult.commRemainCount
        SplashActivity.REMANING_COUNT = checkinResult.commRemainCount.toString()
    }


    private fun setToolbar() {
        Log.e("TOOLBAR", "-->")
        binding.toolbar.tvToolbarTitle.text = getString(R.string.lbl_my_work)
        setNotificationCount(binding.toolbar,prefs.notificationCount!!)
        setToolbarLeftIcon(binding.toolbar, R.drawable.ic_back_white, object :
            BaseActivity.ToolbarLeftMenuClickListener {
            override fun onLeftIconClicked() {
                replaceFragment(
                    R.id.frame_container,
                    ManagerWorkAssignmentFragment.newInstance(),
                    false
                )

            }

        })
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

        if(requestCode==10) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                hideProgress()
            }
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

        mLongitude=mLastLocation.longitude.toString()
        mLatitude=mLastLocation.latitude.toString()
        if (isCheckOut) {
            val param: HashMap<String, Any?> = HashMap()
            param[ApiParams.LATITUDE] = mLatitude
            param[ApiParams.LONGITUDE] = mLongitude
            param[ApiParams.COMM_ID] = prefs.checkingResponse!!.result!!.commId
            param[ApiParams.SCH_ID] = prefs.checkingResponse!!.result!!.commSchId
            param[ApiParams.TYPE] = prefs.checkingResponse!!.result!!.checkinType
            param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
            mViewModel.callApiCheckout(param)
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
    lateinit var mLatitude:String
    lateinit var mLongitude:String

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


}