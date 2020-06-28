package com.enviroclean.ui.fragment.manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.enviroclean.R
import com.enviroclean.adapter.DustBinListAdapter
import com.enviroclean.adapter.ValetsWorkCheckIngUserListAdapter
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseFragment
import com.enviroclean.databinding.FragmentManagerValetWorkCheckingBinding
import com.enviroclean.iterfacea.CheckInValetsClick
import com.enviroclean.iterfacea.MessageInterface
import com.enviroclean.iterfacea.ReconnectSocketInterface
import com.enviroclean.model.AreaDataModel
import com.enviroclean.model.CheckIngResponse1
import com.enviroclean.model.Reminder
import com.enviroclean.ui.activity.*
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.ManagerVelatesWorkCheckingViewModel
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by imobdev on 19/12/19
 */
class ManagerValetWorkWithoutCheckingFragment : BaseFragment<ManagerVelatesWorkCheckingViewModel>(),
    View.OnClickListener, MessageInterface, ReconnectSocketInterface,
    BaseBindingAdapter.ItemClickListener<AreaDataModel>, CheckInValetsClick {
    override fun setValetClick(position: Int, item: CheckIngResponse1.Result.CommCheckinValet) {
        startActivity(
            ChatActivity.newInstance(
                context!!,
                item.uFirstName + " " + item.uLastName
                ,
                item.u_channel_id,
                item.uId.toString(),
                "enviroclean-" + item.uId,
                item.uImage,
                item.uId
            )
        )
    }

    override fun onItemClick(view: View, data: AreaDataModel?, position: Int) {

    }

    override fun reconnected() {

    }

    override fun massagesRecived(s: JSONObject?) {
        Log.e("SOCKET_CODE", "--->" + s!!.getInt("code"))
        EventBus.getDefault().post(s)
        updateQRCodeStatus(s)
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {

                R.id.ivCheckOut -> {


                }
                R.id.tvViolationCount -> {
                        if (responseCheckIn.result!!.commVioCount != 0) {
                            startActivity(
                                ViolationReportListActivity.newInstance(
                                    context!!, responseCheckIn.result!!.commId.toString(),
                                    ManagerValetWorkFragment.SELECTED_DATE
                                )
                            )
                            AppUtils.startFromRightToLeft(context!!)
                        }

                }

                R.id.ivMap -> {
                    val instance = Calendar.getInstance()
                    val mCurrentDate =
                        (instance.get(Calendar.DATE).toString() + "-" + (instance.get(Calendar.MONTH) + 1) + "-" + instance.get(
                            Calendar.YEAR
                        ).toString())
                    startActivity(
                        LiveTrackingActivity.newInstance(
                            context!!,
                            responseCheckIn.result!!.commId.toString(),
                            responseCheckIn.result!!.commSchId.toString(),
                            mCurrentDate
                        )
                    )
                    AppUtils.startFromRightToLeft(context!!)
                }

            }
        }

    }

    lateinit var responseCheckIn: CheckIngResponse1

    internal lateinit var mLocationRequest: LocationRequest
    lateinit var prefs: Prefs

    private lateinit var binding: FragmentManagerValetWorkCheckingBinding

    private val mViewModel: ManagerVelatesWorkCheckingViewModel by lazy {
        ViewModelProviders.of(this).get(ManagerVelatesWorkCheckingViewModel::class.java)
    }

    override fun getViewModel(): ManagerVelatesWorkCheckingViewModel {
        return mViewModel
    }

    var qrCodesList: ArrayList<CheckIngResponse1.Result.CommQrcode?> = ArrayList()
    lateinit var checkinResponse: CheckIngResponse1.Result

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentManagerValetWorkCheckingBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        fun newInstance(): ManagerValetWorkWithoutCheckingFragment {
            val bundle = Bundle()
            val fragment = ManagerValetWorkWithoutCheckingFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs.getInstance(context!!)!!

        mViewModel.getResponse().observe(this, observeCummintyList)
        binding.ivMap.visibility = View.VISIBLE
        binding.ivCheckOut.visibility = View.GONE
        binding.clTime.visibility = View.GONE
        callApi()
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        val mSelectedDate = sdf.parse(ManagerValetWorkFragment.SELECTED_DATE)
        val mCurrentDate = sdf.parse(ManagerValetWorkFragment.currentDate)

        if (mSelectedDate.before(mCurrentDate) || mSelectedDate == mCurrentDate) {
            binding.ivMap.visibility = View.VISIBLE
        } else {
            binding.ivMap.visibility = View.GONE
        }


    }

    override fun onResume() {
        super.onResume()
        setNotificationCount(binding.toolbar, prefs.notificationCount!!)

    }

    override fun onDestroy() {
        super.onDestroy()
        ManagerHomeActivity.currentFragment=1

    }

    fun init() {
        binding.clCommunityHeader1.visibility = View.VISIBLE
        mLocationRequest = LocationRequest()
        checkinResponse = responseCheckIn.result!!
        binding.ivCheckOut.setOnClickListener(this)
        binding.tvViolationCount.setOnClickListener(this)
        binding.ivMap.setOnClickListener(this)
        setToolbar()
        setHeaderData()
        setWorkAdapter()

        if (!ManagerHomeActivity.socketConnectedManager) {
            socketRegister()
        }
        fabFilter()
    }

    private fun setHeaderData() {


        binding.tvCommunityName.text = responseCheckIn.result!!.commName
        binding.tvViolationCount.text = "" + responseCheckIn.result!!.commVioCount
        binding.tvCommunityCount.text = "" + responseCheckIn.result!!.commRemainCount
        SplashActivity.REMANING_COUNT = responseCheckIn.result!!.commRemainCount.toString()
    }

    private fun setToolbar() {
        Log.e("TOOLBAR", "-->")
        binding.toolbar.tvToolbarTitle.text = getString(R.string.lbl_valets_work)
        setToolbarLeftIcon(binding.toolbar, R.drawable.ic_back_white, object :
            BaseActivity.ToolbarLeftMenuClickListener {
            override fun onLeftIconClicked() {
                handler.removeCallbacks(checkSocketConnection)
                handler.removeCallbacksAndMessages(null)
                onBackPressed()
            }

        })
        setNotificationCount(binding.toolbar, prefs.notificationCount!!)
        setToolbarRight1MenuIcon(binding.toolbar, R.drawable.ic_notification_white, object :
            BaseActivity.ToolbarRight1MenuClickListener {
            override fun onRight1MenuClicked() {
                context?.let {
                    startActivity(NotificationListActivity.newInstance(context!!))

                }
            }
        })
        /*on backpress key fragment*/
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener { _, _, _ ->
            AppUtils.webSocket.cancel()
            AppUtils.client!!.dispatcher.executorService.shutdown()
            ManagerHomeActivity.socketConnectedManager = false
            false
        }

    }


    @Subscribe
    fun onEvent(isConnected: String) {
        if (isConnected == AppConstants.IS_CONNECTED) {
            if (!ManagerHomeActivity.socketConnectedManager) {
                if (prefs.isCheck) {
                    AppUtils.loge("CONNECTED DATA")
                    socketRegister()
                }
            }
        }
        if (isConnected == AppConstants.NOTIFICATION_COUNT) {
            setNotificationCount(binding.toolbar, prefs.notificationCount!!)
        }
    }

    private fun socketRegister() {
        if (context == null) {
            handler.removeCallbacks(checkSocketConnection)
            handler.removeCallbacksAndMessages(null)
            return
        }
        AppUtils.startSocketConnection(
            "subscribe",
            checkinResponse.commId.toString(),
            context!!,
            this@ManagerValetWorkWithoutCheckingFragment,
            this@ManagerValetWorkWithoutCheckingFragment
        )

        val jsonObject = JSONObject()
        try {
            jsonObject.put("command", "register")
            jsonObject.put("userId", prefs.userDataModel!!.logindata.uId)
            AppUtils.loge(jsonObject.toString(), "SOCKET_REGISTER")
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

    private val checkSocketConnection = object : Runnable {
        public override fun run() {
            if (!ManagerHomeActivity.socketConnectedManager) {
                socketRegister()
            }
            handler.postDelayed(this, 5000)
        }
    }


    private val observeCummintyList = Observer<CheckIngResponse1> {
        responseCheckIn = it
        init()
        directViewValtes = true


    }
    private fun fabFilter() {
        binding.fbButton.setMenuListener(object : SimpleMenuListenerAdapter() {
            override fun onMenuItemSelected(menuItem: MenuItem?): Boolean {
                when (menuItem!!.title) {
                    "All" -> {
                        Log.e("TAG", "action_all")
                        ManagerHomeActivity.FILTER_KEY = AppConstants.FILTER_ALL
                        filterApply()

                    }
                    "Pending" -> {
                        ManagerHomeActivity.FILTER_KEY = AppConstants.FILTER_PENDDING
                        Log.e("TAG", "action_pending")
                        filterApply()

                    }
                    "Scanned" -> {
                        ManagerHomeActivity.FILTER_KEY = AppConstants.FILTER_SCAN
                        Log.e("TAG", "action_scan")
                        filterApply()
                    }
                    "Violation" -> {
                        ManagerHomeActivity.FILTER_KEY = AppConstants.FILTER_VIOLARION
                        Log.e("TAG", "action_violation")
                        filterApply()
                    }
                }

                return false
            }
        })

    }

    val areaList: ArrayList<AreaDataModel?> = ArrayList()
    fun filterApply() {
        areaList.clear()
        if (ManagerHomeActivity.FILTER_KEY == AppConstants.FILTER_ALL) {
            for (i in 0 until qrCodesList.size) {
                if (checkinResponse.commQrcodes[i]!!.qrMandatory == 1) {
                    areaList.add(
                        AreaDataModel(
                            qrCodesList[i]!!.qrType, qrCodesList[i]!!.qrId, qrCodesList[i]!!.qrText
                            , qrCodesList[i]!!.qrCode, qrCodesList[i]!!.username
                        )
                    )
                }
            }
        } else if (ManagerHomeActivity.FILTER_KEY == AppConstants.FILTER_SCAN) {
            for (i in 0 until qrCodesList!!.size) {
                if (checkinResponse.commQrcodes[i]!!.qrMandatory == 1) {

                    if (qrCodesList[i]!!.qrType == 2) {
                        areaList.add(
                            AreaDataModel(
                                qrCodesList[i]!!.qrType,
                                qrCodesList[i]!!.qrId,
                                qrCodesList[i]!!.qrText
                                ,
                                qrCodesList[i]!!.qrCode,
                                qrCodesList[i]!!.username
                            )
                        )
                    }
                }
            }

        } else if (ManagerHomeActivity.FILTER_KEY == AppConstants.FILTER_VIOLARION) {
            for (i in 0 until qrCodesList!!.size) {
                if (checkinResponse.commQrcodes[i]!!.qrMandatory == 1) {
                    if (qrCodesList[i]!!.qrType == 3) {
                        areaList.add(
                            AreaDataModel(
                                qrCodesList[i]!!.qrType,
                                qrCodesList[i]!!.qrId,
                                qrCodesList[i]!!.qrText
                                ,
                                qrCodesList[i]!!.qrCode,
                                qrCodesList[i]!!.username
                            )
                        )
                    }
                }
            }
        } else if (ManagerHomeActivity.FILTER_KEY == AppConstants.FILTER_PENDDING) {
            for (i in 0 until qrCodesList!!.size) {
                if (checkinResponse.commQrcodes[i]!!.qrMandatory == 1) {
                    if (qrCodesList[i]!!.qrType == 1) {
                        areaList.add(
                            AreaDataModel(
                                qrCodesList[i]!!.qrType,
                                qrCodesList[i]!!.qrId,
                                qrCodesList[i]!!.qrText
                                ,
                                qrCodesList[i]!!.qrCode,
                                qrCodesList[i]!!.username
                            )
                        )
                    }
                }
            }

        }
        (binding.rvCommunityBuilding.adapter as DustBinListAdapter).setItem(areaList)
        (binding.rvCommunityBuilding.adapter as DustBinListAdapter).notifyDataSetChanged()

    }


    private fun callApi(progressShow: Boolean = true) {
        val param: HashMap<String, Any?> = HashMap()
        param[ApiParams.TYPE] = AppConstants.WORK_TYPE_DAY
        param[ApiParams.DATE] = ManagerValetWorkFragment.SELECTED_DATE
        param[ApiParams.COMM_ID] = ManagerValetWorkFragment.COMM_ID
        param[ApiParams.SCH_ID] = ManagerValetWorkFragment.SCH_ID
        param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE

        mViewModel.callApi(param, progressShow)

    }


    private fun setWorkAdapter() {

        for (i in 0 until checkinResponse.commQrcodes.size) {
            if (checkinResponse.commQrcodes[i]!!.qrMandatory == 1) {
                areaList.add(
                    AreaDataModel(
                        checkinResponse.commQrcodes[i]!!.qrType,
                        checkinResponse.commQrcodes[i]!!.qrId,
                        checkinResponse.commQrcodes[i]!!.qrText
                        , checkinResponse.commQrcodes[i]!!.qrCode,
                        checkinResponse.commQrcodes[i]!!.username
                    )
                )
            }
        }
        binding.rvUserList.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val adapter = ValetsWorkCheckIngUserListAdapter(this)

        binding.rvUserList.adapter = adapter

        binding.rvCommunityBuilding.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val communityAdapter = DustBinListAdapter()

        binding.rvCommunityBuilding.adapter = communityAdapter

        communityAdapter.itemClickListener = this

        (binding.rvUserList.adapter as ValetsWorkCheckIngUserListAdapter).setItem(
            checkinResponse.commCheckinValets
        )
        (binding.rvUserList.adapter as ValetsWorkCheckIngUserListAdapter).notifyDataSetChanged()
        qrCodesList = checkinResponse.commQrcodes
        (binding.rvCommunityBuilding.adapter as DustBinListAdapter).setItem(areaList)
    }


    
    var directViewValtes: Boolean = false
    private fun updateQRCodeStatus(obj: JSONObject?) {
        try {
            if (obj!!.getInt("code") == 200) {
                val mQrType = obj.getJSONObject("result").getInt("qr_type")

                if (mQrType == 2) {
                    responseCheckIn.result!!.commVioCount =
                        obj.getJSONObject("result").getInt("comm_vio_count")
                    binding.tvViolationCount.text =
                        obj.getJSONObject("result").getInt("comm_vio_count")
                            .toString()
                } else {
                    val mQRId = obj.getJSONObject("result").getInt("qr_id")
                    //   val mAreaId = obj.getJSONObject("result").getInt("area_id")
                    val mQRStatus = obj.getJSONObject("result").getInt("qr_status")
                    val mName = obj.getJSONObject("result").getString("username")
                    val mRemainCount = obj.getJSONObject("result").getInt("comm_remain_count")
                    val mType = obj.getJSONObject("result").getInt("type")
                    
                        SplashActivity.REMANING_COUNT = mRemainCount.toString()

                        responseCheckIn.result!!.commRemainCount = mRemainCount
                        binding.tvCommunityCount.text =
                            responseCheckIn.result!!.commRemainCount.toString()

                        for (i in 0 until responseCheckIn.result!!.commQrcodes.size) {


                            if (responseCheckIn.result!!.commQrcodes[i]!!.qrId == mQRId) {
                                EventBus.getDefault().post(AppConstants.REFRESH_REMAINING_COUNT)
                                responseCheckIn.result!!.commQrcodes[i]!!.qrType = mQRStatus
                                responseCheckIn.result!!.commQrcodes[i]!!.username = mName

                                qrCodesList = responseCheckIn.result?.commQrcodes!!

                                          filterApply()
                                Log.e("REFRESH_MAIN_ADPTER", "----------->" + qrCodesList)

                                break
                            }

                        
                    }
                }

            } else {
                EventBus.getDefault().post(AppConstants.WRONG_QR_CODE)

            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}