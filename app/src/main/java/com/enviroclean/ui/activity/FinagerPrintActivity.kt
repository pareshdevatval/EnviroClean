package com.enviroclean.ui.activity

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.enviroclean.Fingerprint.FingerprintHandler
import com.enviroclean.R
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityFingerprintBinding
import com.enviroclean.iterfacea.FingerPrint
import com.enviroclean.model.LoginResponse1
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.LoginViewModel
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey


/**
 * Created by keshu odedara on 04,February,2020
 */
class FinagerPrintActivity :
    BaseActivity<LoginViewModel>(FinagerPrintActivity::class.java.simpleName), FingerPrint {
    override fun fingerPrint(status: Boolean) {
        Log.e("FINGERPRINT", "---->" + status)

        if (isLoginApiCall && status) {

            mViewModel.callApi(prefs.userEmail, prefs.userPassword, "",false)

        } else {
            if (status) {
                prefs.isTouchId = status
                val returnIntent = Intent()
                returnIntent.putExtra("status", status)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            } else {
                prefs.isTouchId = true
            }
        }
    }

    private lateinit var binding: ActivityFingerprintBinding
    private val mViewModel: LoginViewModel by lazy {
        ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }

    override fun getViewModel(): LoginViewModel {
        return mViewModel
    }

    private var keyStore: KeyStore? = null
    // Variable used for storing the key in the Android Keystore container
    private val KEY_NAME = "ENVIRO_CLEAN"
    private var cipher: Cipher? = null
    private var textView: TextView? = null

    private val isLoginApiCall: Boolean by lazy {
        intent.getBooleanExtra(AppConstants.IS_LOGIN_WITH_TOUCH_ID, false)
    }
    private val isRemove: Boolean by lazy {
        intent.getBooleanExtra(AppConstants.IS_FINGER_REMOVE, false)
    }
    lateinit var prefs: Prefs

    companion object {
        fun newInstance(context: Context, isLogin: Boolean = false,isRemove:Boolean=false,backStackClear:Boolean=false): Intent {
            val intent = Intent(context, FinagerPrintActivity::class.java)
            intent.putExtra(AppConstants.IS_LOGIN_WITH_TOUCH_ID, isLogin)
            intent.putExtra(AppConstants.IS_FINGER_REMOVE, isRemove)

            if (backStackClear) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            return intent
        }
    }

    @RequiresApi(M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fingerprint)
        prefs = Prefs.getInstance(this)!!
        init()

    }

    @RequiresApi(M)
    private fun init() {
        mViewModel.getResponse().observe(this, observeLoginData)
        // Initializing both Android Keyguard Manager and Fingerprint Manager
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager


        textView = findViewById<View>(R.id.errorText) as TextView


        // Check whether the device has a Fingerprint sensor.
        if (!fingerprintManager!!.isHardwareDetected) {
            /**
             * An error message will be displayed if the device does not contain the fingerprint hardware.
             * However if you plan to implement a default authentication method,
             * you can redirect the user to a default authentication activity from here.
             * Example:
             * Intent intent = new Intent(this, DefaultAuthenticationActivity.class);
             * startActivity(intent);
             */
            textView!!.setText("Your Device does not have a Fingerprint Sensor")
        } else {
            // Checks whether fingerprint permission is set on manifest
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.USE_FINGERPRINT
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                textView!!.setText("Fingerprint authentication permission not enabled")
            } else {
                // Check whether at least one fingerprint is registered
                if (!fingerprintManager!!.hasEnrolledFingerprints()) {
                    textView!!.setText("Register at least one fingerprint in Settings")
                } else {
                    // Checks whether lock screen security is enabled or not
                    if (!keyguardManager!!.isKeyguardSecure) {
                        textView!!.setText("Lock screen security not enabled in Settings")
                    } else {
                        generateKey()


                        if (cipherInit()) {
                            val cryptoObject = cipher?.let { FingerprintManager.CryptoObject(it) }
                            val helper = FingerprintHandler(this, this)
                            helper.startAuth(fingerprintManager, cryptoObject!!)
                        }
                    }
                }
            }
        }
    }

    private val observeLoginData = Observer<LoginResponse1> {
        if (it.status && it.code == AppConstants.SUCCESSFULLY_CODE) {
            prefs.isLoggedIn = true
            prefs.isTouchId=true
            prefs.userDataModel = it
            prefs.accessToken = it.logindata.token
            prefs.writeString(AppConstants.KEY_USER_TYPE, it.logindata.uType.toString())
            if (it.logindata.uType == 1) {
                if (it.logindata.checkinFlag == 1) {
                    prefs.isCheck = true
                    prefs.checkingResponse = it
                } else {
                    prefs.isCheck = false
                }
                startActivity(ManagerHomeActivity.newInstance(this))
                AppUtils.startFromRightToLeft(this)
                finishAffinity()
            } else {
                if (it.logindata.checkinFlag == 1) {
                    prefs.isCheck = true
                    prefs.checkingResponse = it
                } else {
                    prefs.isCheck = false
                }
                startActivity(ValetHomeActivity.newInstance(this))
                AppUtils.startFromRightToLeft(this)
                finishAffinity()

            }

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: Exception) {
            e.printStackTrace()
        }


        val keyGenerator: KeyGenerator
        try {
            keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        } catch (e: NoSuchProviderException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        }


        try {
            keyStore!!.load(null)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
                    )
                    .build()
            )
            keyGenerator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException(e)
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }


    @TargetApi(Build.VERSION_CODES.M)
    fun cipherInit(): Boolean {
        try {
            cipher =
                Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }


        try {
            keyStore!!.load(
                null
            )
            val key = keyStore!!.getKey(KEY_NAME, null) as SecretKey
            cipher!!.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }

    }

    override fun onBackPressed() {
        if(!isLoginApiCall){
            prefs.isTouchId = false
            val returnIntent = Intent()
            returnIntent.putExtra("status", false)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
        super.onBackPressed()
    }
}