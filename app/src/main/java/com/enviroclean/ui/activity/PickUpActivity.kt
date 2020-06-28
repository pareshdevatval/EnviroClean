package com.enviroclean.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.enviroclean.R
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityPickUpBinding
import com.enviroclean.model.DustBinScanResponse
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.PickUpViewModel
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject

class PickUpActivity : BaseActivity<PickUpViewModel>(PickUpActivity::class.java.simpleName) ,BaseActivity.ToolbarLeftMenuClickListener{
    override fun onLeftIconClicked() {

        onBackPressed()
    }
    private lateinit var codeScanner: CodeScanner


    private lateinit var binding:ActivityPickUpBinding
    private val mViewModel: PickUpViewModel by lazy {
        ViewModelProviders.of(this).get(PickUpViewModel::class.java)
    }

    override fun getViewModel(): PickUpViewModel {
        return mViewModel
    }

    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 2000
    private val FASTEST_INTERVAL: Long = 1000
    lateinit var mLastLocation: Location
    internal lateinit var mLocationRequest: LocationRequest

    lateinit var prefs: Prefs
    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    private val mCommId:String by lazy {
        intent.getStringExtra(AppConstants.COMM_ID)
    }
    private val mSchId:String by lazy {
        intent.getStringExtra(AppConstants.COMM_SCH_ID)
    }
    private val mQrCodeId:String by lazy {
        intent.getStringExtra(AppConstants.QR_CODE_ID)
    }
    private val mAreaId:String by lazy {
        intent.getStringExtra(AppConstants.AREA_ID)
    }
    private val mQrCodeName:String by lazy {
        intent.getStringExtra(AppConstants.QR_CODE_NAME)
    }
    private val mQRText:String by lazy {
        intent.getStringExtra(AppConstants.QR_TEXT)
    }
    companion object {
        fun newInstance(
            context: Context,
            qrCodeId: Int,
            commId: Int,
            commSchId: Int,
            qrCodeName: String,
            qrText: String
        ): Intent {
            val intent = Intent(context, PickUpActivity::class.java)
            intent.putExtra(AppConstants.COMM_ID,commId.toString())
            intent.putExtra(AppConstants.COMM_SCH_ID,commSchId.toString())
            intent.putExtra(AppConstants.QR_CODE_ID,qrCodeId.toString())

            intent.putExtra(AppConstants.QR_CODE_NAME,qrCodeName)
            intent.putExtra(AppConstants.QR_TEXT,qrText)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_pick_up)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.CAMERA), 1022)
        }else{
            inti()
        }
        binding.tvRemaingCount.text="Remaining Count "+SplashActivity.REMANING_COUNT
        binding.tvBinDis.text="You are now in "+prefs.checkingResponse!!.result!!.comm_areas[0]!!.area_name+"\n Scan any QR code which is related to "+prefs.checkingResponse!!.result!!.commName
    }

    private fun inti() {
        EventBus.getDefault().register(this)
        prefs= Prefs.getInstance(this)!!
        if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {
            ManagerHomeActivity.currentFragment=0
        } else {
            ValetHomeActivity.currentFragment=0

        }
        initToolbar()

        mLocationRequest = LocationRequest()
        binding.tvQrCodeNumber.text=mQrCodeName
        mViewModel.getResponse().observe(this,observeQrCodeData)
        codeScanner = CodeScanner(this, binding.scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        qrCodeScanner()
    }

    private val observeQrCodeData=Observer<DustBinScanResponse>{
        if(it.status){
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            val returnIntent = Intent()
            returnIntent.putExtra("area_id", mAreaId)
            returnIntent.putExtra("qr_id", mQrCodeId)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }else{
            codeScanner.startPreview()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            Log.e("DATAREFRESH","--->"+data.getStringExtra("area_id"))
        }
    }

    var isScanTrue:Boolean=false
    var qrList:ArrayList<String> = ArrayList()
    private fun qrCodeScanner() {
        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
              //  qrList.clear()
                mScanCode=it.text
                for(i in 0 until prefs.checkingResponse!!.result!!.commQrcodes.size){
                 //   qrList.add(prefs.checkingResponse!!.result!!.commQrcodes[i]!!.qrCode)
                    if(mScanCode==prefs.checkingResponse!!.result!!.commQrcodes[i]!!.qrText){
                      //  val mScanMusic = MediaPlayer.create(this, R.raw.scan)
                       // mScanMusic.start()
                        isScanTrue=true
                        if (AppUtils.hasInternet(this)){
                            showProgress()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                showPermission()
                            }else{
                                startLocationUpdates()
                            }
                        }else{
                            AppUtils.showSnackBar(binding.tvBinDis,resources.getString(R.string.msg_no_internet))
                            codeScanner.startPreview()
                        }
                    }

                }
                if(!isScanTrue){
                    val mScanMusic = MediaPlayer.create(this, R.raw.wrong_scan)
                    mScanMusic.start()

                    codeScanner.startPreview()
                    AppUtils.showSnackBar(binding.tvScannerQr,getString(R.string.lbl_scan_qr_is_different))
                }



            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
       codeScanner.releaseResources()
        super.onPause()
    }
    private fun initToolbar() {
        setToolbarBackground(R.color.blue_toolbar)
        setToolbarTitle(R.string.lbl_pick_up)
        setToolbarLeftIcon(R.drawable.ic_back_white, this)
    }

    override fun onBackPressed() {
        if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {
            ManagerHomeActivity.currentFragment=1
            startActivity(ManagerHomeActivity.newInstance(this))
            AppUtils.startFromRightToLeft(this)
            finishAffinity()
        } else {
            ValetHomeActivity.currentFragment=1
            startActivity(ValetHomeActivity.newInstance(this))
            AppUtils.startFromRightToLeft(this)
            finishAffinity()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode==10){
            startLocationUpdates()
        }
        if(requestCode==1022){

            Handler().postDelayed(Runnable {
                inti()
            },1000)

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

        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        builder.setAlwaysShow(true)
        val locationSettingsRequest = builder.build()
        val settingsClient = this?.let { LocationServices.getSettingsClient(it) }
        settingsClient!!.checkLocationSettings(locationSettingsRequest)

        /*[START]location enable*/
        val googleApiClient = this?.let {
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
                    status.startResolutionForResult(this as Activity?, 0x1)
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

        mLatitude=mLastLocation.latitude.toString()
        mLongitude=mLastLocation.longitude.toString()


            val jsonScanQR = JSONObject()
            try {
                jsonScanQR.put("command", "scan")
                jsonScanQR.put("sc_comm_id", mCommId)
                jsonScanQR.put("sc_sch_id", mSchId)
                jsonScanQR.put("sc_latitude", mLatitude)
                jsonScanQR.put("sc_longitude", mLongitude)
                jsonScanQR.put("sc_qrcode", mScanCode)
                jsonScanQR.put("type", prefs.checkingResponse!!.result!!.checkinType.toString())
                if(prefs.checkingResponse!!.result!!.checkinType==2){
                    jsonScanQR.put("weekno", prefs.checkingResponse!!.result!!.weekno.toString())
                }else{
                    jsonScanQR.put("weekno", "")
                }
                jsonScanQR.put("userId", prefs.userDataModel!!.logindata.uId)
                jsonScanQR.put("scan_device_type",1)
                Log.e("SOCKET_SEND_RESPONSE","---->"+jsonScanQR.toString())
                AppUtils.webSocket.send(jsonScanQR.toString())

                isScanTrue=false
                Handler().postDelayed(Runnable {
                    if (progressDialog != null) {
                        hideProgress()
                    }
                    val commQrcodesResopnse=prefs.checkingResponse!!.result!!.commQrcodes
                    for(i in 0 until commQrcodesResopnse.size){
                                if(SplashActivity.REMANING_COUNT=="0"){
                                    onBackPressed()
                                    return@Runnable
                                }
                                if(commQrcodesResopnse[i]!!.qrType==1 && commQrcodesResopnse[i]!!.qrMandatory==1 ){
                                    startActivity(newInstance(
                                        this,commQrcodesResopnse[i]!!.qrId,
                                        mCommId.toInt(),
                                        mSchId.toInt(),
                                        commQrcodesResopnse[i]!!.qrCode,
                                        commQrcodesResopnse[i]!!.qrText))
                                    Log.e("FINISH_ACTIVITY","---->")
                                    finish()
                                    break
                                }else{
                                    onBackPressed()
                                }



                    }
                },1500)



            } catch (e: JSONException) {
                e.printStackTrace()
            }

        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }
    lateinit var mLatitude:String
    lateinit var mLongitude:String
    lateinit var mScanCode:String

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onEvent(worngQr: String) {
        if(worngQr==AppConstants.WRONG_QR_CODE){
            AppUtils.showSnackBar(binding.tvScannerQr,getString(R.string.lbl_qr_all_ready_scan))
            val mScanMusic = MediaPlayer.create(this, R.raw.alreadyscannedqr)
            mScanMusic.start()
        }
        if(worngQr==AppConstants.REFRESH_REMAINING_COUNT){
            binding.tvRemaingCount.text="Remaining Count "+SplashActivity.REMANING_COUNT
            if(SplashActivity.REMANING_COUNT=="0"){
                onBackPressed()
            }
        }
    }
}
