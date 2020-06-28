package com.enviroclean.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.enviroclean.base.BaseViewModel
import com.enviroclean.iterfacea.TwilioFcmStatusListener
import com.enviroclean.model.ChatAccessTokenResponse
import com.enviroclean.model.CreateChannelResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.koushikdutta.ion.Ion
import com.twilio.accessmanager.AccessManager
import com.twilio.chat.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream
import java.io.FileInputStream


/**
 * Created by imobdev on 24/2/20
 */
class ChatViewModel(application: Application) : BaseViewModel(application), ChannelListener,
    AccessManager.Listener,
    AccessManager.TokenUpdateListener {
    override fun onMemberDeleted(p0: Member?) {

    }

    override fun onTypingEnded(channel: Channel?, p1: Member?) {
        Log.e("onTypingEnded", p1!!.identity)
        isTyping.value=false
    }

    override fun onMessageAdded(message: Message?) {
        message.let {
            messageAdd.value = it
        }

    }

    override fun onMessageDeleted(message: Message?) {

    }

    override fun onMemberAdded(p0: Member?) {

    }

    override fun onTypingStarted(channel: Channel?, p1: Member?) {
        Log.e("onTypingStarted", p1!!.identity)
        isTyping.value=true
    }

    override fun onSynchronizationChanged(channel: Channel?) {
        Log.e("", "onSynchronizationChanged: " + channel!!.friendlyName)
    }

    override fun onMessageUpdated(p0: Message?, p1: Message.UpdateReason?) {

    }

    override fun onMemberUpdated(p0: Member?, p1: Member.UpdateReason?) {

    }

    override fun onTokenExpired(p0: AccessManager?) {

    }

    override fun onTokenWillExpire(p0: AccessManager?) {

    }

    override fun onError(p0: AccessManager?, p1: String?) {

    }

    override fun onTokenUpdated(p0: String?) {

    }

    private var subscription: Disposable? = null
    private  var mChannel: Channel?= null
    private var uniqueName: String = ""
    private var fcmToken: String = ""
    private var userName: String = ""
    private lateinit var context: Context
    private var isMsgAdd=true

    private val messageList: MutableLiveData<List<Message>> by lazy {
        MutableLiveData<List<Message>>()
    }

    fun getMessageList(): LiveData<List<Message>> {
        return messageList
    }

    private val channelResponse: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }


    fun getChannelResponse(): LiveData<Boolean> {
        return channelResponse
    }

    private val messageAdd: MutableLiveData<Message> by lazy {
        MutableLiveData<Message>()
    }


    fun getMessageAdd(): LiveData<Message> {
        return messageAdd
    }

    private val sendImageResponse: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }


    fun getSendImageResponse(): LiveData<Boolean> {
        return sendImageResponse
    }

    private val videoDownloadSucess:MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }


    fun getVideoDownloadResponse(): LiveData<Boolean> {
        return videoDownloadSucess
    }

    private val isTyping:MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }


    fun getIsTyping(): LiveData<Boolean> {
        return isTyping
    }

    /* Get Twilio chat access token */
    fun callGetTwilioAccessTokenAPI(userName: String, showProgress: Boolean = true) {
        AppUtils.loge(userName)
        if (AppUtils.hasInternet(getApplication())) {

            val params: HashMap<String, Any?> = HashMap()
            params[ApiParams.KEY_IDENTITY] = userName
            params[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
            subscription = getApiService()
                .getTwilioAccessToken(params)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleTokenResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    /*[START] handle API response here*/
    private fun handleTokenResponse(response: ChatAccessTokenResponse) {
        /*If response is successful, then assign to the response object*/
        if (response.status) {
            createChatClient(context, response.result.token, uniqueName, fcmToken, userName)
        } else {
            apiErrorMessage.value = response.message
        }
    }


    /**
     * Create Twilio ChatClient and Get Message List.
     */
    fun createChatClient(
        context: Context,
        accessToken: String,
        uniqueName: String,
        fcmToken: String,
        userName: String
    ) {
        this.uniqueName = uniqueName
        this.fcmToken = fcmToken
        this.context = context
        this.userName = userName
        if (accessToken.isNotEmpty()) {
            Log.e("userName", userName)
            Log.e("START", "START")
            Log.e("ChatAccessToken", "" + accessToken)
            Log.e("fcmToken", "" + fcmToken)
            this.uniqueName = uniqueName
            this.fcmToken = fcmToken
            loadingVisibility.value = true
            val builder = ChatClient.Properties.Builder()
            val props = builder.createProperties()
           ChatClient.create(context, accessToken, props, mChatClientCallback)
        } else {
            callGetTwilioAccessTokenAPI(userName, false)
        }
    }

    private val mChatClientCallback = object : CallbackListener<ChatClient>() {
        override fun onSuccess(chatClient: ChatClient?) {
            Log.e("CHAT CLIENT", "Success creating Twilio Chat Client")
            chatClient!!.registerFCMToken(
                fcmToken, TwilioFcmStatusListener(
                    "Firebase Messaging registration successful",
                    "Firebase Messaging registration not successful"
                )
            )

            Handler().postDelayed({
                chatClient.channels.getChannel(uniqueName, mChannelCallBack)

            }, 2000)

        }

        override fun onError(errorInfo: ErrorInfo?) {
            super.onError(errorInfo)
            loadingVisibility.value = false
            apiErrorMessage.value = errorInfo!!.message
        }
    }
    /* Get Channel Response */
    private val mChannelCallBack = object : CallbackListener<Channel>() {
        override fun onSuccess(channel: Channel?) {
            Log.e("NAME", channel!!.uniqueName)
            Log.e("SID", channel.sid)
            Log.e("MESSAGE", "" + channel.messages)
            if (channel.messages != null) {
                mChannel = channel
                channel.addListener(this@ChatViewModel)
                getMessageList(channel)
            } else {
                loadingVisibility.value = false
                channelResponse.value = true
            }
        }

        override fun onError(errorInfo: ErrorInfo?) {
            super.onError(errorInfo)
            loadingVisibility.value = false
            apiErrorMessage.value = errorInfo!!.message
            Log.e("", "Error Get channel: " + errorInfo!!.message)
        }
    }

    private fun getMessageList(channel: Channel?) {
        channel!!.messages.getLastMessages(20, mGetMessageCallBack)
    }

    /* Message List callback */
    private val mGetMessageCallBack = object : CallbackListener<List<Message>>() {
        override fun onSuccess(messages: List<Message>?) {
            loadingVisibility.value = false
            messageList.value = messages
        }

        override fun onError(errorInfo: ErrorInfo?) {
            super.onError(errorInfo)
            apiErrorMessage.value = errorInfo!!.message
            Log.e("onError", errorInfo!!.message)
        }
    }

    /* Send Text Message */

    fun sendTextMessage(textMessage: String) {
        val message = Message.options()
        message.withBody(textMessage)

        mChannel?.let {
            it.messages.sendMessage(message, object : CallbackListener<Message>() {
            override fun onSuccess(message: Message?) {
                Log.e("Send Message author", message!!.author)
                Log.e("Message messageBody", message.messageBody)
            }

            override fun onError(errorInfo: ErrorInfo?) {
                super.onError(errorInfo)
                Log.e("Send Message Channel", errorInfo!!.message)
                apiErrorMessage.value = errorInfo!!.message
            }
        })
        }

    }


    /* Send Image Message */
    fun sendImageMessage(filePath: String, fileName: String) {
        val message = Message.options()
        message.withMedia(FileInputStream(filePath), "image/*")
            .withMediaFileName(fileName)
            .withMediaProgressListener(object : ProgressListener() {
                override fun onStarted() {
                    Log.e("onStarted", "Start")
                    // loadingVisibility.value = true
                }

                override fun onProgress(progress: Long) {

                }

                override fun onCompleted(completed: String?) {
                    Log.e("onCompleted", "" + completed)
                    //loadingVisibility.value = false
                }

            })
        mChannel.let {
            it!!.messages.sendMessage(message, sendImageCallbackListener)
        }
    }

    private val sendImageCallbackListener = object : CallbackListener<Message>() {
        override fun onSuccess(message: Message?) {
            Log.e("Send Message author", message!!.author)
            saveImageInSDCard(message.media)
        }

        override fun onError(errorInfo: ErrorInfo?) {
            super.onError(errorInfo)
            Log.e("Send Message Channel", errorInfo!!.message)
            apiErrorMessage.value = errorInfo!!.message
        }

    }

    fun sendVideoMessage(filePath: String, fileName: String) {
        val message = Message.options()
        message.withMedia(FileInputStream(filePath), "video/*")
            .withMediaFileName(fileName)
            .withMediaProgressListener(object : ProgressListener() {
                override fun onStarted() {
                    Log.e("onStarted", "Start")
                    // loadingVisibility.value = true
                }

                override fun onProgress(progress: Long) {

                }

                override fun onCompleted(completed: String?) {
                    Log.e("onCompleted", "" + completed)
                    //loadingVisibility.value = false
                }

            })
        mChannel.let {
          it!!.messages.sendMessage(message, sendVideoCallbackListener)
        }
    }

    private val sendVideoCallbackListener = object : CallbackListener<Message>() {
        override fun onSuccess(message: Message?) {
            Log.e("Send Message author", message!!.author)
        }

        override fun onError(errorInfo: ErrorInfo?) {
            super.onError(errorInfo)
            Log.e("Send Message Channel", errorInfo!!.message)
            apiErrorMessage.value = errorInfo!!.message
        }

    }

    /* Save Image in SDCard when user send or download received image*/
    fun saveImageInSDCard(media: Message.Media) {
        if (media.type.contentEquals("image/*")) {
            val out = ByteArrayOutputStream()
            media.download(out, object : StatusListener() {
                override fun onSuccess() {
                    val content = out.toString()
                    val bmp =
                        BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.toByteArray().size)
                    AppUtils.saveImageToExternalStorage(
                        bmp,
                        media.fileName,
                        media.fileName.substringAfter(".")
                    )
                    sendImageResponse.value = true
                    Log.e("Downloaded media", content)
                }

                override fun onError(error: ErrorInfo?) {
                    Log.e("Error downloading media", "")
                }
            }, object : ProgressListener() {
                override fun onStarted() {
                    Log.e("Download started", "")
                }

                override fun onProgress(bytes: Long) {
                    Log.e("Downloaded $bytes bytes", "")
                }

                override fun onCompleted(mediaSid: String) {
                    Log.e("Download completed", "")
                }
            })
        }
    }


    fun saveVideoInSDCard(media: Message.Media) {
        if (media.type.contentEquals("video/*")) {
            val out = ByteArrayOutputStream()
            media.download(out, object : StatusListener() {
                override fun onSuccess() {
                    val content = out.toString()
                    AppUtils.saveVideoToExternalStorage(media.fileName,out)
                    Log.e("Downloaded media", content)
                    videoDownloadSucess.value=true
                }

                override fun onError(error: ErrorInfo?) {
                    Log.e("Error downloading media", "")
                    videoDownloadSucess.value=true
                }
            }, object : ProgressListener() {
                override fun onStarted() {
                    Log.e("Download started", "")
                }

                override fun onProgress(bytes: Long) {
                    Log.e("Downloaded $bytes bytes", "")
                }

                override fun onCompleted(mediaSid: String) {
                    Log.e("Download completed", "")
                }
            })
        }
    }


    /* Create Channel API */

    private val createChannelResponse: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getCreateChannelResponse(): LiveData<String> {
        return createChannelResponse
    }


    fun createChannel(userId: String, showProgress: Boolean = true) {
        if (AppUtils.hasInternet(getApplication())) {
            val param: HashMap<String, Any?> = HashMap()
            param[ApiParams.KEY_USER_TO_ID] = userId
            param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
            subscription = getApiService()
                .createChannel(param)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { if (showProgress) onApiStart() }
                .doOnTerminate { if (showProgress) onApiFinish() }
                .subscribe(this::handleResponse, this::handleError)
        } else {
            onInternetError()
        }
    }

    /*[START] handle API response here*/
    private fun handleResponse(response: CreateChannelResponse) {
        /*If response is successful, then assign to the response object*/
        if (response.status) {
            this.createChannelResponse.value = response.channel_id
        } else {
            apiErrorMessage.value = response.message
        }
    }
    /*[END] handle API response here*/

    override fun onCleared() {
        super.onCleared()
        subscription?.dispose()
    }

    fun setTyping() {
        mChannel?.typing()

    }

    fun removeListener() {
        if (mChannel!=null){
            mChannel.let {
                it!!.removeAllListeners()
            }

        }

    }

    private val messageMoreList: MutableLiveData<List<Message>> by lazy {
        MutableLiveData<List<Message>>()
    }


    fun getMessageMoreList(): LiveData<List<Message>> {
        return messageMoreList
    }

    fun loadMoreMessage(index: Long, count: Int) {
        loadingVisibility.value = false
        mChannel!!.messages.getLastMessages(count, mGetMoreMessageCallBack)
    }

    private val mGetMoreMessageCallBack = object : CallbackListener<List<Message>>() {
        override fun onSuccess(messages: List<Message>?) {
            loadingVisibility.value = false
            messageMoreList.value = messages
        }

        override fun onError(errorInfo: ErrorInfo?) {
            super.onError(errorInfo)
            apiErrorMessage.value = errorInfo!!.message
            Log.e("onError", errorInfo!!.message)
        }
    }

    private val sendVoiceAccessToken: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }


    fun getVoiceAccessToken(): LiveData<String> {
        return sendVoiceAccessToken
    }


    fun createVoiceAccessToken(context:Context,toCall:String,fromCall:String){
        val from=  fromCall.replace("\\s".toRegex(), "")
        val to=toCall.replace("\\s".toRegex(), "")
        Ion.with(context).load("${AppConstants.VOICE_ACCESS_TOKEN_URL}?identity=$from").asString()
            .setCallback { e, accessToken ->
                if (e == null) {
                    Log.e("Access token", "Access token: $accessToken")
                    sendVoiceAccessToken.value= accessToken

                } else {

                }
            }
    }
}