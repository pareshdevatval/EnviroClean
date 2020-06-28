package com.enviroclean.Fingerprint

import android.Manifest
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import androidx.annotation.RequiresApi
import android.Manifest.permission.USE_FINGERPRINT
import android.content.Context
import android.content.pm.PackageManager
import android.os.CancellationSignal
import android.os.Parcel
import android.os.Parcelable
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.R.attr.colorPrimaryDark
import android.app.Activity
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import com.enviroclean.R
import android.R.attr.colorPrimaryDark
import android.view.View
import com.enviroclean.iterfacea.FingerPrint


/**
 * Created by keshu odedara on 04,February,2020
 */

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.M)
class FingerprintHandler(val context:Context, val click: FingerPrint) : FingerprintManager.AuthenticationCallback() {

    fun startAuth(manager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject) {
        val cancellationSignal = CancellationSignal()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.USE_FINGERPRINT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
    }


    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
        this.update("Fingerprint Authentication error\n$errString", false)
    }


    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
        this.update("Fingerprint Authentication help\n$helpString", false)
    }


    override fun onAuthenticationFailed() {
        this.update("Fingerprint Authentication failed.", false)
        click.fingerPrint(false)

    }


    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        this.update("Fingerprint Authentication succeeded.", true)
        click.fingerPrint(true)
    }


    fun update(e: String, success: Boolean) {

        val textView = (context as Activity).findViewById<View>(R.id.errorText) as TextView
        textView.text = e
        if (success) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
    }
}