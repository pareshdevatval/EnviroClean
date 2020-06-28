package com.enviroclean.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enviroclean.R
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityVoiceBinding
import com.enviroclean.iterfacea.MessageInterface
import com.enviroclean.iterfacea.ReconnectSocketInterface
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppConstants.ACTION_CANCEL_CALL
import com.enviroclean.utils.AppConstants.ACTION_FCM_TOKEN
import com.enviroclean.utils.AppConstants.ACTION_INCOMING_CALL
import com.enviroclean.utils.AppConstants.INCOMING_CALL_INVITE
import com.enviroclean.utils.AppConstants.INCOMING_CALL_NOTIFICATION_ID
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.utils.SoundPoolManager
import com.enviroclean.viewmodel.VoiceVewModel
import com.google.firebase.iid.FirebaseInstanceId
import com.twilio.voice.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class VoiceActivity : BaseActivity<VoiceVewModel>(VoiceActivity::class.java.simpleName),
    MessageInterface, ReconnectSocketInterface {
    override fun reconnected() {

    }

    override fun massagesRecived(jsonObject: JSONObject?) {
        updateHokyTalky(jsonObject)
    }

    @Subscribe
    fun onEvent(jsonObject: JSONObject?) {
        updateHokyTalky(jsonObject)
    }


    override fun getViewModel(): VoiceVewModel {
        return mViewModel
    }

    private val mViewModel: VoiceVewModel by lazy {
        ViewModelProviders.of(this).get(VoiceVewModel::class.java)
    }

    lateinit var prefs:Prefs

    private var accessToken: String? = null
    private var audioManager: AudioManager? = null
    private var savedAudioMode = AudioManager.MODE_INVALID

    private var isReceiverRegistered = false
    private var voiceBroadcastReceiver: VoiceBroadcastReceiver? = null

    // Empty HashMap, never populated for the Quickstart
    internal var params = HashMap<String, String>()

    private var soundPoolManager: SoundPoolManager? = null

    private var notificationManager: NotificationManager? = null
    private var alertDialog: AlertDialog? = null
    private var activeCallInvite: CallInvite? = null
    private var activeCall: Call? = null
    private var activeCallNotificationId: Int = 0

    private var registrationListener = registrationListener()
    private var callListener = callListener()

    private val MIC_PERMISSION_REQUEST_CODE = 1

    private val receiverId: Int by lazy {
        intent.getIntExtra(AppConstants.RECEVER_USER_ID,0)
    }
    private val isRecevied: Boolean by lazy {
        intent.getBooleanExtra(AppConstants.CALL_RECEVED,false)
    }
     var mReceivred:Int=0
    companion object {

        fun newInstance(
            context: Context,
            from: String,
            to: String,
            receiverId: Int,isRecevied:Boolean
        ): Intent {
            val intent = Intent(context, VoiceActivity::class.java)
            intent.putExtra(AppConstants.KEY_FROM, from)
            intent.putExtra(AppConstants.KEY_TO, to)
            intent.putExtra(AppConstants.RECEVER_USER_ID, receiverId)
            intent.putExtra(AppConstants.CALL_RECEVED, isRecevied)
            return intent
        }

    }


    private lateinit var binding: ActivityVoiceBinding
    private var fromCall = ""
    private var toCall = ""
    var  mAudioManager:AudioManager?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_voice)
        fromCall = intent.getStringExtra(AppConstants.KEY_FROM)!!
        toCall = intent.getStringExtra(AppConstants.KEY_TO)!!
        binding.voice = this
        mAudioManager =  getSystemService(Context.AUDIO_SERVICE) as AudioManager
        EventBus.getDefault().register(this)
        mReceivred=receiverId
        prefs= Prefs.getInstance(this)!!
        // These flags ensure that the activity can be launched when the screen is locked.
        val window = window
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setObserver()
        if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1){
            if(!ManagerHomeActivity.socketConnectedManager){
                socketRegister()
            }
        }else{
            if(!ValetHomeActivity.socketConnectedValets){
                socketRegister()
            }
        }
        if(!isRecevied){
            binding.btnEndCall.visibility=View.VISIBLE
        }
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        soundPoolManager = SoundPoolManager.getInstance(this)

        /*
         * Setup the broadcast receiver to be notified of FCM Token updates
         * or incoming call invite in this Activity.
         */
        voiceBroadcastReceiver = VoiceBroadcastReceiver()
        registerReceiver()

        /*
         * Needed for setting/abandoning audio focus during a call
         */
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager!!.isSpeakerphoneOn = true

        /*
         * Enable changing the volume using the up/down keys during a conversation
         */
        volumeControlStream = AudioManager.STREAM_VOICE_CALL


        /*
         * Displays a call dialog if the intent contains a call invite
         */
        handleIncomingCallIntent(intent)

        /*
         * Ensure the microphone permission is enabled
         */
        if (!checkPermissionForMicrophone()) {
            requestPermissionForMicrophone()
        } else {
            if (fromCall.isNotEmpty() && toCall.isNotEmpty()) {
                retrieveAccessToken()
            }

        }
        buttonPressEvent()
    }


    private fun buttonPressEvent() {
        binding.btnEndCall.setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(v:View, event: MotionEvent):Boolean {
                val eventaction = event.getAction()
                when (eventaction) {
                    MotionEvent.ACTION_DOWN -> {
                        AppUtils.loge("ACTION_DOWN")
                        socketAPICall("start_talk")
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        socketAPICall("end_talk")
                        AppUtils.loge("ACTION_UP")
                    }
                }
                // tell the system that we handled the event but a further processing is required
                return false
            }
        })
    }

    private fun socketRegister() {

        var commId:String=""
        if(prefs.isCheck){
            commId=prefs.checkingResponse!!.result!!.commId.toString()
        }else{
            commId="0"
        }
        AppUtils.startSocketConnection("subscribe", commId, this, this, this)

        val jsonObject = JSONObject()
        try {
            jsonObject.put("command", "register")
            jsonObject.put("userId", prefs.userDataModel!!.logindata.uId)
            AppUtils.loge(jsonObject.toString(),"SOCKET_REGISTER")
            AppUtils.webSocket.send(jsonObject.toString())

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
            }else{
            }
            handler.postDelayed(this, 5000)
        }
    }

    private fun socketAPICall(command:String) {
        val jsonScanQR = JSONObject()
        try {
            jsonScanQR.put("command", command)
            jsonScanQR.put("sender_id", prefs.userDataModel!!.logindata.uId)
            jsonScanQR.put("receiver_id", mReceivred)

            Log.e("SOCKET_SEND_RESPONSE", "---->" + jsonScanQR.toString())
            AppUtils.webSocket.send(jsonScanQR.toString())

        } catch (e: JSONException) {

            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingCallIntent(intent)
    }


    private fun registrationListener(): RegistrationListener {
        return object : RegistrationListener {
            override fun onRegistered(accessToken: String, fcmToken: String) {
                Log.e(TAG, "Successfully registered FCM $fcmToken")
            }

            override fun onError(
                error: RegistrationException,
                accessToken: String,
                fcmToken: String
            ) {
                val message =
                    String.format("Registration Error: %d, %s", error.errorCode, error.message)
                Log.e(TAG, message)

                //Snackbar.make(coordinatorLayout!!, message, SNACKBAR_DURATION).show()
            }
        }
    }

    private fun callListener(): Call.Listener {
        return object : Call.Listener {
            /*
             * This callback is emitted once before the Call.Listener.onConnected() callback when
             * the callee is being alerted of a Call. The behavior of this callback is determined by
             * the answerOnBridge flag provided in the Dial verb of your TwiML application
             * associated with this client. If the answerOnBridge flag is false, which is the
             * default, the Call.Listener.onConnected() callback will be emitted immediately after
             * Call.Listener.onRinging(). If the answerOnBridge flag is true, this will cause the
             * call to emit the onConnected callback only after the call is answered.
             * See answeronbridge for more details on how to use it with the Dial TwiML verb. If the
             * twiML response contains a Say verb, then the call will emit the
             * Call.Listener.onConnected callback immediately after Call.Listener.onRinging() is
             * raised, irrespective of the value of answerOnBridge being set to true or false
             */
            override fun onRinging(call: Call) {
                Log.e(TAG, "Ringing")
            }

            override fun onConnectFailure(call: Call, error: CallException) {
                setAudioFocus(false)
                Log.e(TAG, "Connect failure")
                val message = String.format("Call Error: %d, %s", error.errorCode, error.message)
                Log.e(TAG, message)
                AppUtils.showSnackBar(binding.root, message)
                finish()
                //Snackbar.make(coordinatorLayout!!, message, SNACKBAR_DURATION).show()
            }

            override fun onConnected(call: Call) {
                setAudioFocus(true)
                Log.e(TAG, "Connected--->")

                activeCall = call
            }

            override fun onReconnecting(call: Call, callException: CallException) {
                Log.e(TAG, "onReconnecting")
            }

            override fun onReconnected(call: Call) {
                Log.e(TAG, "onReconnected")
            }

            override fun onDisconnected(call: Call, error: CallException?) {
                setAudioFocus(false)
                Log.e(TAG, "Disconnected")
               finish()
                if (error != null) {
                    val message =
                        String.format("Call Error: %d, %s", error.errorCode, error.message)
                    Log.e(TAG, message)
                    AppUtils.showSnackBar(binding.root, message)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver()
        disconnect()
    }

    public override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        handler.removeCallbacks(checkSocketConnection)
        handler.removeCallbacksAndMessages(null)
        if(!prefs.isCheck){
            if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1){
                AppUtils.client!!.dispatcher.executorService.shutdown()
                AppUtils.webSocket.cancel()
                ManagerHomeActivity.socketConnectedManager = false
            }else{
                AppUtils.client!!.dispatcher.executorService.shutdown()
                AppUtils.webSocket.cancel()
                ValetHomeActivity.socketConnectedValets = false
            }

        }
        soundPoolManager!!.release()
        super.onDestroy()
    }

    private fun handleIncomingCallIntent(intent: Intent?) {

        if (intent != null && intent.action != null) {
            if (intent.action == ACTION_INCOMING_CALL) {
                activeCallInvite = intent.getParcelableExtra(INCOMING_CALL_INVITE)
                if (activeCallInvite != null) {
                    answer()
                    binding.tvUserName.text = ""+activeCallInvite!!.from+" is calling."

                    activeCallNotificationId = intent.getIntExtra(INCOMING_CALL_NOTIFICATION_ID, 0)

                } else {

                }
            } else if (intent.action == ACTION_CANCEL_CALL) {
                if (alertDialog != null && alertDialog!!.isShowing) {
                    soundPoolManager!!.stopRinging()
                    alertDialog!!.cancel()
                }
            } else if (intent.action == ACTION_FCM_TOKEN) {
                retrieveAccessToken()
            }else{

            }
        }
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_INCOMING_CALL)
            intentFilter.addAction(ACTION_CANCEL_CALL)
            intentFilter.addAction(ACTION_FCM_TOKEN)
            LocalBroadcastManager.getInstance(this).registerReceiver(
                voiceBroadcastReceiver!!, intentFilter
            )
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(voiceBroadcastReceiver!!)
            isReceiverRegistered = false
        }
    }

    private inner class VoiceBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == ACTION_INCOMING_CALL || action == ACTION_CANCEL_CALL) {
                /*
                 * Handle the incoming or cancelled call invite
                 */
                handleIncomingCallIntent(intent)
            }
        }
    }


    /*
     * Register your FCM token with Twilio to receive incoming call invites
     *
     * If a valid google-services.json has not been provided or the FirebaseInstanceId has not been
     * initialized the fcmToken will be null.
     *
     * In the case where the FirebaseInstanceId has not yet been initialized the
     * VoiceFirebaseInstanceIDService.onTokenRefresh should result in a LocalBroadcast to this
     * activity which will attempt registerForCallInvites again.
     *
     */
    private fun registerForCallInvites() {
        val fcmToken = FirebaseInstanceId.getInstance().token
        if (fcmToken != null) {
            Log.i(TAG, "Registering with FCM")
            Voice.register(
                accessToken!!,
                Voice.RegistrationChannel.FCM,
                fcmToken,
                registrationListener
            )
        }
    }

    fun clickCancel() {
    }

    private fun answer() {
        activeCallInvite!!.accept(this, callListener)
        notificationManager!!.cancel(activeCallNotificationId)
        Log.e("AAAAAAA","------------->")
    }

    /*
     * Disconnect from Call
     */
    private fun disconnect() {
        if (activeCall != null) {
            activeCall!!.disconnect()
            activeCall = null
        }
    }


    private fun setAudioFocus(setFocus: Boolean) {
        if (audioManager != null) {
            if (setFocus) {
                savedAudioMode = audioManager!!.mode
                // Request audio focus before making any device switch.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val playbackAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    val focusRequest =
                        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                            .setAudioAttributes(playbackAttributes)
                            .setAcceptsDelayedFocusGain(true)
                            .setOnAudioFocusChangeListener { }
                            .build()
                    audioManager!!.requestAudioFocus(focusRequest)
                } else {
                    val focusRequestResult = audioManager!!.requestAudioFocus(
                        { }, AudioManager.STREAM_VOICE_CALL,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                    )
                }
                /*
                 * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
                 * required to be in this mode when playout and/or recording starts for
                 * best possible VoIP performance. Some devices have difficulties with speaker mode
                 * if this is not set.
                 */
                audioManager!!.mode = AudioManager.MODE_IN_COMMUNICATION
            } else {
                audioManager!!.mode = savedAudioMode
                audioManager!!.abandonAudioFocus(null)
            }
        }
    }

    private fun checkPermissionForMicrophone(): Boolean {
        val resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        return resultMic == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("WrongConstant")
    private fun requestPermissionForMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                MIC_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("WrongConstant")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        /*
         * Check if microphone permissions is granted
         */
        if (requestCode == MIC_PERMISSION_REQUEST_CODE && permissions.size > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {

            } else {
                retrieveAccessToken()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setObserver() {
        mViewModel.getAccessToken().observe({ this.lifecycle }, { token: String? ->
            accessToken = token
            registerForCallInvites()
            val to = toCall.replace("\\s".toRegex(), "")
            binding.tvUserName.text = "Calling to $toCall"
            params["to"] = to
            val connectOptions = ConnectOptions.Builder(accessToken!!)
                .params(params)
                .build()
            activeCall = Voice.connect(this, connectOptions, callListener)
        })
    }

    /*
     * Get an access token from your Twilio access token server
     */
    private fun retrieveAccessToken() {
        if (fromCall.isNotEmpty() && toCall.isNotEmpty()) {
            mViewModel.createAccessToken(this, toCall, fromCall)

        }

    }


    fun updateHokyTalky(jsonObject: JSONObject?) {
        val senderID= jsonObject!!.getJSONObject("result").getInt("sender_id")
        val reciverID=jsonObject!!.getJSONObject("result").getInt("receiver_id")
        Log.e("SENDERID----","---->"+jsonObject.getInt("code"))
        Log.e("SENDER ID","------->>>>>"+senderID)
        if(prefs.userDataModel!!.logindata.uId==senderID){
            mReceivred=reciverID
        }else{
            mReceivred=senderID
        }

        if(jsonObject.getInt("code")==205){

            if(prefs.userDataModel!!.logindata.uId==senderID){
                mAudioManager!!.isMicrophoneMute = false
                binding.btnEndCall.visibility=View.VISIBLE
            }else{
                mAudioManager!!.isMicrophoneMute = true
                binding.btnEndCall.visibility=View.GONE
            }
        }
        if(jsonObject.getInt("code")==206){
            binding.btnEndCall.visibility=View.VISIBLE
            mAudioManager!!.isMicrophoneMute = true
        }
    }
}
