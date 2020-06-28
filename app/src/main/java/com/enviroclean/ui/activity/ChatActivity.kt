package com.enviroclean.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.enviroclean.R
import com.enviroclean.adapter.MessageListAdapter
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.databinding.ActivityChatBinding
import com.enviroclean.databinding.DialogImageVideoSelectionBinding
import com.enviroclean.fcm.FirebaseMessagingService
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.utils.filePick.FilePickUtils
import com.enviroclean.viewmodel.ChatViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.iid.FirebaseInstanceId
import com.twilio.chat.Message
import com.twilio.voice.ConnectOptions
import com.twilio.voice.RegistrationException
import com.twilio.voice.RegistrationListener
import com.twilio.voice.Voice
import kotlinx.android.synthetic.main.row_chat_left_list.view.*
import kotlinx.android.synthetic.main.row_chat_left_list.view.videoProgress
import kotlinx.android.synthetic.main.row_chat_right_list.view.*
import java.io.File
import java.text.SimpleDateFormat

class ChatActivity : BaseActivity<ChatViewModel>(ChatActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener, BaseBindingAdapter.ItemClickListener<Message>,
    View.OnClickListener {
    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.tvSelectPhoto -> {
                filePickUtils?.requestImageCamera(
                    AppConstants.PICK_IMAGE_CAMERA_REQUEST_CODE,
                    allowCrop = false,
                    isFixedRatio = false
                )
                mImageSelectionDialog.cancel()
            }

            R.id.tvSelectVideo -> {

                val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                videoFileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO)
                // set the image file name
                intent.putExtra(MediaStore.EXTRA_OUTPUT, videoFileUri)

                // set the video image quality to high
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                startActivityForResult(intent, AppConstants.PICK_VIDEO_REQUEST_CODE)
                mImageSelectionDialog.cancel()
            }

        }
    }

    override fun onItemClick(view: View, data: Message?, position: Int) {
        when (view.id) {
            R.id.clDownloadImgLeft, R.id.ivImgDwLeft -> {
                isImgDowload = true
                isSelectLeft = true
                isSelectRigth = false
                if (checkAndRequestPermissions()) {
                    if (data!!.media.type == "image/*") {
                        videoDoalodViewLeft =
                            binding.rvChat.findViewHolderForLayoutPosition(position)!!
                        mViewModel.saveImageInSDCard(data.media)
                        if (!videoDoalodViewLeft.itemView.imgProgressLeft.isVisible) {
                            videoDoalodViewLeft.itemView.imgProgressLeft.visibility = View.VISIBLE
                            videoDoalodViewLeft.itemView.ivImgDwLeft.visibility = View.GONE
                        }
                    }
                }
            }
            R.id.clDownloadImgRight, R.id.ivImgDwRight -> {
                isImgDowload = true
                isSelectLeft = false
                isSelectRigth = true
                if (checkAndRequestPermissions()) {
                    if (data!!.media.type == "image/*") {
                        videoDoalodViewRight =
                            binding.rvChat.findViewHolderForLayoutPosition(position)!!
                        mViewModel.saveImageInSDCard(data.media)
                        if (!videoDoalodViewRight.itemView.imgProgressRight.isVisible) {
                            videoDoalodViewRight.itemView.imgProgressRight.visibility = View.VISIBLE
                            videoDoalodViewRight.itemView.ivImgDwRight.visibility = View.GONE
                        }
                    }
                }
            }
            R.id.ivChatVideoLeft, R.id.clChatVideoLeft -> {
                isSelectLeft=true
                if (AppUtils.fileIsSaveOrNot(data!!.media.fileName).isNotEmpty()) {
                    startActivity(
                        PlayVideoActivity.newInstance(
                            this,
                            AppUtils.fileIsSaveOrNot(data!!.media.fileName)
                        )
                    )
                } else {
                    videoDoalodViewLeft = binding.rvChat.findViewHolderForLayoutPosition(position)!!
                    if (!videoDoalodViewLeft.itemView.videoProgress.isVisible) {
                        videoDoalodViewLeft.itemView.videoProgress.visibility = View.VISIBLE
                        videoDoalodViewLeft.itemView.ivChatVideoLeft.visibility = View.GONE

                        mViewModel.saveVideoInSDCard(data.media)
                    }

                }
            }
            R.id.ivChatVideoRight, R.id.clChatVideoRight -> {
                isSelectLeft=false
                if (AppUtils.fileIsSaveOrNot(data!!.media.fileName).isNotEmpty()) {
                    startActivity(
                        PlayVideoActivity.newInstance(
                            this,
                            AppUtils.fileIsSaveOrNot(data!!.media.fileName)
                        )
                    )
                } else {
                    videoDoalodViewRight =
                        binding.rvChat.findViewHolderForLayoutPosition(position)!!
                    if (!videoDoalodViewRight.itemView.videoProgress.isVisible) {
                        videoDoalodViewRight.itemView.videoProgress.visibility = View.VISIBLE
                        videoDoalodViewRight.itemView.ivChatVideoRight.visibility = View.GONE

                        mViewModel.saveVideoInSDCard(data.media)
                    }

                }
            }

            R.id.ivChatImage, R.id.clChatImage -> {
                if (AppUtils.fileIsSaveOrNot(data!!.media.fileName).isNotEmpty()) {
                    startActivity(
                        ImageDialogActivity.newInstant(
                            this,
                            AppUtils.fileIsSaveOrNot(data.media.fileName)
                        )
                    )
                }
            }
        }
    }

    override fun onLeftIconClicked() {
        AppUtils.hideKeyboard(this)
        onBackPressed()
    }

    private val mViewModel: ChatViewModel by lazy {
        ViewModelProviders.of(this).get(ChatViewModel::class.java)
    }

    override fun getViewModel(): ChatViewModel {
        return mViewModel
    }

    private val chatUserName: String by lazy {
        intent.getStringExtra(AppConstants.CHAT_USER_NAME)
    }

    private val uniqueName: String by lazy {
        intent.getStringExtra(AppConstants.CHAT_UNIQUE_NAME)
    }

    private val chatUserId: String by lazy {
        intent.getStringExtra(AppConstants.CHAT_USER_ID)
    }

    private val senderName: String by lazy {
        intent.getStringExtra(AppConstants.CHAT_SENDER_NAME)
    }

    private val receiverImage: String by lazy {
        intent.getStringExtra(AppConstants.CHAT_RECEIVER_IMAGE)
    }
    private val receiverId: Int by lazy {
        intent.getIntExtra(AppConstants.RECEVER_USER_ID,0)
    }

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: MessageListAdapter
    private val layoutManager = LinearLayoutManager(this)
    private lateinit var mImageSelectionDialog: BottomSheetDialog
    private lateinit var prefs: Prefs

    private var filePickUtils: FilePickUtils? = null
    private lateinit var videoFileUri: Uri
    private lateinit var videoFileName: String

    private lateinit var videoDoalodViewLeft: RecyclerView.ViewHolder
    private lateinit var videoDoalodViewRight: RecyclerView.ViewHolder

    private  var isSelectLeft:Boolean=false
    private var isSelectRigth: Boolean = false
    private var isImgDowload = false
    private var isStart = false

    private var canLoadMore = true
    private var isLoading = false
    private var messageCount = 20
    private var messageLastIndex: Long = 0
    private var voiceAccessToken=""
    private var registrationListener = registrationListener()

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

            }
        }
    }

    /**
     * File chooser Listener
     */
    private val mOnFileChoose = object : FilePickUtils.OnFileChoose {
        override fun onFileChoose(fileUri: String, requestCode: Int) {
            var fileName = fileUri.substring(fileUri.lastIndexOf("/") + 1)
            mViewModel.sendImageMessage(fileUri, fileName)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)


        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        binding.chatActivity = this
        prefs = Prefs.getInstance(this)!!

        init()
    }

    companion object {
        fun newInstance(
            context: Context,
            chatuserName: String,
            uniqueName: String,
            chatUserId: String,
            senderName: String,
            receiverImg: String,
            receiverId: Int
        ): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(AppConstants.CHAT_USER_NAME, chatuserName)
            intent.putExtra(AppConstants.CHAT_UNIQUE_NAME, uniqueName)
            intent.putExtra(AppConstants.CHAT_USER_ID, chatUserId)
            intent.putExtra(AppConstants.CHAT_SENDER_NAME, senderName)
            intent.putExtra(AppConstants.CHAT_RECEIVER_IMAGE, receiverImg)
            intent.putExtra(AppConstants.RECEVER_USER_ID, receiverId)
            return intent
        }
    }

    private fun init() {
        setChatAdapter()
        setObserver()
        setSendButtonWatcher()
        setPagination()

        mViewModel.createVoiceAccessToken(
            this,
            prefs.userDataModel!!.logindata.uFirstName + " " + prefs.userDataModel!!.logindata.uLastName,
            chatUserName
        )
        if (uniqueName.isNotEmpty()) {
            binding.rvChat.visibility = View.VISIBLE
            binding.clSayHi.visibility = View.GONE
            binding.clChatView.visibility = View.VISIBLE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isStart = true
                checkAndRequestPermissions()
            } else {
                isStart = false
                mViewModel.createChatClient(
                    this,
                    prefs.twilioAccessToken!!,
                    uniqueName,
                    prefs.firebaseToken!!, "enviroclean" + prefs.userDataModel!!.logindata.uId
                )
            }

        } else {
            binding.btnSayHi.text = "Say hi to $chatUserName in order to start conversation"
            binding.rvChat.visibility = View.GONE
            binding.clSayHi.visibility = View.VISIBLE
            binding.clChatView.visibility = View.GONE
            Glide.with(this)
                .asGif()
                .load(R.drawable.say_hi)
                .fitCenter()
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(binding.ivSayHi)
        }

        initToolBar()
        filePickUtils = FilePickUtils(this, mOnFileChoose)
        FirebaseMessagingService.chatUserChannelId = uniqueName
        FirebaseMessagingService.isOnChatScreen = true
    }

    private fun initToolBar() {
        setToolbarTitle(chatUserName)
        setToolbarLeftIcon(R.drawable.ic_back_white, this)
        setToolbarBackground(R.color.blue_toolbar)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        AppUtils.hideKeyboard(this)
        FirebaseMessagingService.isOnChatScreen = false
        FirebaseMessagingService.chatUserChannelId = ""
        finish()
    }

    fun sendTextMessage() {
        mViewModel.sendTextMessage(binding.edtMsg.text.toString())
        binding.edtMsg.setText("")
    }

    fun createChannel() {
        mViewModel.createChannel(chatUserId)
    }

    private fun setChatAdapter() {
        binding.rvChat.layoutManager = layoutManager
        chatAdapter = MessageListAdapter(
            this,
            senderName,
            receiverImage,
            prefs.userDataModel!!.logindata.uImage
        )
        layoutManager.stackFromEnd = true

        binding.rvChat.adapter = chatAdapter
        binding.rvChat.setHasFixedSize(true)
        chatAdapter.itemClickListener = this

    }

    /* Load More Message Recyclerview Pagination */
    private fun setPagination() {
        binding.rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val dataSize = chatAdapter.itemCount
                //Log.e("DATA", "" + dataSize)
                if ((binding.rvChat.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() == 0) {
                    // Log.e("Load", "" + dataSize)
                    if (canLoadMore && !isLoading) {
                        Log.e("DATA", "" + dataSize)
                        if (chatAdapter.itemCount % 20 == 0) {
                            messageCount = messageCount.plus(20)
                            mViewModel.loadMoreMessage(messageLastIndex, messageCount)
                        }
                    }
                }
            }
        })

    }
    private fun setSendButtonWatcher() {
        binding.edtMsg.addTextChangedListener(object :
            TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.isEmpty()) {
                    binding.ivSend.visibility = View.GONE
                } else {
                    mViewModel.setTyping()
                    binding.ivSend.visibility = View.VISIBLE
                }
            }

        })
    }


    private fun setObserver() {
        /* Create Channel Response */
        mViewModel.getCreateChannelResponse().observe({ this.lifecycle }, {
            binding.clSayHi.visibility = View.GONE
            binding.rvChat.visibility=View.VISIBLE
            binding.clChatView.visibility = View.VISIBLE

            Handler().postDelayed({
                mViewModel.createChatClient(
                    this,
                    prefs.twilioAccessToken!!,
                    it,
                    prefs.firebaseToken!!
                    , "enviroclean" + prefs.userDataModel!!.logindata.uId
                )
            }, 1000)

        })
        /* Get Channel Response */
        mViewModel.getChannelResponse().observe({ this.lifecycle }, { response: Boolean? ->
            if (response!!) {
                AppUtils.showPopup(this, "Something Wrong....Please try again.")
            }
        })

        /* Get Message List Response */
        mViewModel.getMessageList()
            .observe({ this.lifecycle }, { messageListResponse: List<Message>? ->
                if (messageListResponse!!.isNotEmpty()) {
                    chatAdapter.setItem(messageListResponse as ArrayList<Message?>)
                    chatAdapter.notifyItemRangeInserted(0,messageListResponse.size-1)
                    //chatAdapter.notifyDataSetChanged()
                    binding.rvChat.smoothScrollToPosition(messageListResponse.size)
                }
            })

        /* Get Message Add Response */
        mViewModel.getMessageAdd().observe({ this.lifecycle }, { message: Message? ->

            var arr = chatAdapter.items
            arr.add(message)

            chatAdapter.setItem(arr)

            //chatAdapter.notifyItemInserted(chatAdapter.itemCount)
            val lastAdapterItem = chatAdapter.itemCount - 1
            binding.rvChat.post {
                var recyclerViewPositionOffset = -1000000
                val bottomView = layoutManager.findViewByPosition(lastAdapterItem)
                if (bottomView != null) {
                    recyclerViewPositionOffset = 0 - bottomView.height
                }
                layoutManager.scrollToPositionWithOffset(
                    lastAdapterItem,
                    recyclerViewPositionOffset
                )
            }
        })

        /* Send Image Response*/
        mViewModel.getSendImageResponse().observe({ this.lifecycle }, { isSave: Boolean? ->
            if (isSave!!) {
                chatAdapter.notifyDataSetChanged()
                if (isSelectLeft) {
                    videoDoalodViewLeft.itemView.imgProgressLeft.visibility = View.GONE
                }
                if (isSelectRigth) {
                    videoDoalodViewRight.itemView.imgProgressRight.visibility = View.GONE
                }
            }
        })

        /* Video Download Response */
        mViewModel.getVideoDownloadResponse().observe({ this.lifecycle }, { response: Boolean? ->
            if (isSelectLeft){
                videoDoalodViewLeft.itemView.videoProgress.visibility = View.GONE
                videoDoalodViewLeft.itemView.ivChatVideoLeft.visibility = View.VISIBLE
            }else{
                videoDoalodViewRight.itemView.videoProgress.visibility = View.GONE
                videoDoalodViewRight.itemView.ivChatVideoRight.visibility = View.VISIBLE
            }
        })

        /* Typing indicator */
        mViewModel.getIsTyping().observe({ this.lifecycle }, { isTyping: Boolean? ->
            if (isTyping!!) {
                binding.tvIsTyping.visibility = View.VISIBLE
                binding.tvIsTyping.text = chatUserName + " is typing..."
            } else {
                binding.tvIsTyping.visibility = View.GONE
            }
        })

        /* Load More Message observer */
        mViewModel.getMessageMoreList()
            .observe({ this.lifecycle }, { messageList: List<Message>? ->
                messageLastIndex = messageList!![0].messageIndex
                Log.e("MESSAGE INDEX", "" + messageLastIndex)

                chatAdapter.setNewItem(messageList as ArrayList<Message?>)
                //adapter.notifyDataSetChanged()
                chatAdapter.notifyItemRangeInserted(0, chatAdapter.itemCount - 20)


                if (messageLastIndex.toInt() == 0) {
                    isLoading = false
                    canLoadMore = false
                }
            })


        mViewModel.getVoiceAccessToken().observe({ this.lifecycle }, { token: String? ->
            voiceAccessToken=token!!
            registerForCallInvites()
        })
    }


    private fun registerForCallInvites() {
        val fcmToken = FirebaseInstanceId.getInstance().token
        if (fcmToken != null) {
            Log.i(TAG, "Registering with FCM")
            Voice.register(
                voiceAccessToken!!,
                Voice.RegistrationChannel.FCM,
                fcmToken,
                registrationListener
            )
        }
    }
    /**
     * Show Image selection Dialog
     */
    fun showImageVideoSelectionDialog() {
        if (checkAndRequestPermissions()) {
            mImageSelectionDialog = BottomSheetDialog(this)
            val dialogImageSelectionBinding =
                DialogImageVideoSelectionBinding.inflate(layoutInflater)
            mImageSelectionDialog.setContentView(dialogImageSelectionBinding.root)
            dialogImageSelectionBinding.tvSelectPhoto.setOnClickListener(this)
            dialogImageSelectionBinding.tvSelectVideo.setOnClickListener(this)
            mImageSelectionDialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == AppConstants.PICK_VIDEO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val file = File(videoFileUri.path!!)
            Log.e("FileURI", "" + file.length())
            var fileSize = file.length()
            Log.e("FileURI", "" + videoFileUri)
            Log.e("FileURI", "" + videoFileUri.path)
            Log.e("FileURI", "" + videoFileName)
            Log.e("SIZE FILE", "" + AppUtils.getSizeLengthFile(file.length()))
            /* 105255200 byte - 105 MB*/
            if (fileSize.toInt() != 0 && fileSize.toInt() < 105255200) {
                mViewModel.sendVideoMessage(videoFileUri.path!!, videoFileName)
            } else {
                AppUtils.showSnackBar(binding.root, "Please capture small video.")
            }

        } else {
            filePickUtils?.onActivityResult(requestCode, resultCode, intent)
        }
    }


    fun startVoice() {
        startActivity(
            VoiceActivity.newInstance(
                this,
                prefs.userDataModel!!.logindata.uFirstName + " " + prefs.userDataModel!!.logindata.uLastName,
                chatUserName,receiverId,false
            )
        )
    }
    private fun checkAndRequestPermissions(): Boolean {
        val writepermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        val listPermissionsNeeded = ArrayList<String>()


        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(), AppConstants.STORAGE_PERMISSION
            )
            return false
        } else {
            if (isStart) {
                isStart = false
                mViewModel.createChatClient(
                    this,
                    prefs.twilioAccessToken!!,
                    uniqueName,
                    prefs.firebaseToken!!
                    , "enviroclean" + prefs.userDataModel!!.logindata.uId
                )
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == AppConstants.STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isImgDowload) {
                    isImgDowload = false

                } else if (isStart) {
                    isStart = false
                    mViewModel.createChatClient(
                        this,
                        prefs.twilioAccessToken!!,
                        uniqueName,
                        prefs.firebaseToken!!
                        , "enviroclean" + prefs.userDataModel!!.logindata.uId
                    )
                } else {
                    showImageVideoSelectionDialog()
                }

            } else {
                Toast.makeText(
                    this,
                    "Permission Denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    /** Create a file Uri for saving an image or video */
    private fun getOutputMediaFileUri(type: Int): Uri {
        return Uri.fromFile(getOutputMediaFile(type))
    }

    /** Create a File for saving an image or video */
    private fun getOutputMediaFile(type: Int): File? {
        val fullPath =
            Environment.getExternalStorageDirectory().absolutePath + "/EnviroClean"
        // Check that the SDCard is mounted
        val mediaStorageDir = File(fullPath)
        // Create the storage directory(MyCameraVideo) if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraVideo", "Failed to create directory MyCameraVideo.")
                return null
            }
        }
        // Create a media file name

        // For unique file name appending current timeStamp with file name
        val date = java.util.Date()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss")
            .format(date.getTime())
        val mediaFile: File
        if (type == MEDIA_TYPE_VIDEO) {
            // For unique video file name appending current timeStamp with file name
            mediaFile = File(
                (mediaStorageDir.path + File.separator +
                        "ENVID_" + timeStamp + ".mp4")
            )
            videoFileName = mediaFile.name
        } else {
            return null
        }
        return mediaFile
    }

}
