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
import com.enviroclean.databinding.ActivityManagerHomeBinding
import com.enviroclean.ui.fragment.manager.*
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.MyLocationProvider
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.ManagerHomeViewModel
import com.luseen.spacenavigation.SpaceItem
import com.luseen.spacenavigation.SpaceOnClickListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class ManagerHomeActivity :
    BaseActivity<ManagerHomeViewModel>(LoginActivity::class.java.simpleName),
    BaseActivity.ToolbarRightMenuClickListener, MyLocationProvider.MyLocationListener {
    override fun onLocationReceived(location: Location?) {
        location?.let {
            Log.w("LATITUDE", "" + it.latitude)
            Log.w("LONGITUDE", "" + it.longitude)
            mLatitude = it.latitude.toString()
            mLongitude = it.longitude.toString()
            locationProvider?.stopLocationUpdates()

            // When we need to get location again, then call below line
            //    locationProvider?.startGettingLocations()

        }
    }

    override fun onRightMenuClicked() {
        if (selectedTabIndex == 2) {
            prefs.clearPrefs()
            prefs.isLoggedIn = false
            startActivity(LoginActivity.newInstance(this, true))
            finishAffinity()
            AppUtils.finishFromLeftToRight(this)
        } else {
            AppUtils.showSnackBar(binding.root, "Under Development")
            //startActivity(NotificationListActivity.newInstance(this))
        }
    }

    private lateinit var prefs: Prefs
    private var selectedTabIndex: Int = 0
    private lateinit var binding: ActivityManagerHomeBinding
    var isClick: Boolean = true
    private val mViewModel: ManagerHomeViewModel by lazy {
        ViewModelProviders.of(this).get(ManagerHomeViewModel::class.java)
    }

    override fun getViewModel(): ManagerHomeViewModel {
        return mViewModel
    }

    private val channelId: String by lazy {
        intent.getStringExtra("CHANNEL_ID")
    }

    private val notificationType: String by lazy {
        intent.getStringExtra("NOTIFICATION_TYPE")
    }
    private val isDisplayPopUp: Boolean by lazy {
        intent.getBooleanExtra(AppConstants.IS_NOTIFICATION_POP_UP, false)
    }

    private val isDisplayCheckOutPopUp: Boolean by lazy {
        intent.getBooleanExtra(AppConstants.IS_DISPLAY_CKECK_OUT_POP_UP, false)
    }


    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    companion object {
        var FILTER_KEY: String = AppConstants.FILTER_ALL
        var currentFragment: Int = 1
        var clickDirectViolationFragment: Boolean = false
        var socketConnectedManager: Boolean = false
        fun newInstance(
            context: Context,
            blackStackClear: Boolean = false,
            channelId: String = "",
            notificationType: String = "",
            isDisplayPopUp: Boolean = false,
            isDisplayCheckOutPopUp: Boolean = false
        ): Intent {
            val intent = Intent(context, ManagerHomeActivity::class.java)
            intent.putExtra("CHANNEL_ID", channelId)
            intent.putExtra("NOTIFICATION_TYPE", notificationType)
            intent.putExtra(AppConstants.IS_NOTIFICATION_POP_UP, isDisplayPopUp)
            intent.putExtra(AppConstants.IS_DISPLAY_CKECK_OUT_POP_UP, isDisplayCheckOutPopUp)
            if (blackStackClear) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }
    }

    var mLongitude: String = ""
    var mLatitude: String = ""
    override fun onStop() {
        super.onStop()
        // locationProvider?.onStop()
    }

    var locationProvider: MyLocationProvider? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manager_home)
        activity = this
        EventBus.getDefault().register(this)
        binding.managerHomeActivity = this
        prefs = Prefs.getInstance(this)!!

        setBottomTab()
        initToolbar(0)
        getLocation()

        if (isDisplayPopUp) {
            showDailog()
        }
        if (isDisplayCheckOutPopUp) {
            AppUtils.showSnackBar(
                binding.bottomNavigation,
                "Thanks " + prefs.userDataModel!!.logindata.uFirstName + ", Drive home safe."
            )
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    var currentFragmentIndex = 0

    @Subscribe
    fun onEvent(event: String) {
        if (AppConstants.REFRESH_FREGMENT == event) {
            changeFragment(currentFragmentIndex)
        }
        if (AppConstants.AUTO_LOGOUT == event) {
            prefs.clearCheckinDate()
            AppUtils.webSocket.cancel()
            AppUtils.client!!.dispatcher.executorService.shutdown()
            prefs.violationAreaSize = ""
            socketConnectedManager = false
            finish()
            startActivity(intent)
        }
    }

    fun initToolbar(index: Int) {
        when (index) {
            0 -> {
                setToolbarBackground(R.color.blue_toolbar)
                setToolbarRightMenuIcon(R.drawable.ic_notification_white, this)
                setToolbarTitle(R.string.lbl_work_assignment)

            }
            1 -> {
                setToolbarBackground(R.color.blue_toolbar)
                setToolbarRightMenuIcon(R.drawable.ic_search_white, this)
                setToolbarTitle(R.string.lbl_chat)
            }
            2 -> {
                setToolbarBackground(R.color.blue_toolbar)
                setToolbarRightMenuIcon(0, this)
                setToolbarTitle(R.string.lbl_violation)
            }
            3 -> {
                setToolbarBackground(R.color.blue_toolbar)
                setToolbarRightMenuIcon(R.drawable.ic_logout_white, this)
                setToolbarTitle(R.string.lbl_my_profile)
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }

    private fun setBottomTab() {

        binding.bottomNavigation.setSpaceOnClickListener(object : SpaceOnClickListener {
            override fun onCentreButtonClick() {
                startActivity(CreateScheduleActivity.newInstance(this@ManagerHomeActivity))
                AppUtils.startFromBottomToUp(this@ManagerHomeActivity)
            }

            override fun onItemClick(itemIndex: Int, itemName: String?) {
                changeFragment(itemIndex)

            }

            override fun onItemReselected(itemIndex: Int, itemName: String?) {
                if (isClick) {
                    isClick = false
                    changeFragment(itemIndex)
                }
            }

        })


        binding.bottomNavigation.addSpaceItem(
            SpaceItem(
                "",
                R.drawable.ic_assignment_select
            )
        )
        binding.bottomNavigation.addSpaceItem(
            SpaceItem(
                "",
                R.drawable.ic_chat_selected
            )
        )

        binding.bottomNavigation.setCentreButtonIcon(R.drawable.ic_schedule)
        binding.bottomNavigation.setCentreButtonColor(resources.getColor(R.color.bottom_tab_select))

        binding.bottomNavigation.addSpaceItem(
            SpaceItem(
                "",
                R.drawable.ic_photo
            )
        )
        binding.bottomNavigation.addSpaceItem(
            SpaceItem(
                "",
                R.drawable.ic_profile_select
            )
        )

        binding.bottomNavigation.shouldShowFullBadgeText(false)

        binding.bottomNavigation.setCentreButtonId(R.id.nav_create_schedule)
        binding.bottomNavigation.setCentreButtonIconColorFilterEnabled(false)


        if (prefs.isCheck) {
            if (prefs.checkingResponse!!.result!!.checkinType == 1) {
                replaceFragment(
                    R.id.frame_container,
                    ManagerValetWorkCheckingFragment.newInstance(),
                    false
                )
            } else {
                replaceFragment(
                    R.id.frame_container,
                    ManagerMyWorkCheckingFragment.newInstance(),
                    false
                )
            }
        } else {
            replaceFragment(
                R.id.frame_container,
                ManagerWorkAssignmentFragment.newInstance(),
                false
            )
        }

        if (notificationType == "MSG") {
            if (channelId.isNotEmpty()) {
                Log.e("CHANNELID HOME", channelId)
            }
            changeFragment(1)
        }
    }

    fun changeFragment(index: Int) {


        selectedTabIndex = index

        when (index) {
            0 -> {

                initToolbar(index)
                currentFragment = 1
                currentFragmentIndex = 0
                if (prefs.isCheck) {

                    if (prefs.checkingResponse!!.result!!.checkinType == 1) {
                        replaceFragment(
                            R.id.frame_container,
                            ManagerValetWorkCheckingFragment.newInstance(),
                            false
                        )
                    } else {
                        replaceFragment(
                            R.id.frame_container,
                            ManagerMyWorkCheckingFragment.newInstance(),
                            false
                        )
                    }
                } else {
                    replaceFragment(
                        R.id.frame_container,
                        ManagerWorkAssignmentFragment.newInstance(),
                        false
                    )
                }
            }
            1 -> {
                currentFragmentIndex = 1
                replaceFragment(
                    R.id.frame_container,
                    ManagerChatFragment.newInstance(channelId),
                    false
                )
            }
            2 -> {

                initToolbar(index)

                if (prefs.isCheck) {
                    currentFragment = 4
                    currentFragmentIndex = 2
                    replaceFragment(
                        R.id.frame_container,
                        ManagerViolationFragment.newInstance(),
                        false
                    )
                } else {

                    AppUtils.showSnackBar(
                        binding.bottomNavigation,
                        getString(R.string.lbl_sacn_qr_)
                    )
                }
            }
            3 -> {
                currentFragmentIndex = 3
                initToolbar(index)
                replaceFragment(R.id.frame_container, ManagerProfileFragment.newInstance(), false)


            }
            8 -> {
                currentFragmentIndex = 8
                initToolbar(index)
                replaceFragment(
                    R.id.frame_container,
                    ManagerValetWorkWithoutCheckingFragment.newInstance(),
                    false
                )

            }
        }
        Handler().postDelayed(Runnable {
            isClick = true
        }, 1000)


    }

    private fun showDailog() {
        AppUtils.showSnackBar(
            binding.bottomNavigation,
            "Hi " + prefs.userDataModel!!.logindata.uFirstName + ", you have a unit audit at " + prefs.checkingResponse!!.result!!.commSchName + ". Have a great shift"
        )
    }

    fun getLocation() {
        locationProvider = MyLocationProvider(this, this)
        locationProvider?.init()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_container)
        fragment!!.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MyLocationProvider.REQUEST_LOCATION_SETTINGS) {
            locationProvider?.onActivityResult(requestCode, resultCode, intent)
        }
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
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
            }
        }
    }
}
