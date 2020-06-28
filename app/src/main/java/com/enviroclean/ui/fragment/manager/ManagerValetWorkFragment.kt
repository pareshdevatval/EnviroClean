package com.enviroclean.ui.fragment.manager

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enviroclean.R
import com.enviroclean.adapter.ManagerValetsWorkAdapter
import com.enviroclean.adapter.ValetWorkAssignmentAdapter
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseFragment
import com.enviroclean.customeview.PaginationScrollListenerLinear
import com.enviroclean.databinding.FragmentManagerValetWorkBinding
import com.enviroclean.model.WorkListResponse
import com.enviroclean.ui.activity.*
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.viewmodel.ManagerValetWorkViewModel
import com.vivekkaushik.datepicker.OnDateSelectedListener
import kotlinx.android.synthetic.main.item_manager_valets_work.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by imobdev on 19/12/19
 */
class ManagerValetWorkFragment : BaseFragment<ManagerValetWorkViewModel>(),
    BaseBindingAdapter.ItemClickListener<WorkListResponse.Result>, View.OnClickListener {
    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                R.id.tvViolationCount -> {
                    if(violationCount!=0){
                        startActivity(ViolationReportListActivity.newInstance(context!!, communitesId,SELECTED_DATE))
                        AppUtils.startFromRightToLeft(context!!)
                    }
                }
            }
        }
    }

    override fun onItemClick(view: View, data: WorkListResponse.Result?, position: Int) {
        when (view.id) {
            R.id.ivMap -> {
                startActivity(
                    LiveTrackingActivity.newInstance(
                        context!!,
                        data!!.commId.toString(),
                        data.commSchId.toString(),
                        SELECTED_DATE
                    )
                )
                AppUtils.startFromRightToLeft(context!!)
            }
            R.id.ivCheckIn -> {
                ManagerValetWorkCheckingFragment.WITHOUT_CHECK = 1
                if (currentDate == selectedDate) {
                    data?.let {

                    if((parentFragment as ManagerWorkAssignmentFragment).allReadyCheckInSchId==""){

                        ManagerHomeActivity.socketConnectedManager = false
                        startActivity(CheckInActivity.newInstance(
                            context!!,
                            data.commId,
                            data.commSchId,
                            AppConstants.WORK_TYPE_DAY,
                            selectedDate,"",true
                        ),
                            ActivityOptions.makeSceneTransitionAnimation(activity as ManagerHomeActivity).toBundle())
                    }else{
                        AppUtils.showSnackBar(
                            binding.rvManagerValetsWork,
                            "You are already checked-in in "+ (parentFragment as ManagerWorkAssignmentFragment).allReadyCheckInSchName+" in my work"
                        )
                    }
                    }
                } else {
                    AppUtils.showSnackBar(
                        binding.rvManagerValetsWork,
                        getString(R.string.lbl_you_are_not_allow)
                    )
                }

            }
            R.id.ivLocation -> {
                startActivity(
                    LocationRouteActivity.newInstance(
                        context!!,
                        data!!.commLatitude,
                        data.commLongitude
                    )
                )
            }

            else -> {
                ManagerValetWorkCheckingFragment.WITHOUT_CHECK = 2

                COMM_ID = data!!.commId.toString()
                SCH_ID = data.commSchId.toString()
                addFragment(
                    R.id.frame_container,
                    ManagerValetWorkWithoutCheckingFragment.newInstance(),
                    true
                )
            }

        }
    }

    private lateinit var binding: FragmentManagerValetWorkBinding
     var violationCount:Int=0
    /*variable for current page in API pagination*/
    var CURRENT_PAGE: Int = 1
    /*Total pages for API pagination*/
    var TOTAL_PAGE: Int = 1
    lateinit var selectedDate: String
    /*loading flag*/
    var isLoading: Boolean = true
    private var noDataFound = false
    val instance = Calendar.getInstance()
    private val mViewModel: ManagerValetWorkViewModel by lazy {
        ViewModelProviders.of(this).get(ManagerValetWorkViewModel::class.java)
    }

    override fun getViewModel(): ManagerValetWorkViewModel {
        return mViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentManagerValetWorkBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        lateinit var SELECTED_DATE: String
        lateinit var currentDate: String
        lateinit var COMM_ID: String
        lateinit var SCH_ID: String
        fun newInstance(): ManagerValetWorkFragment {
            val bundle = Bundle()
            val fragment = ManagerValetWorkFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentDate =
            instance.get(Calendar.DATE).toString() + "-" + (instance.get(Calendar.MONTH) + 1) + "-" + (instance.get(
                Calendar.YEAR
            ))
        selectedDate =
            instance.get(Calendar.DATE).toString() + "-" + (instance.get(Calendar.MONTH) + 1) + "-" + (instance.get(
                Calendar.YEAR
            ))
        SELECTED_DATE = selectedDate
        init()
        setDatePiker()
    }


    @SuppressLint("WrongConstant")
    private fun setDatePiker() {

        binding.tvTitleDate.text =
            AppUtils.setMonAndDate(instance.get(Calendar.DATE), (instance.get(Calendar.MONTH)))
        binding.datePickerTimeline.setInitialDate(2019, 10, 1)
        binding.datePickerTimeline.setActiveDate(Calendar.getInstance())

        binding.datePickerTimeline.setOnDateSelectedListener(object : OnDateSelectedListener {
            override fun onDateSelected(year: Int, month: Int, day: Int, dayOfWeek: Int) {
                binding.tvTitleDate.text = AppUtils.setMonAndDate(day, month)

                Log.e("SELECTED_DATE", "----->$day-$month-$year")
                (binding.rvManagerValetsWork.adapter as ManagerValetsWorkAdapter).clear()
                isLoading = true
                selectedDate = "" + day + "-" + (month + 1) + "-" + year

                SELECTED_DATE = selectedDate
                callApi()
            }

            override fun onDisabledDateSelected(
                year: Int,
                month: Int,
                day: Int,
                dayOfWeek: Int,
                isDisabled: Boolean
            ) {

            }
        })
    }

    private fun init() {
        binding.rvManagerValetsWork.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            ) as RecyclerView.LayoutManager?
        val adapter = ManagerValetsWorkAdapter()
        adapter.itemClickListener = this
        binding.rvManagerValetsWork.adapter = adapter
        binding.tvViolationCount.setOnClickListener(this)
        mViewModel.getResponse().observe(this, observeValetesList)

        callApi()

    }


    private val observeValetesList = Observer<WorkListResponse> {
        if (it.status) {
            (binding.rvManagerValetsWork.adapter as ManagerValetsWorkAdapter).setItem(it.result as ArrayList<WorkListResponse.Result?>)
            (binding.rvManagerValetsWork.adapter as ManagerValetsWorkAdapter).notifyDataSetChanged()

            violationCount=it.comm_vio_count
            if(violationCount!=0){
                binding.tvViolationCount.visibility=View.VISIBLE
                binding.tvViolationCount.text =
                    "View Photo Report (" + it.comm_vio_count.toString() + ")"
            }else{
                binding.tvViolationCount.visibility=View.GONE
            }
            binding.noDataCl.visibility=View.GONE
            initSpinnerCommunity(it.communities)
        } else {
            if (it.currentPage == 1) {
                noDataFound = true

            }
            binding.tvViolationCount.visibility=View.GONE
            binding.noDataCl.visibility=View.VISIBLE
            (binding.rvManagerValetsWork.adapter as ManagerValetsWorkAdapter).clear()
        }

    }

    /*Calling an API from viewModel class*/
    private fun callApi(progressShow: Boolean = true) {
        val param: HashMap<String, Any?> = HashMap()
        param[ApiParams.TYPE] = AppConstants.WORK_TYPE_DAY
        param[ApiParams.DATE] = selectedDate
        param[ApiParams.COMM_ID] = communitesId
        param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE

        mViewModel.callApi(param, progressShow)

    }

    var communitesId: String = ""

    private fun initSpinnerCommunity(communities: ArrayList<WorkListResponse.Communities>) {
        val commnitesNameList: ArrayList<String> = ArrayList()
        var selectedPotion: Int = 0
        for (i in 0 until communities.size) {
            commnitesNameList.add(communities[i].commName)
            if (communitesId.isNotEmpty()) {
                if (communitesId == communities[i].commId.toString()) {
                    selectedPotion = i
                }
            }else{
                communitesId= communities[0].commId.toString()
            }
        }
        val selectCommunityPopupWindow = ListPopupWindow(context!!)
        selectCommunityPopupWindow.setAdapter(
            ArrayAdapter(
                context!!,
                R.layout.spinner_drop_down_item,
                commnitesNameList
            )
        )
        binding.tvSelectCommunityList.text = commnitesNameList[selectedPotion]
        selectCommunityPopupWindow.anchorView = binding.tvSelectCommunityList
        selectCommunityPopupWindow.isModal = true

        selectCommunityPopupWindow.setOnItemClickListener { _, _, position, _ ->
            val selectedSortBy = commnitesNameList[position]
            communitesId = communities[position].commId.toString()
            binding.tvSelectCommunityList.text = selectedSortBy
            CURRENT_PAGE = 1
            callApi()
            selectCommunityPopupWindow.dismiss()
        }
        binding.tvSelectCommunityList.setOnClickListener { selectCommunityPopupWindow.show() }
    }
}