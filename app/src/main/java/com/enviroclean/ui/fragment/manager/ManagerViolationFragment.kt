package com.enviroclean.ui.fragment.manager

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enviroclean.R
import com.enviroclean.adapter.ViolationSubmitAdapter
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseFragment
import com.enviroclean.databinding.FragmentManagerViolationBinding
import com.enviroclean.model.UploadImagesModel
import com.enviroclean.model.ViolationResponse
import com.enviroclean.ui.activity.ManagerHomeActivity
import com.enviroclean.ui.activity.ValetHomeActivity
import com.enviroclean.ui.fragment.valet.ValetWorkCheckingFragment
import com.enviroclean.utils.*
import com.enviroclean.utils.filePick.FilePickUtils
import com.enviroclean.viewmodel.ManagerViolationViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import java.io.File

/**
 * Created by imobdev on 20/12/19
 */
class ManagerViolationFragment : BaseFragment<ManagerViolationViewModel>(), View.OnClickListener,
    BaseBindingAdapter.ItemClickListener<UploadImagesModel?>,
    MyLocationProvider.MyLocationListener {
    override fun onLocationReceived(location: Location?) {
        location?.let {
            Log.e("LATITUDE", "" + it.latitude)
            Log.e("LONGITUDE", "" + it.longitude)
            mLatitude = it.latitude.toString()
            mLongitude = it.longitude.toString()
            locationProvider?.stopLocationUpdates()

            // When we need to get location again, then call below line
            //locationProvider?.startGettingLocations()

        }
    }

    var mLongitude: String = ""
    var mLatitude: String = ""
    override fun onStop() {
        super.onStop()
        // locationProvider?.onStop()
    }

    override fun onItemClick(view: View, data: UploadImagesModel?, position: Int) {
        when (view.id) {
            R.id.ivDelete -> {
                (binding.rvViolationImages.adapter as ViolationSubmitAdapter).removeItem(position)
                imagesList.removeAt(position-1)
                (binding.rvViolationImages.adapter as ViolationSubmitAdapter).notifyDataSetChanged()
            }
           else->{
                if ((binding.rvViolationImages.adapter as ViolationSubmitAdapter).items.size <= 3) {
                    filePickUtils!!.requestImageCamera(101, false, false)
                } else {
                    AppUtils.showSnackBar(binding.btnSubmit, "Upload image between 1 to 3")
                }
            }
        }
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.ivAddDustBin -> {
                    if ((binding.rvViolationImages.adapter as ViolationSubmitAdapter).items.size <= 2) {
                        filePickUtils!!.requestImageCamera(101, false, false)
                    } else {
                        AppUtils.showSnackBar(binding.btnSubmit, "Upload image between 1 to 3")
                    }
                }
                R.id.btnSubmit -> {

                    /*manager login*/
                    if (prefs.checkingResponse!!.result!!.checkinType == 1) {
                        if (violationID.isEmpty()) {
                            AppUtils.showSnackBar(binding.btnSubmit, "Your not under the radius")
                            return
                        }
                    }

                    if (binding.tvSelectViolation.text == getString(R.string.lbl_select_violation)) {
                        AppUtils.showSnackBar(
                            binding.btnSubmit,
                            getString(R.string.lbl_select_violation)
                        )
                        return
                    }
                    if (binding.etUnitNo.text!!.isEmpty()) {
                        AppUtils.showSnackBar(
                            binding.btnSubmit,
                            getString(R.string.lbl_eneter_unit_no)
                        )
                        return
                    }
                    if(binding.clOther.visibility ==View.VISIBLE){
                        if(binding.etOther.text!!.isEmpty()){
                            AppUtils.showSnackBar(binding.btnSubmit, "Enter other reason")
                            return
                        }
                    }
                    if (convertZip()) {
                        if ((binding.rvViolationImages.adapter as ViolationSubmitAdapter).items.size == 1) {
                            AppUtils.showSnackBar(binding.btnSubmit, "Selected at list 1 image")
                            return
                        }
                        mViewModel.imageZipPath = mImagesZip
                        val param: HashMap<String, String?> = HashMap()
                        param[ApiParams.VIO_COMM_ID] =
                            prefs.checkingResponse!!.result!!.commId.toString()
                        param[ApiParams.VIO_SCH_ID] =
                            prefs.checkingResponse!!.result!!.commSchId.toString()
                        param[ApiParams.VIO_AREA_ID] = violationID
                        param[ApiParams.VIO_LATITUDE] = mLatitude
                        param[ApiParams.VIO_LONGITUDE] = mLongitude
                        //         param[ApiParams.VIO_NAME] = ""
                        param[ApiParams.VIO_DESC] = binding.tvDescription.text.toString()
                        //       param[ApiParams.VIO_QR_CODE] = ""
                        param[ApiParams.VIO_UNIT_NUMBER] = binding.etUnitNo.text.toString()
                        param[ApiParams.VIO_REASON] = violationReasonId
                        if(binding.etOther.text.toString().isNotEmpty()){
                            param[ApiParams.VIO_OTHER_REASON] = binding.etOther.text.toString()
                        }else{
                            param[ApiParams.VIO_OTHER_REASON] = ""
                        }
                        if (prefs.checkingResponse!!.result!!.checkinType == 2) {
                            param[ApiParams.TYPE] = "2"
                            param[ApiParams.WEEK_NO] =
                                prefs.checkingResponse!!.result!!.weekno.toString()
                        } else {
                            param[ApiParams.TYPE] = "1"
                            param[ApiParams.WEEK_NO] = ""
                        }
                        param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE

                        mViewModel.callApi(param)
                        AppUtils.loge(param.toString())
                    }
                }
            }
        }
    }

    private fun convertZip(): Boolean {
        val tsLong = System.currentTimeMillis()
        val ts = tsLong.toString()
        val direct = File(AppConstants.Images_PATH + ts)

        if (!direct.exists()) {
            if (direct.mkdir()); //directory is created;
        }
        ImagesConvertZipFile.copyOrMoveFile(direct, imagesList)
        ImagesConvertZipFile.zipFileAtPath(direct.toString(), direct.toString() + ".zip")
        mImagesZip = direct.toString() + ".zip"
        return true
    }

    lateinit var mImagesZip: String
    lateinit var prefs: Prefs
    var locationProvider: MyLocationProvider? = null
    private lateinit var binding: FragmentManagerViolationBinding
    var imagesList: ArrayList<String> = ArrayList()
    private val mViewModel: ManagerViolationViewModel by lazy {
        ViewModelProviders.of(this).get(ManagerViolationViewModel::class.java)
    }

    override fun getViewModel(): ManagerViolationViewModel {
        return mViewModel
    }

    companion object {
        fun newInstance(): ManagerViolationFragment {
            val bundle = Bundle()
            val fragment = ManagerViolationFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private var filePickUtils: FilePickUtils? = null
    var violationID: String = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentManagerViolationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs.getInstance(context!!)!!
        violationsList()
        setToolbar()
        init()
        getLocation()
        scollingEditText()

        if (violationID.isEmpty()) {
            setViolationArea()
            binding.tvSelectArea.visibility = View.VISIBLE
            binding.view5.visibility = View.VISIBLE
        } else {
            binding.tvSelectArea.visibility = View.GONE
            binding.view5.visibility = View.GONE
        }
    }

    var areaListName: ArrayList<String> = ArrayList()
    var areaListId: ArrayList<String> = ArrayList()
    private fun setViolationArea() {
        areaListId.clear()
        areaListName.clear()
        for (i in 0 until prefs.checkingResponse!!.result!!.comm_areas.size) {
            areaListId.add(prefs.checkingResponse!!.result!!.comm_areas[i]!!.area_id.toString())
            areaListName.add(prefs.checkingResponse!!.result!!.comm_areas[i]!!.area_name)
        }
        val selectViolationsPopupWindow = ListPopupWindow(context!!)
        selectViolationsPopupWindow.setAdapter(
            ArrayAdapter(
                context!!,
                R.layout.spinner_drop_down_item,
                areaListName
            )
        )
        selectViolationsPopupWindow.anchorView = binding.tvSelectArea
        selectViolationsPopupWindow.isModal = true

        selectViolationsPopupWindow.setOnItemClickListener { _, _, position, _ ->
            val selectedSortBy = areaListName[position]

            violationID = areaListId[position]
            binding.tvSelectArea.text = selectedSortBy
            selectViolationsPopupWindow.dismiss()
        }
        binding.tvSelectArea.setOnClickListener { selectViolationsPopupWindow.show() }

    }

    private fun scollingEditText() {
        binding.tvDescription.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (binding.tvDescription.hasFocus()) {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    when (event.action and MotionEvent.ACTION_MASK) {
                        MotionEvent.ACTION_SCROLL -> {

                            v.parent.requestDisallowInterceptTouchEvent(false)
                            return true
                        }
                    }
                }
                return false
            }
        })
    }

    fun getLocation() {
        locationProvider = MyLocationProvider(this, this)
        locationProvider?.init()
    }

    lateinit var qrCode: String
    lateinit var qrName: String
    lateinit var qrCodeId: String


    fun init() {

        mViewModel.getResponse().observe(this, observerViolationResponse)
        binding.ivAddDustBin.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
        val list: ArrayList<UploadImagesModel?> = ArrayList()
        list.add(UploadImagesModel(""))
        context?.let {
            filePickUtils = FilePickUtils(this, mOnFileChoose)
            binding.rvViolationImages.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvViolationImages.isNestedScrollingEnabled=false
            val adapter = ViolationSubmitAdapter()

            adapter.itemClickListener = this
            binding.rvViolationImages.adapter = adapter

            ( binding.rvViolationImages.adapter as ViolationSubmitAdapter).setItem(list)
            ( binding.rvViolationImages.adapter as ViolationSubmitAdapter).notifyDataSetChanged()
        }
        binding.ivAddDustBin.performClick()
        initSpinnerViolationReason()
    }

    private val observerViolationResponse = Observer<ViolationResponse> {
        if (it.status) {
            imagesList.clear()
            val jsonScanQR = JSONObject()
            try {
                if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {
                    /*manager login*/
                    if (ManagerHomeActivity.clickDirectViolationFragment) {
                        prefs.lastScanQrCodeIdMangerValets = ""
                    }
                } else {
                    /*valets login*/
                    if (ValetHomeActivity.clickDirectViolationFragment) {
                        prefs.lastScanQrCodeIdValets = ""
                    }
                }
                jsonScanQR.put("command", "violation")
                jsonScanQR.put("vio_comm_id", prefs.checkingResponse!!.result!!.commId.toString())
                jsonScanQR.put("vio_sch_id", prefs.checkingResponse!!.result!!.commSchId.toString())
                jsonScanQR.put("vio_id", it.result.vioId.toString())
                jsonScanQR.put("userId", prefs.userDataModel!!.logindata.uId)
                Log.e("SOCKET_SEND_RESPONSE", "---->" + jsonScanQR.toString())
                AppUtils.webSocket.send(jsonScanQR.toString())
            } catch (e: Exception) {

            }
            if (progressDialog != null) {
                hideProgress()
            }
            (binding.rvViolationImages.adapter as ViolationSubmitAdapter).clear()
            binding.tvDescription.setText("")
            binding.etUnitNo.setText("")
            binding.etOther.setText("")
            val ft = fragmentManager!!.beginTransaction();
            ft.detach(this)
            ft.attach(this)
            ft.commit()

        } else {
            if (progressDialog != null) {
                hideProgress()
            }
            if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {
                if (ManagerHomeActivity.clickDirectViolationFragment) {
                    prefs.lastScanQrCodeIdMangerValets = ""
                }
                ManagerHomeActivity.currentFragment = 1
                AppUtils.showToast(context!!, it.message)
                startActivity(ManagerHomeActivity.newInstance(context!!, true))
                AppUtils.startFromRightToLeft(context!!)
            } else {
                if (ValetHomeActivity.clickDirectViolationFragment) {
                    prefs.lastScanQrCodeIdValets = ""
                }
                ValetHomeActivity.currentFragment = 1
                AppUtils.showToast(context!!, it.message)
                startActivity(ValetHomeActivity.newInstance(context!!, true))
                AppUtils.startFromRightToLeft(context!!)

            }
        }

    }

    private fun setToolbar() {
        binding.toolbar.tvToolbarTitle.text = getString(R.string.lbl_violation)
    }

    var mFlag: Boolean = false

    private val mOnFileChoose = object : FilePickUtils.OnFileChoose {
        override fun onFileChoose(fileUri: String, requestCode: Int) {
            Log.e("TAG", "------>" + fileUri)

                imagesList.add(fileUri)
                if (mFlag) {
                    mFlag = false
                    val list: ArrayList<UploadImagesModel?> = ArrayList()
                    list.add(UploadImagesModel(fileUri))
                    (binding.rvViolationImages.adapter as ViolationSubmitAdapter).setItem(list)
                } else {
                    (binding.rvViolationImages.adapter as ViolationSubmitAdapter).items.add(1, UploadImagesModel(fileUri))
                }
                (binding.rvViolationImages.adapter as ViolationSubmitAdapter).notifyDataSetChanged()


        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        filePickUtils?.onRequestPermissionsResult(
            requestCode,
            permissions as Array<String>,
            grantResults
        )
        if (requestCode == MyLocationProvider.LOCATION_PERMISSION_REQUEST_CODE) {
            locationProvider?.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {
                    //   ManagerHomeActivity().changeFragment(0)
                    startActivity(ManagerHomeActivity.newInstance(context!!))
                    AppUtils.startFromRightToLeft(context!!)

                } else {
                    val navView =
                        (context as ValetHomeActivity).findViewById<BottomNavigationView>(R.id.nav_view)
                    navView.menu.findItem(R.id.nav_violate_report).isChecked = true
                    replaceFragment(R.id.frame_container, ValetWorkCheckingFragment(), false)
                }

            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            filePickUtils?.onActivityResult(requestCode, resultCode, intent)
        }
        if (requestCode == MyLocationProvider.REQUEST_LOCATION_SETTINGS) {
            locationProvider?.onActivityResult(requestCode, resultCode, intent)
        }
    }

    var valetsViolationList: ArrayList<String> = ArrayList()
    var valetsViolationId: ArrayList<String> = ArrayList()
    private fun violationsList() {
        valetsViolationList.clear()
        valetsViolationId.clear()
        if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {

            for (i in 0 until prefs.userDataModel!!.managerReason.size) {
                valetsViolationList.add(prefs.userDataModel!!.managerReason[i].reasonName)
                valetsViolationId.add(prefs.userDataModel!!.managerReason[i].reasonId)
            }
            violationID = prefs.violationAreaId!!
        } else {

            for (i in 0 until prefs.userDataModel!!.valetReason.size) {
                valetsViolationList.add(prefs.userDataModel!!.valetReason[i].reasonName)
                valetsViolationId.add(prefs.userDataModel!!.valetReason[i].reasonId)
            }
            violationID = prefs.violationAreaId!!
        }
    }

    @Subscribe
    fun onEvent(event: String) {
        if (event.equals("refresh", true)) {
            Log.e("RESHRESH_EVENT", "----->" + event)
            if (progressDialog != null) {
                hideProgress()
            }
            if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {
                if (ManagerHomeActivity.clickDirectViolationFragment) {
                    prefs.lastScanQrCodeIdMangerValets = ""
                }
                startActivity(ManagerHomeActivity.newInstance(context!!, true))
                AppUtils.startFromRightToLeft(context!!)

            } else {
                if (ValetHomeActivity.clickDirectViolationFragment) {
                    prefs.lastScanQrCodeIdValets = ""
                }
                startActivity(ValetHomeActivity.newInstance(context!!, true))
                AppUtils.startFromRightToLeft(context!!)

            }
        }

    }

    lateinit var violationReasonId: String
    private fun initSpinnerViolationReason() {

        val selectViolationsPopupWindow = ListPopupWindow(context!!)
        selectViolationsPopupWindow.setAdapter(
            ArrayAdapter(
                context!!,
                R.layout.spinner_drop_down_item,
                valetsViolationList
            )
        )
        selectViolationsPopupWindow.anchorView = binding.tvSelectViolation
        selectViolationsPopupWindow.isModal = true

        selectViolationsPopupWindow.setOnItemClickListener { _, _, position, _ ->
            val selectedSortBy = valetsViolationList[position]
            violationReasonId = valetsViolationId[position]
            if (selectedSortBy == "Other") {
                binding.clOther.visibility = View.VISIBLE
            } else {
                binding.clOther.visibility = View.GONE
            }

            binding.tvSelectViolation.text = selectedSortBy
            selectViolationsPopupWindow.dismiss()
        }
        binding.tvSelectViolation.setOnClickListener { selectViolationsPopupWindow.show() }


    }


}