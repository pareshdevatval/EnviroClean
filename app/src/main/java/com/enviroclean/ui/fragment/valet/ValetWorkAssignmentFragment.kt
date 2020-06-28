package com.enviroclean.ui.fragment.valet

import android.os.Bundle
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
import com.enviroclean.adapter.ValetWorkAssignmentAdapter
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseFragment
import com.enviroclean.customeview.PaginationScrollListenerLinear
import com.enviroclean.databinding.FragmentValetWorkAssignmentBinding
import com.enviroclean.model.WorkListResponse
import com.enviroclean.ui.activity.CheckInActivity
import com.enviroclean.ui.activity.LocationRouteActivity
import com.enviroclean.ui.activity.NotificationListActivity
import com.enviroclean.ui.activity.ValetHomeActivity
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.ValetWorkAssignmentViewModel
import com.vivekkaushik.datepicker.OnDateSelectedListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*


/**
 * Created by imobdev on 20/12/19
 */
class ValetWorkAssignmentFragment : BaseFragment<ValetWorkAssignmentViewModel>(),
    BaseBindingAdapter.ItemClickListener<WorkListResponse.Result?> {
    override fun onItemClick(view: View, data: WorkListResponse.Result?, position: Int) {
        when (view.id) {
            R.id.ivCheckIn -> {
                if (currentDate == selectedDate) {
                    data?.let {
                        ValetHomeActivity.socketConnectedValets = false
                        startActivity(
                            CheckInActivity.newInstance(
                                context!!,
                                data.commId,
                                data.commSchId,
                                AppConstants.WORK_TYPE_DAY,
                                selectedDate,
                                "",
                                true
                            )
                        )
                    }
                } else {
                    AppUtils.showSnackBar(
                        binding.rvWorkAssignment,
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
                AppUtils.startFromRightToLeft(context!!)
            }
        }
    }

    var communitesId: String = ""
    lateinit var prefs: Prefs
    private lateinit var binding: FragmentValetWorkAssignmentBinding
    /*variable for current page in API pagination*/
    var CURRENT_PAGE: Int = 1
    /*Total pages for API pagination*/
    var TOTAL_PAGE: Int = 1

    /*loading flag*/
    var isLoading: Boolean = true
    private var noDataFound = false
    val instance = Calendar.getInstance()
    private val mViewModel: ValetWorkAssignmentViewModel by lazy {
        ViewModelProviders.of(this).get(ValetWorkAssignmentViewModel::class.java)
    }

    override fun getViewModel(): ValetWorkAssignmentViewModel {
        return mViewModel
    }

    companion object {
        lateinit var selectedDate: String
        lateinit var currentDate: String

        fun newInstance(): ValetWorkAssignmentFragment {
            val bundle = Bundle()
            val fragment = ValetWorkAssignmentFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentValetWorkAssignmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedDate =
            instance.get(Calendar.DATE).toString() + "-" + (instance.get(Calendar.MONTH) + 1) + "-" + (instance.get(
                Calendar.YEAR
            ))
        currentDate =
            instance.get(Calendar.DATE).toString() + "-" + (instance.get(Calendar.MONTH) + 1) + "-" + (instance.get(
                Calendar.YEAR
            ))
        prefs = Prefs.getInstance(context!!)!!
        init()
        setToolbar()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        setNotificationCount(binding.toolbar, prefs.notificationCount!!)

    }

    @Subscribe
    fun onEvent(count: String) {
        if (count == AppConstants.NOTIFICATION_COUNT) {

            setNotificationCount(binding.toolbar, prefs.notificationCount!!)
        }
    }

    private fun init() {
        EventBus.getDefault().register(this)

        binding.rvWorkAssignment.layoutManager =
            LinearLayoutManager(context) as RecyclerView.LayoutManager?
        val adapter = ValetWorkAssignmentAdapter()

        adapter.itemClickListener = this
        binding.rvWorkAssignment.adapter = adapter
        setDatePiker()
        mViewModel.getResponse().observe(this, observeValetesList)
        callApi()
    }

    private fun setDatePiker() {
        val instance = Calendar.getInstance()
        binding.tvTitleDate.text =
            AppUtils.setMonAndDate(instance.get(Calendar.DATE), (instance.get(Calendar.MONTH)))

        binding.datePickerTimeline.setInitialDate(2019, 11, 1)
        binding.datePickerTimeline.setActiveDate(Calendar.getInstance())
        binding.datePickerTimeline.setOnDateSelectedListener(object : OnDateSelectedListener {
            override fun onDateSelected(year: Int, month: Int, day: Int, dayOfWeek: Int) {
                binding.tvTitleDate.text = AppUtils.setMonAndDate(day, month)
                isLoading = true
                selectedDate = "" + day + "-" + (month + 1) + "-" + year
                CURRENT_PAGE = 1
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


    private val observeValetesList = Observer<WorkListResponse> {
        if (it.status) {
            binding.noDataCl.visibility = View.GONE
            if (progressDialog != null) {
                mViewModel.loadingVisibility.value = false
            }
            (binding.rvWorkAssignment.adapter as ValetWorkAssignmentAdapter).setItem(it.result as java.util.ArrayList<WorkListResponse.Result?>)
            (binding.rvWorkAssignment.adapter as ValetWorkAssignmentAdapter).notifyDataSetChanged()
            initSpinnerCommunity(it.communities)
        } else {
            if (it.currentPage == 1) {
                noDataFound = true

            }
            binding.noDataCl.visibility = View.VISIBLE
            (binding.rvWorkAssignment.adapter as ValetWorkAssignmentAdapter).clear()
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

    private fun setToolbar() {
        binding.toolbar.tvToolbarTitle.text = getString(R.string.lbl_work_assignment)
        setNotificationCount(binding.toolbar, prefs.notificationCount!!)
        setToolbarRight1MenuIcon(binding.toolbar, R.drawable.ic_notification_white, object :
            BaseActivity.ToolbarRight1MenuClickListener {
            override fun onRight1MenuClicked() {
                context?.let {
                    startActivity(NotificationListActivity.newInstance(context!!))

                }
            }
        })

    }

    private fun initSpinnerCommunity(communities: ArrayList<WorkListResponse.Communities>) {
        val commnitesNameList: ArrayList<String> = ArrayList()
        var selectedPotion: Int = 0
        for (i in 0 until communities.size) {
            commnitesNameList.add(communities[i].commName)
            if (communitesId.isNotEmpty()) {
                if (communitesId == communities[i].commId.toString()) {
                    selectedPotion = i
                }
            } else {
                communitesId = communities[0].commId.toString()
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