package com.enviroclean.receiver

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.enviroclean.R
import com.enviroclean.ui.activity.VoiceActivity
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.SoundPoolManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.twilio.voice.CallException
import com.twilio.voice.CallInvite
import com.twilio.voice.CancelledCallInvite
import com.twilio.voice.MessageListener
import com.twilio.voice.Voice

class VoiceFirebaseMessagingService : FirebaseMessagingService() {

    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Received onMessageReceived()")
        Log.d(TAG, "Bundle data: " + remoteMessage.data)
        Log.d(TAG, "From: " + remoteMessage.from!!)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            val data = remoteMessage.data

            val valid = Voice.handleMessage(this, remoteMessage.data, object : MessageListener {
                override fun onCallInvite(callInvite: CallInvite) {
                    val notificationId = System.currentTimeMillis().toInt()
                    this@VoiceFirebaseMessagingService.notify(callInvite, notificationId)
                    this@VoiceFirebaseMessagingService.sendCallInviteToActivity(
                        callInvite,
                        notificationId
                    )
                }

                override fun onCancelledCallInvite(
                    cancelledCallInvite: CancelledCallInvite,
                    callException: CallException?
                ) {
                    this@VoiceFirebaseMessagingService.cancelNotification(cancelledCallInvite)
                    this@VoiceFirebaseMessagingService.sendCancelledCallInviteToActivity(
                        cancelledCallInvite
                    )
                }

            })

            if (!valid) {
                Log.e(
                    TAG,
                    "The message was not a valid Twilio Voice SDK payload: " + remoteMessage.data
                )
            }

        }
    }

    private fun notify(callInvite: CallInvite, notificationId: Int) {
        val intent = Intent(this, VoiceActivity::class.java)
        intent.action = AppConstants.ACTION_INCOMING_CALL
        intent.putExtra(AppConstants.INCOMING_CALL_NOTIFICATION_ID, notificationId)
        intent.putExtra(AppConstants.INCOMING_CALL_INVITE, callInvite)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        /*
         * Pass the notification id and call sid to use as an identifier to cancel the
         * notification later
         */
        val extras = Bundle()
        extras.putInt(NOTIFICATION_ID_KEY, notificationId)
        extras.putString(CALL_SID_KEY, callInvite.callSid)

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
                .setContentIntent(pendingIntent)
                .setGroup("test_app_notification")
                .setColor(Color.rgb(214, 10, 37))

            notificationManager!!.notify(notificationId, notificationBuilder.build())
        }
    }

    private fun cancelNotification(cancelledCallInvite: CancelledCallInvite) {
        SoundPoolManager.getInstance(this).stopRinging()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /*
             * If the incoming call was cancelled then remove the notification by matching
             * it with the call sid from the list of notifications in the notification drawer.
             */
            val activeNotifications = notificationManager!!.activeNotifications
            for (statusBarNotification in activeNotifications) {
                val notification = statusBarNotification.notification
                val extras = notification.extras
                val notificationCallSid = extras.getString(CALL_SID_KEY)

                if (cancelledCallInvite.callSid == notificationCallSid) {
                    notificationManager!!.cancel(extras.getInt(NOTIFICATION_ID_KEY))
                }
            }
        } else {
            /*
             * Prior to Android M the notification manager did not provide a list of
             * active notifications so we lazily clear all the notifications when
             * receiving a CancelledCallInvite.
             *
             * In order to properly cancel a notification using
             * NotificationManager.cancel(notificationId) we should store the call sid &
             * notification id of any incoming calls using shared preferences or some other form
             * of persistent storage.
             */
            notificationManager!!.cancelAll()
        }
    }

    /*
     * Send the CallInvite to the VoiceActivity. Start the activity if it is not running already.
     */
    private fun sendCallInviteToActivity(callInvite: CallInvite, notificationId: Int) {
        val intent = Intent(this, VoiceActivity::class.java)
        intent.action = AppConstants.ACTION_INCOMING_CALL
        intent.putExtra(AppConstants.INCOMING_CALL_NOTIFICATION_ID, notificationId)
        intent.putExtra(AppConstants.INCOMING_CALL_INVITE, callInvite)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
    }

    /*
     * Send the CancelledCallInvite to the VoiceActivity
     */
    private fun sendCancelledCallInviteToActivity(cancelledCallInvite: CancelledCallInvite) {
        val intent = Intent(AppConstants.ACTION_CANCEL_CALL)
        intent.putExtra(AppConstants.CANCELLED_CALL_INVITE, cancelledCallInvite)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
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

    companion object {

        private val TAG = "VoiceFCMService"
        private val NOTIFICATION_ID_KEY = "NOTIFICATION_ID"
        private val CALL_SID_KEY = "CALL_SID"
        private val VOICE_CHANNEL = "default"
    }
}
