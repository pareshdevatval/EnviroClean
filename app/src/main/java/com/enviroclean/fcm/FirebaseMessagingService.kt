package com.enviroclean.fcm

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.enviroclean.R
import com.enviroclean.ui.activity.*
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.twilio.voice.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.*


class FirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseToken"
    private lateinit var notificationManager: NotificationManager
    private val ADMIN_CHANNEL_ID = "ENVIRO_CLEAN"
    lateinit var intent: Intent
    lateinit var prefs: Prefs
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "firebase token->$token")

        // Notify Activity of FCM token
        val intent = Intent(AppConstants.ACTION_FCM_TOKEN)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        var isOnChatScreen: Boolean = false
        var chatUserChannelId = ""
        private val TAG = "VoiceFCMService"
        private val NOTIFICATION_ID_KEY = "NOTIFICATION_ID"
        private val CALL_SID_KEY = "CALL_SID"
        private val VOICE_CHANNEL = "default"
    }

    private var isMassges=true
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        prefs = Prefs.getInstance(this)!!
        Log.i(TAG, "MessageReceived" + remoteMessage.data)
        remoteMessage.let { message ->
            val map: Map<String, String> = message.data


            if (map.containsKey("android")) {

                    val json = JSONObject(map["android"])
                    Log.e("FIREBASE_MSG", "--->" + json)

                    if(json.getInt("type")==5){
                        prefs.violationAreaSize = ""
                        if (!AppUtils.isAppOnForeground(this, applicationContext.packageName)) {
                            Log.e("BACKGROUND", "---->TRUE")
                            prefs.clearCheckinDate()
                        } else {
                            Log.e("BACKGROUND", "---->FALSE")
                            EventBus.getDefault().post(AppConstants.AUTO_LOGOUT)
                        }
                    }else{
                        prefs.notificationCount=json.getInt("noti_count").toString()
                        EventBus.getDefault().post(AppConstants.NOTIFICATION_COUNT)
                        if (!AppUtils.isAppOnForeground(this, applicationContext.packageName)) {
                            Log.e("BACKGROUND", "---->TRUE")
                            showPushNotification(json)
                        } else {
                            Log.e("BACKGROUND", "---->FALSE")
                            openDialog(json)
                        }
                    }


            } else {

                if (!AppUtils.isAppOnForeground(this, applicationContext.packageName)) {
                    Log.e("BACKGROUND", "---->TRUE")
                    val jsonObject = JSONObject(map)
                    val msgType = jsonObject.getString("twi_message_type")
                    if (msgType == "twilio.channel.new_message") {
                        sendNewMessageNotification(map)
                    } else {
                        sendCallNotification(remoteMessage)

                    }

                } else {
                    Log.e("BACKGROUND", "---->FALSE")
                    val jsonObject = JSONObject(map)
                    val msgType = jsonObject.getString("twi_message_type")
                    if (msgType == "twilio.channel.new_message") {
                        var channel_id = jsonObject.getString("channel_id")

                        if (chatUserChannelId == channel_id) {

                        } else if (!isOnChatScreen) {
                            sendNewMessageNotification(map)
                        } else {
                            sendNewMessageNotification(map)
                        }
                    } else {
                        sendCallNotification(remoteMessage)
                    }
                }
            }
        }
    }


    private fun sendCallNotification(remoteMessage: RemoteMessage) {
        val valid = Voice.handleMessage(this, remoteMessage.data, object :
            MessageListener {
            override fun onCallInvite(callInvite: CallInvite) {
                val notificationId = System.currentTimeMillis().toInt()
                notify(callInvite, notificationId)
            }

            override fun onCancelledCallInvite(
                cancelledCallInvite: CancelledCallInvite,
                callException: CallException?
            ) {
                notificationManager.cancelAll()
            }

        })

        if (!valid) {
            Log.e(
                TAG,
                "The message was not a valid Twilio Voice SDK payload: " + remoteMessage.data
            )
        }
    }
    private fun openDialog(json: JSONObject) {
        startActivity(
            DialogActivity.newInstant(
                this,
                json.getString("message"),
                json.getString("title")
            )
        )
    }


    private fun showPushNotification(json: JSONObject) {
        intent = NotificationListActivity.newInstance(this,true)
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //Setting up Notification channels for android O and above
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupNotificationChannels()
        }
        val notificationId = Random().nextInt(60000)

        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)  //a resource for your custom small icon
            .setContentTitle(json.getString("title")) //the "title" value you sent in your notification
            .setContentText(json.getString("message")) //ditto
            .setAutoCancel(true)  //dismisses the notification on click
            .setSound(defaultSoundUri)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(
            notificationId /* ID of notification */,
            notificationBuilder.build()
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupNotificationChannels() {

        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        val adminChannelName = "chanel"
        val adminChannelDescription = "chanell"
        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(
            ADMIN_CHANNEL_ID,
            adminChannelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        adminChannel.setSound(defaultSoundUri, audioAttributes)
        notificationManager.createNotificationChannel(adminChannel)
    }

    @SuppressLint("WrongConstant")
    private fun sendNewMessageNotification(map: Map<String, String>) {
        val jsonObject = JSONObject(map)
        var channel_id = jsonObject.getString("channel_id")
        var fromUserID = jsonObject.getString("author")
        var message = jsonObject.getString("twi_body")

        if (prefs.isLoggedIn){
            if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1){
                intent = ManagerHomeActivity.newInstance(this, true,channel_id,"MSG")
            }else{
                intent = ValetHomeActivity.newInstance(this, true,channel_id,"MSG")
            }
        }else{
            intent = NotificationListActivity.newInstance(this, true)
        }

        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //Setting up Notification channels for android O and above
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupNotificationChannels()
        }
        val notificationId = Random().nextInt(60000)

        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)  //a resource for your custom small icon
            .setContentTitle("New Message") //the "title" value you sent in your notification
            .setContentText(fromUserID+":"+message) //ditto
            .setAutoCancel(true)  //dismisses the notification on click
            .setSound(defaultSoundUri)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(
            notificationId /* ID of notification */,
            notificationBuilder.build()
        )
    }


    private fun notify(callInvite: CallInvite, notificationId: Int) {
        val intent = Intent(this, VoiceActivity::class.java)
        intent.action = AppConstants.ACTION_INCOMING_CALL
        intent.putExtra(AppConstants.KEY_FROM, "")
        intent.putExtra(AppConstants.KEY_TO, "")
        intent.putExtra(AppConstants.INCOMING_CALL_NOTIFICATION_ID, notificationId)
        intent.putExtra(AppConstants.INCOMING_CALL_INVITE, callInvite)
        intent.putExtra(AppConstants.CALL_RECEVED, true)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        /*
         * Pass the notification id and call sid to use as an identifier to cancel the
         * notification later
         */
        val extras = Bundle()
        extras.putInt(NOTIFICATION_ID_KEY, notificationId)
        extras.putString(CALL_SID_KEY, callInvite.callSid)

        val defaultSoundUri =
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val callInviteChannel = NotificationChannel(
                VOICE_CHANNEL,
                "Primary Voice Channel", NotificationManager.IMPORTANCE_DEFAULT
            )
            callInviteChannel.lightColor = Color.GREEN
            callInviteChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager!!.createNotificationChannel(callInviteChannel)

            val notification = buildNotification(
                callInvite.from!! + " is calling.",
                pendingIntent,
                extras
            )

            notificationManager!!.notify(notificationId, notification)
        } else {
            val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_call_end_white_24dp)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(callInvite.from!! + " is calling.")
                .setAutoCancel(true)
                .setExtras(extras)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setGroup("Walky Talky Notification")
                .setColor(Color.rgb(214, 10, 37))

            notificationManager!!.notify(notificationId, notificationBuilder.build())
        }
    }

    /**
     * Build a notification.
     *
     * @param text          the text of the notification
     * @param pendingIntent the body, pending intent for the notification
     * @param extras        extras passed with the notification
     * @return the builder
     */
    @TargetApi(Build.VERSION_CODES.O)
    fun buildNotification(
        text: String,
        pendingIntent: PendingIntent,
        extras: Bundle
    ): Notification {
        return Notification.Builder(applicationContext, VOICE_CHANNEL)
            .setSmallIcon(R.drawable.ic_call_end_white_24dp)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setExtras(extras)
            .setAutoCancel(true)
            .build()
    }

}