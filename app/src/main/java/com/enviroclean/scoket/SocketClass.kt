package com.enviroclean.scoket

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.enviroclean.R
import com.enviroclean.iterfacea.MessageInterface
import com.enviroclean.iterfacea.ReconnectSocketInterface
import com.enviroclean.ui.activity.ManagerHomeActivity
import com.enviroclean.ui.activity.ValetHomeActivity
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.Prefs
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject
import androidx.core.content.ContextCompat.getSystemService
import com.enviroclean.utils.AppUtils


/**
 * Created by imobdev-paresh on 22,January,2020
 */
class SocketClass : WebSocketListener {
    lateinit var command: String
    lateinit var comId: String
    lateinit var context: Context
    lateinit var messageInterface: MessageInterface
    lateinit var reconnected: ReconnectSocketInterface
    lateinit var prefs: Prefs
    var handler = Handler()

    constructor() {
    }

    constructor(
        command: String,
        comId: String,
        context: Context,
        messageInterface: MessageInterface,
        reconnected: ReconnectSocketInterface
    ) {
        this.command = command
        this.comId = comId
        this.context = context
        this.messageInterface = messageInterface
        this.reconnected = reconnected

    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("command", command)
            jsonObject.put("com_id", comId)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        webSocket.send(jsonObject.toString())
        Log.e("SOCKET_SUBSCRIBE", "--->" + jsonObject.toString())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {

        handler.post {
            ValetHomeActivity.socketConnectedValets = true
            ManagerHomeActivity.socketConnectedManager = true

            Log.e("SOCKET_RECEIVE_RESPONSE", "" + JSONObject(text))

            if (JSONObject(text).getInt("code") == 200 ||JSONObject(text).getInt("code")
                == 206 ||JSONObject(text).getInt("code") == 205) {
                messageInterface.massagesRecived(JSONObject(text))
            }
            prefs = Prefs.getInstance(context)!!
            if(prefs.isCheck){
                if(JSONObject(text).getInt("code") == 600){
                    AppUtils.showToast(context,JSONObject(text).getString("message"))
                    vibrate()
                    val mScanMusic = MediaPlayer.create(context, R.raw.alreadyscannedqr)
                    mScanMusic.start()
                }
                if(JSONObject(text).getInt("code") == 60){
                    AppUtils.showToast(context,JSONObject(text).getString("message"))
                    vibrate()
                    val mScanMusic = MediaPlayer.create(context, R.raw.wrong_scan)
                    mScanMusic.start()
                }
                if(JSONObject(text).getInt("code") == 200){
                    val mType = JSONObject(text).getJSONObject("result").getInt("qr_type")
                    if(mType==1){
                        AppUtils.showToast(context,JSONObject(text).getString("message"))
                        vibrate()
                        val mScanMusic = MediaPlayer.create(context, R.raw.scan)
                        mScanMusic.start()
                    }
                }
            }

        }
    }

    private fun vibrate(){
        val vibrator = getSystemService(context, Vibrator::class.java)
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(2000)
            }
        }
    }
    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.e("SOCKET_RECEIVE_RESPONSE", "Closing-->" + reason)
        prefs = Prefs.getInstance(context)!!
        if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {
            ManagerHomeActivity.socketConnectedManager = false
        } else {
            ValetHomeActivity.socketConnectedValets = false
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("SOCKET_RECEIVE_RESPONSE", "Failure->" + t.message)
        prefs = Prefs.getInstance(context)!!

        if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {
            ManagerHomeActivity.socketConnectedManager = false
        } else {
            ValetHomeActivity.socketConnectedValets = false
        }
    }

}