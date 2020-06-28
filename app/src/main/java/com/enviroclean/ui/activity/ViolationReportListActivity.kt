package com.enviroclean.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enviroclean.R
import com.enviroclean.adapter.ViolationReportListAdapter
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.customeview.PaginationScrollListenerLinear
import com.enviroclean.databinding.ActivityViolationReportListBinding
import com.enviroclean.model.ViolationList
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.ViolationReportListViewModel
import com.google.android.material.chip.Chip
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ViolationReportListActivity :
    BaseActivity<ViolationReportListViewModel>(LoginActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener,
    BaseBindingAdapter.ItemClickListener<ViolationList.Result>,
    BaseActivity.ToolbarRightMenuClickListener, View.OnClickListener {
    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                R.id.btnSendToClient -> {
                    imagesCounter = 0
                    val size =
                        (binding.rvViolationReport.adapter as ViolationReportListAdapter).items.size
                    val jsonArrayList: JSONArray = JSONArray()
                    val imagesIdList: ArrayList<Int> = ArrayList()


                    for (i in 0 until size) {

                        if ((binding.rvViolationReport.adapter as ViolationReportListAdapter).items[i]!!.isCheck) {
                            val jsonObject: JSONObject = JSONObject()
                            jsonObject.put(
                                "vio_id",
                                (binding.rvViolationReport.adapter as ViolationReportListAdapter).items[i]!!.vioId
                            )

                            val sizeImages =
                                (binding.rvViolationReport.adapter as ViolationReportListAdapter).items[i]!!.vioImages.size
                            for (j in 0 until sizeImages) {

                                if ((binding.rvViolationReport.adapter as ViolationReportListAdapter).items[i]!!.vioImages[j]!!.isCheck) {
                                    imagesIdList.add((binding.rvViolationReport.adapter as ViolationReportListAdapter).items[i]!!.vioImages[j]!!.vioImageId)
                                    imagesCounter += 1

                                }

                            }
                            val imagesIds = imagesIdList.toString()
                            jsonObject.put(
                                "vio_img_ids",
                                imagesIds.substring(1, imagesIds.length - 1).replace(" ", "")
                            )
                            jsonArrayList.put(jsonObject)
                            imagesIdList.clear()
                        }

                    }
                    if (imagesCounter < 1) {
                        AppUtils.showSnackBar(binding.btnSendToClient, "Select at least 1 images")
                        return
                    }
                    AppUtils.loge("" + jsonArrayList)
                    vio_ids = jsonArrayList.toString()
                    AppUtils.loge("" + imagesCounter)
                    showSendToClientDialog()

                }
            }
        }
    }

    lateinit var vio_ids: String
    var imagesCounter: Int = 0
    override fun onRightMenuClicked() {
        startActivityForResult(
            FilterActivity.newInstance(
                this,
                selectedAreaid,
                selectedAreaName,
                mSelectedViolationReason,
                mSelectedViolationReasonName
            ), 1006
        )
        AppUtils.startFromRightToLeft(this)
    }

    override fun onItemClick(view: View, data: ViolationList.Result?, position: Int) {

    }

    lateinit var prefs: Prefs
    override fun onLeftIconClicked() {
        onBackPressed()
    }

    private lateinit var binding: ActivityViolationReportListBinding

    private val mViewModel: ViolationReportListViewModel by lazy {
        ViewModelProviders.of(this).get(ViolationReportListViewModel::class.java)
    }

    override fun getViewModel(): ViolationReportListViewModel {
        return mViewModel
    }

    private val mCommunityId: String by lazy {
        intent.getStringExtra(AppConstants.COMM_ID)
    }
    private val mSelctedDate: String by lazy {
        intent.getStringExtra(AppConstants.SELECTED_DATE)
    }

    /*variable for current page in API pagination*/
    var CURRENT_PAGE: Int = 1
    /*Total pages for API pagination*/
    var TOTAL_PAGE: Int = 1

    /*loading flag*/
    var isLoading: Boolean = true

    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    companion object {
        fun newInstance(
            context: Context,
            communitesId: String,
            selectedDate: String
        ): Intent {
            val intent = Intent(context, ViolationReportListActivity::class.java)
            intent.putExtra(AppConstants.COMM_ID, communitesId)
            intent.putExtra(AppConstants.SELECTED_DATE, selectedDate)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_violation_report_list)
        prefs = Prefs.getInstance(this)!!
        initToolBar()
        inti()
    }

    private fun initToolBar() {
        setToolbarTitle(R.string.lbl_violation_report)
        setToolbarLeftIcon(R.drawable.ic_back_white, this)
        setToolbarBackground(R.color.blue_toolbar)
        setToolbarRightMenuIcon(R.drawable.ic_filter, this)
    }

    private fun inti() {
        mViewModel.getResponse().observe(this, observerViolationList)
        mViewModel.getResponseSendToClient().observe(this, observerViolationSubmit)
        binding.rvViolationReport.layoutManager =
            LinearLayoutManager(this) as RecyclerView.LayoutManager?
        val adapter = ViolationReportListAdapter(binding.rvViolationReport)
        adapter.itemClickListener = this
        binding.rvViolationReport.adapter = adapter
        binding.btnSendToClient.setOnClickListener(this)
        callApi()
        loadMore()
        addChipView()
    }

    val instance = Calendar.getInstance()
    lateinit var currentDate: String
    private fun callApi(progressShow: Boolean = true) {
        currentDate = instance.get(Calendar.DATE).toString() + "-" + (instance.get(
            Calendar.MONTH
        ) + 1) + "-" + (instance.get(Calendar.YEAR))
        val params: HashMap<String, Any?> = HashMap()

        params[ApiParams.COMM_ID] = mCommunityId
        params[ApiParams.DATE] = mSelctedDate
        params[ApiParams.REASON_ID] = mSelectedViolationReason
        if (selectedAreaid.size != 0) {
            val selectedAreaId = selectedAreaid.toString()
            params[ApiParams.AREA_ID] = selectedAreaId.substring(1, selectedAreaId.length - 1)
        } else {
            params[ApiParams.AREA_ID] = ""
        }

        params[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
        mViewModel.callApi(params, progressShow)

    }

    /*A load More method to detect the end of the recyclerview and logic to check if we need to go for load more or not*/
    private fun loadMore() {
        /*recyclerview scroll listener*/
        binding.rvViolationReport.addOnScrollListener(object :
            PaginationScrollListenerLinear(
                binding.rvViolationReport.layoutManager as LinearLayoutManager,
                true
            ) {
            override fun loadMoreItems() {

                /*If we have load all total pages then no need to go for next*/
                if (TOTAL_PAGE != CURRENT_PAGE) {
                    /*checking for loading flag to prevent too many calls at same time*/
                    if (isLoading) {
                        /*switching the value of loading flag*/
                        isLoading = false
                        /*increment the current page count*/
                        CURRENT_PAGE += 1

                        /*Adding a dummy(null) item to the list to show a footer progress bar*/
                        (binding.rvViolationReport.adapter as ViolationReportListAdapter).addItem(
                            null
                        )
                        /*animate the footer view*/
                        val adapterCount =
                            (binding.rvViolationReport.adapter as ViolationReportListAdapter).itemCount
                        (binding.rvViolationReport.adapter as ViolationReportListAdapter).notifyItemInserted(
                            adapterCount - 1
                        )
                        (binding.rvViolationReport.adapter as ViolationReportListAdapter).addLoadingFooter()

                        /*Calling an API for load more*/
                        callApi(false)
                    }
                }
            }

            override fun getTotalPageCount(): Int {
                return TOTAL_PAGE
            }

            override fun isLastPage(): Boolean {
                return false
            }

            override fun isLoading(): Boolean {
                return false
            }

        })

    }


    private val observerViolationList = Observer<ViolationList> {
        if (it.status) {
            (binding.rvViolationReport.adapter as ViolationReportListAdapter).clear()
            (binding.rvViolationReport.adapter as ViolationReportListAdapter).setItem(it.result)
            (binding.rvViolationReport.adapter as ViolationReportListAdapter).notifyDataSetChanged()
            prefs.areaList = it
            binding.btnSendToClient.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE

            if (it.vioClientFlag == 1) {
                binding.btnSendToClient.visibility = View.VISIBLE
            } else {
                binding.btnSendToClient.visibility = View.GONE
            }

        } else {
            Log.e("TESTING", "------>>>>>>")
            binding.btnSendToClient.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            binding.tvNoData.text = it.message
            (binding.rvViolationReport.adapter as ViolationReportListAdapter).clear()
        }
    }

    private val observerViolationSubmit = Observer<BaseResponse> {
        if (it.status) {
            finish()
            startActivity(intent)
            AppUtils.showSnackBar(binding.btnSendToClient, it.message)
        }
    }

    private fun addChipView() {
        /*[start] area chip view*/
        binding.chipGroup.removeAllViews()
        for (i: Int in 0 until selectedAreaid.size) {
            if (selectedAreaid[i].toInt() != 0) {
                val chip = Chip(binding.chipGroup.context)
                chip.text = selectedAreaName[i]
                chip.tag = selectedAreaid[i]
                chip.isCloseIconVisible = true
                chip.isClickable = false
                chip.closeIconTint = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.text_color_chip_state_list_white
                    )
                )

                chip.setTextColor(ContextCompat.getColor(this, R.color.white))
                chip.chipBackgroundColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.background_color_chip_state_list_blue
                    )
                )
                binding.chipGroup.addView(chip)

                chip.setOnCloseIconClickListener {
                    val cId: String = chip.tag as String

                    for (j in 0 until selectedAreaid.size) {
                        if (cId == selectedAreaid[j]) {

                            selectedAreaid.removeAt(j)
                            selectedAreaName.removeAt(j)
                            CURRENT_PAGE = 1
                            callApi()
                            break
                        }
                    }
                    binding.chipGroup.removeView(chip as View)
                }
            }
        }
        /*[end] area chip view*/

        /*[start] violation reason chip view*/
        binding.chipGroupViolationReason.removeAllViews()
        if (mSelectedViolationReason.isEmpty()) {
            return
        }
        if (mSelectedViolationReason.toInt() != 0) {
            val chip = Chip(binding.chipGroup.context)
            chip.text = mSelectedViolationReasonName
            chip.tag = mSelectedViolationReason
            chip.isCloseIconVisible = true
            chip.isClickable = false
            chip.closeIconTint = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this,
                    R.color.text_color_chip_state_list_white
                )
            )

            chip.setTextColor(ContextCompat.getColor(this, R.color.white))
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this,
                    R.color.background_color_chip_state_list_blue
                )
            )
            binding.chipGroupViolationReason.addView(chip)

            chip.setOnCloseIconClickListener {
                val cId: String = chip.tag as String

                mSelectedViolationReason = ""
                mSelectedViolationReasonName = ""
                CURRENT_PAGE = 1
                callApi()

                binding.chipGroupViolationReason.removeView(chip as View)
            }
        }
        /*[end] violation reason chip view*/
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }

    var selectedAreaid: ArrayList<String> = ArrayList()
    var selectedAreaName: ArrayList<String> = ArrayList()
    var mSelectedViolationReason: String = ""
    var mSelectedViolationReasonName: String = ""
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1006) {
            if (resultCode == Activity.RESULT_OK) {
                selectedAreaid = data!!.getStringArrayListExtra("selectedAraListId")
                selectedAreaName = data!!.getStringArrayListExtra("selectedAraListName")
                mSelectedViolationReason = data.getStringExtra("selectedReason")
                mSelectedViolationReasonName = data.getStringExtra("selectedReasonName")
                val testing = selectedAreaid.toString()
                Log.e("SELCTED_LIST_ID", "--->" + testing.substring(1, testing.length - 1))
                Log.e("SELCTED_LIST_NAME", "--->" + selectedAreaName)
                Log.e("SELCTED_LIST_NAME", "--->" + mSelectedViolationReasonName)
                CURRENT_PAGE = 1
                addChipView()
                callApi()
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }


    fun showSendToClientDialog() {
        val alert = AlertDialog.Builder(this)

        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.send_to_client_dialog, null)
        val btnSubmit = alertLayout.findViewById<AppCompatButton>(R.id.btnSendToClient)
        val btnCancel = alertLayout.findViewById<AppCompatImageView>(R.id.ivCancel)
        val tvDescription = alertLayout.findViewById<AppCompatEditText>(R.id.tvDescription)
        val etTitle = alertLayout.findViewById<AppCompatTextView>(R.id.etTitle)
        initSpinnerTitleReason(etTitle)

        alert.setView(alertLayout)
        alert.setCancelable(false)

        val dialog = alert.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        btnSubmit.setOnClickListener {

            if (etTitle.text.isNullOrEmpty()) {
                AppUtils.showSnackBar(btnSubmit, getString(R.string.lbl_enter_title))
                return@setOnClickListener
            }

            val params: HashMap<String, Any?> = HashMap()
            params[ApiParams.VIO_IDS] = vio_ids
            params[ApiParams.VIO_TITLE] = violationTitleId
            params[ApiParams.VIO_DESCRIPTION] = tvDescription.text.toString()
            params[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
            mViewModel.sendToClientCallApi(params)
            dialog.dismiss()

        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }


    }


    var violationTitleId: String = "1"
    private fun initSpinnerTitleReason(selectdReason: AppCompatTextView) {
        val valetsViolationTitleList: ArrayList<String> = ArrayList()
        val valetsViolationTitleId: ArrayList<String> = ArrayList()
        valetsViolationTitleId.add("1")
        valetsViolationTitleId.add("2")
        valetsViolationTitleId.add("3")
        valetsViolationTitleId.add("4")
        valetsViolationTitleId.add("5")
        valetsViolationTitleList.add("Walkthrough")
        valetsViolationTitleList.add("Daily Violations")
        valetsViolationTitleList.add("Light Audit")
        valetsViolationTitleList.add("Common Area/Pet Stations")
        valetsViolationTitleList.add("Unit Audit")
        val selectViolationsPopupWindow = ListPopupWindow(this)
        selectViolationsPopupWindow.setAdapter(
            ArrayAdapter(
                this,
                R.layout.spinner_drop_down_item,
                valetsViolationTitleList
            )
        )
        selectViolationsPopupWindow.anchorView = selectdReason
        selectViolationsPopupWindow.isModal = true

        selectViolationsPopupWindow.setOnItemClickListener { _, _, position, _ ->
            val selectedSortBy = valetsViolationTitleList[position]
            violationTitleId = valetsViolationTitleId[position]

            selectdReason.text = selectedSortBy
            selectViolationsPopupWindow.dismiss()
        }
        selectdReason.setOnClickListener { selectViolationsPopupWindow.show() }


    }
}
