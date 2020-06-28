package com.enviroclean.ui.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.enviroclean.R
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityValetHomeBinding
import com.enviroclean.ui.fragment.manager.ManagerChatFragment
import com.enviroclean.ui.fragment.manager.ManagerProfileFragment
import com.enviroclean.ui.fragment.manager.ManagerViolationFragment
import com.enviroclean.ui.fragment.valet.ValetWorkAssignmentFragment
import com.enviroclean.ui.fragment.valet.ValetWorkCheckingFragment
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.MyLocationProvider
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.ValetHomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ValetHomeActivity : BaseActivity<ValetHomeViewModel>(LoginActivity::class.java.simpleName),
    BaseActivity.ToolbarRightMenuClickListener, MyLocationProvider.MyLocationListener {
    override fun onLocationReceived(location: Location?) {
        location?.let {
             Log.w("LATITUDE", "" + it.latitude)
             Log.w("LONGITUDE", "" + it.longitude)
            mLatitude = it.latitude.toString()
            mLongitude = it.longitude.toString()
          //  locationProvider?.stopLocationUpdates()

            // When we need to get location again, then call below line
     //       locationProvider?.startGettingLocations()

        }
    }
    override fun onRightMenuClicked() {
        if (selectedTabIndex == 2) {
            prefs.clearPrefs()
            startActivity(LoginActivity.newInstance(this, true))
            finishAffinity()
            AppUtils.finishFromLeftToRight(this)
        } else {
        }
    }

    private val channelId: String by lazy {
        intent.getStringExtra("CHANNEL_ID")
    }

    private val notificationType: String by lazy {
        intent.getStringExtra("NOTIFICATION_TYPE")
    }

    var mLongitude: String = ""
    var mLatitude: String = ""
    private var selectedTabIndex: Int = 0
    private lateinit var prefs: Prefs
    var locationProvider: MyLocationProvider? = null
    private lateinit var binding: ActivityValetHomeBinding
    private val mViewModel: ValetHomeViewModel by lazy {
        ViewModelProviders.of(this).get(ValetHomeViewModel::class.java)
    }

    override fun getViewModel(): ValetHomeViewModel {
        return mViewModel
    }
    private val isDisplayPopUp: Boolean by lazy {
        intent.getBooleanExtra(AppConstants.IS_NOTIFICATION_POP_UP,false)
    }
    private val isDisplayCheckOutPopUp: Boolean by lazy {
        intent.getBooleanExtra(AppConstants.IS_DISPLAY_CKECK_OUT_POP_UP,false)
    }

    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    companion object {
        var FILTER_KEY: String = AppConstants.FILTER_ALL
        var currentFragment:Int =1
        var clickDirectViolationFragment:Boolean=false
        var socketConnectedValets:Boolean=false
        fun newInstance(
            context: Context,
            blackStackClear: Boolean = false,
            channelId: String = "",
            notificationType: String = "",
            isDisplayPopUp: Boolean = false,
            isDisplayCheckOutPopUp:Boolean =false
        ): Intent {
            val intent = Intent(context, ValetHomeActivity::class.java)
            intent.putExtra("CHANNEL_ID", channelId)
            intent.putExtra("NOTIFICATION_TYPE", notificationType)
            intent.putExtra(AppConstants.IS_NOTIFICATION_POP_UP, isDisplayPopUp)
            intent.putExtra(AppConstants.IS_DISPLAY_CKECK_OUT_POP_UP, isDisplayCheckOutPopUp)
            if (blackStackClear){
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }
    }
    var isClick:Boolean=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_valet_home)
        EventBus.getDefault().register(this)
        activity=this
        init()
        initToolBar()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        getLocation()

        if (notificationType == "MSG") {
            if (channelId.isNotEmpty()) {
                replaceFragment(
                    R.id.frame_container,
                    ManagerChatFragment.newInstance(channelId),
                    false
                )
            }
        }

    }

    private fun init() {
        prefs = Prefs.getInstance(this)!!
        mViewModel.callGetTwilioAccessTokenAPI("enviroclean-" + prefs.userDataModel!!.logindata.uId)
        if (prefs.isCheck) {
            currentFragment =1
            setToolbarBackground(R.color.blue_toolbar)
            setToolbarRightMenuIcon(R.drawable.ic_notification_white, this)
            setToolbarTitle(R.string.lbl_work_assignment)
            replaceFragment(
                R.id.frame_container,
                ValetWorkCheckingFragment.newInstance(),
                false
            )
        } else {
            setToolbarBackground(R.color.blue_toolbar)
            setToolbarRightMenuIcon(R.drawable.ic_notification_white, this)
            setToolbarTitle(R.string.lbl_work_assignment)
            replaceFragment(
                R.id.frame_container,
                ValetWorkAssignmentFragment.newInstance(),
                false
            )

        }
        binding.navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        if(isDisplayPopUp){
            AppUtils.showSnackBar(binding.navView,"Hi "+prefs.userDataModel!!.logindata.uFirstName+", you have a unit audit at "+prefs.checkingResponse!!.result!!.commSchName+". Have a great shift")

        }
        if(isDisplayCheckOutPopUp){
            AppUtils.showSnackBar(binding.navView,"Thanks "+prefs.userDataModel!!.logindata.uFirstName+", Drive home safe.")
        }
    }

    private fun initToolBar() {
        setToolbarBackground(R.color.blue_toolbar)
        setToolbarRightMenuIcon(R.drawable.ic_notification_white, this)
        setToolbarTitle(R.string.lbl_work_assignment)
    }

    fun getLocation() {
        locationProvider = MyLocationProvider(this, this)
        locationProvider?.init()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }
    var currentFragmentIndex=0
    @Subscribe
    fun onEvent(event: String) {
        if(AppConstants.REFRESH_FREGMENT==event){
            openFirstTab()
        }
        if(AppConstants.AUTO_LOGOUT==event){
            prefs.clearCheckinDate()
            AppUtils.webSocket.cancel()
            AppUtils.client!!.dispatcher.executorService.shutdown()
            socketConnectedValets = false
            prefs.violationAreaSize = ""
            finish()
            startActivity(intent)
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
     fun openFirstTab() {
         if(currentFragmentIndex==0){
             if (prefs.isCheck) {
                 setToolbarBackground(R.color.blue_toolbar)
                 setToolbarRightMenuIcon(R.drawable.ic_notification_white, this)
                 setToolbarTitle(R.string.lbl_work_assignment)
                 replaceFragment(
                     R.id.frame_container,
                     ValetWorkCheckingFragment.newInstance(),
                     false
                 )

             } else {
                 setToolbarBackground(R.color.blue_toolbar)
                 setToolbarRightMenuIcon(R.drawable.ic_notification_white, this)
                 setToolbarTitle(R.string.lbl_work_assignment)
                 replaceFragment(
                     R.id.frame_container,
                     ValetWorkAssignmentFragment.newInstance(),
                     false
                 )
             }
         }else if(currentFragmentIndex==1){
             replaceFragment(R.id.frame_container, ManagerChatFragment.newInstance(), false)
         }else if(currentFragmentIndex==2){
             currentFragmentIndex=2
             selectedTabIndex = 2
             setToolbarBackground(R.color.blue_toolbar)
             setToolbarRightMenuIcon(R.drawable.ic_logout_white, this)
             setToolbarTitle(R.string.lbl_my_profile)

             replaceFragment(
                 R.id.frame_container,
                 ManagerProfileFragment.newInstance(),
                 false
             )
         }else if(  currentFragmentIndex==3){
             if(prefs.isCheck){
                 currentFragmentIndex=3
                 currentFragment=4
                 addFragment(
                     R.id.frame_container,
                     ManagerViolationFragment.newInstance(),
                     false
                 )
             }
         }

    }

     val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_assignment -> {
                    if(isClick){
                        isClick=false

                        currentFragmentIndex=0
                        currentFragment=1
                        if (prefs.isCheck) {
                            openFirstTab()
                            return@OnNavigationItemSelectedListener true
                        } else {
                            setToolbarBackground(R.color.blue_toolbar)
                            setToolbarRightMenuIcon(R.drawable.ic_notification_white, this)
                            setToolbarTitle(R.string.lbl_work_assignment)
                            replaceFragment(
                                R.id.frame_container,
                                ValetWorkAssignmentFragment.newInstance(),
                                false
                            )
                            return@OnNavigationItemSelectedListener true
                        }
                    }
                    Handler().postDelayed(Runnable {
                        isClick=true
                    },1500)

                }
                R.id.nav_chat -> {
                    currentFragmentIndex=1
                    replaceFragment(R.id.frame_container, ManagerChatFragment.newInstance(), false)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_profile -> {
                    currentFragmentIndex=2
                    selectedTabIndex = 2
                    setToolbarBackground(R.color.blue_toolbar)
                    setToolbarRightMenuIcon(R.drawable.ic_logout_white, this)
                    setToolbarTitle(R.string.lbl_my_profile)

                    replaceFragment(
                        R.id.frame_container,
                        ManagerProfileFragment.newInstance(),
                        false
                    )
                    return@OnNavigationItemSelectedListener true
                }
                R.id.nav_violate_report -> {
                    if(prefs.isCheck){
                        currentFragmentIndex=3
                        currentFragment=4
                        addFragment(
                            R.id.frame_container,
                            ManagerViolationFragment.newInstance(),
                            false
                        )
                    }else{
                        AppUtils.showSnackBar(binding.navView, getString(R.string.lbl_sacn_qr_))
                    }
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment=supportFragmentManager.findFragmentById(R.id.frame_container)
        fragment!!.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MyLocationProvider.REQUEST_LOCATION_SETTINGS) {
            locationProvider?.onActivityResult(requestCode, resultCode, intent)
        }
    }


    private fun setObserver() {
        mViewModel.getTwilioAccessResponse().observe({ this.lifecycle }, { accessToken: String? ->
            prefs.twilioAccessToken = accessToken
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == MyLocationProvider.LOCATION_PERMISSION_REQUEST_CODE) {
            locationProvider?.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }
}
