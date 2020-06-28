package com.enviroclean.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.enviroclean.R
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivitySplashBinding
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppConstants.SPLASH_TIME
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.SplashViewModel


class SplashActivity : BaseActivity<SplashViewModel>(SplashActivity::class.java.simpleName) {
    /*viewModel variable*/
    companion object {
        val MESSAGE_PROGRESS = "message_progress"
        val PERMISSION_REQUEST_CODE = 1
        var REMANING_COUNT: String = ""
        fun newInstance(context: Context, blackStackClear: Boolean = false): Intent {
            val intent = Intent(context, SplashActivity::class.java)
            if (blackStackClear) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }
    }

    private val mViewModel: SplashViewModel by lazy {
        ViewModelProviders.of(this).get(SplashViewModel::class.java)
    }

    /*Overriden method to get the viewModel in BaseActivity*/
    override fun getViewModel(): SplashViewModel {
        return mViewModel
    }

    lateinit var prefs: Prefs

    private lateinit var binding: ActivitySplashBinding
    private var handler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        prefs = Prefs.getInstance(this)!!
        init()
        Log.e("TOKEN", "--->" + prefs.accessToken)

        AppUtils.getFiresafeNotifications(prefs)

        binding.tvVersionName.text = AppConstants.VER_NAME
    }

    fun init() {
        handler = Handler()
        handler!!.postDelayed(mRunnable, SPLASH_TIME)

    }

    private val mRunnable: Runnable = Runnable {
        if (!isFinishing) {
            startApp()
        }
    }

    override fun onResume() {
        super.onResume()
        handler!!.postDelayed(mRunnable, SPLASH_TIME)
    }

    override fun onPause() {
        super.onPause()
        handler!!.removeCallbacks(mRunnable)
    }


    var ALL_PERMISSIONS = 101
    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun isStoragePermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.v(TAG, "Permission is granted")
                return true
            } else {

                Log.v(TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    1
                )
                return false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
            startApp()
            return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            0 -> {
                var isPerpermissionForAllGranted = false
                if (grantResults.size > 0 && permissions.size == grantResults.size) {
                    for (i in permissions.indices) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            isPerpermissionForAllGranted = true
                        } else {
                            isPerpermissionForAllGranted = false
                        }
                    }
                    startApp()
                    Log.e("value", "Permission Granted, Now you can use local drive .")
                } else {
                    isPerpermissionForAllGranted = true
                    AppUtils.showSnackBar(
                        binding.tvVersionName,
                        "To get access enable locations from settings"
                    )
                    Log.e("value", "Permission Denied, You cannot use local drive .")
                }
                if (isPerpermissionForAllGranted) {
                    startApp()
                }
            }
        }
    }

    private fun startApp() {
        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);
        if (!isStoragePermissionGranted()) {
            return
        }

        if (prefs.isLoggedIn) {
            if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {
                startActivity(ManagerHomeActivity.newInstance(this))
                AppUtils.startFromRightToLeft(this)
                finishAffinity()
            } else {
                startActivity(ValetHomeActivity.newInstance(this))
                AppUtils.startFromRightToLeft(this)
                finishAffinity()
            }
        } else {
            if (prefs.isTutorialIn) {
                if (prefs.isTouchId) {
                    startActivity(FinagerPrintActivity.newInstance(this, true))
                    AppUtils.startFromRightToLeft(this)
                    finishAffinity()
                } else {
                    startActivity(LoginActivity.newInstance(this, true))
                    AppUtils.startFromRightToLeft(this)
                    finishAffinity()
                }

            } else {
                startActivity(TutorialActivity.newInstance(this, true))
                AppUtils.startFromRightToLeft(this)
                finishAffinity()
            }
        }

    }

}
