package com.enviroclean.ui.fragment.manager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enviroclean.R
import com.enviroclean.adapter.ManagerMyWorkAdapter
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseFragment
import com.enviroclean.customeview.PaginationScrollListenerLinear
import com.enviroclean.databinding.FragmentManagerMyWorkBinding
import com.enviroclean.model.WorkListResponse
import com.enviroclean.ui.activity.CheckInActivity
import com.enviroclean.ui.activity.LocationRouteActivity
import com.enviroclean.ui.activity.ManagerHomeActivity
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.viewmodel.ManagerMyWorkViewModel
import kotlinx.android.synthetic.main.week_layout.view.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by imobdev on 20/12/19
 */
class ManagerMyWorkFragment : BaseFragment<ManagerMyWorkViewModel>(), View.OnClickListener,
    BaseBindingAdapter.ItemClickListener<WorkListResponse.Result?> {
    override fun onItemClick(view: View, data: WorkListResponse.Result?, position: Int) {

        when (view.id) {
            R.id.ivCheckIn -> {
                if (currentWeek == selectedWeek) {
                    data?.let {
                        ManagerHomeActivity.socketConnectedManager = false
                        if (allReadyCheckInSchId == "") {
                            startActivity(
                                CheckInActivity.newInstance(
                                    context!!,
                                    data.commId,
                                    data.commSchId,
                                    AppConstants.WORK_TYPE_WEEK,
                                    "" + (instance.get(Calendar.MONTH) + 1) + "-" + (instance.get(
                                        Calendar.YEAR
                                    )),
                                    currentWeek.toString(),
                                    true
                                )
                            )
                        } else {
                            AppUtils.showSnackBar(
                                binding.rvManagerMyWork,
                                "Your are all ready checkin $allReadyCheckInSchName"
                            )
                        }

                    }
                } else {
                    AppUtils.showSnackBar(
                        binding.rvManagerMyWork,
                        getString(R.string.lbl_you_are_not_allow_week)
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
                if (allReadyCheckInSchId == data!!.commSchId.toString()) {
                    replaceFragment(
                        R.id.frame_container,
                        ManagerMyWorkCheckingFragment.newInstance(),
                        false
                    )
                }else{
                    AppUtils.showSnackBar(
                        binding.rvManagerMyWork,
                        "Your are all ready checkin $allReadyCheckInSchName"
                    )
                }
            }
        }
    }

    /*variable for current page in API pagination*/
    var CURRENT_PAGE: Int = 1
    /*Total pages for API pagination*/
    var TOTAL_PAGE: Int = 1
    lateinit var selectedDate: String
    /*loading flag*/
    var isLoading: Boolean = true
    private var noDataFound = false
    val instance = Calendar.getInstance()
    lateinit var allReadyCheckInSchId: String
    lateinit var allReadyCheckInSchName: String
    override fun onClick(p0: View?) {
        p0?.let {
            when (it.id) {
                R.id.viewWeekOne -> {
                    (binding.rvManagerMyWork.adapter as ManagerMyWorkAdapter).clear()
                    currentWeek = 1
                    // CURRENT_PAGE=1
                    isLoading = true
                    callApi()
                    setSelectedWeek(
                        binding.viewWeekOne,
                        binding.viewWeekTwo,
                        binding.viewWeekThree,
                        binding.viewWeekFour,
                        binding.viewWeekFive,
                        binding.viewWeekOne
                    )
                }
                R.id.viewWeekTwo -> {
                    (binding.rvManagerMyWork.adapter as ManagerMyWorkAdapter).clear()
                    currentWeek = 2
                    // CURRENT_PAGE=1
                    isLoading = true
                    callApi()
                    setSelectedWeek(
                        binding.viewWeekOne,
                        binding.viewWeekTwo,
                        binding.viewWeekThree,
                        binding.viewWeekFour,
                        binding.viewWeekFive,
                        binding.viewWeekTwo
                    )
                }
                R.id.viewWeekThree -> {
                    (binding.rvManagerMyWork.adapter as ManagerMyWorkAdapter).clear()
                    currentWeek = 3
                    //    CURRENT_PAGE=1
                    isLoading = true
                    callApi()
                    setSelectedWeek(
                        binding.viewWeekOne,
                        binding.viewWeekTwo,
                        binding.viewWeekThree,
                        binding.viewWeekFour,
                        binding.viewWeekFive,
                        binding.viewWeekThree
                    )
                }
                R.id.viewWeekFour -> {
                    (binding.rvManagerMyWork.adapter as ManagerMyWorkAdapter).clear()
                    currentWeek = 4
                    //   CURRENT_PAGE=1
                    isLoading = true
                    callApi()
                    setSelectedWeek(
                        binding.viewWeekOne,
                        binding.viewWeekTwo,
                        binding.viewWeekThree,
                        binding.viewWeekFour,
                        binding.viewWeekFive,
                        binding.viewWeekFour
                    )
                }

            }
        }
    }

    private lateinit var binding: FragmentManagerMyWorkBinding

    private val mViewModel: ManagerMyWorkViewModel by lazy {
        ViewModelProviders.of(this).get(ManagerMyWorkViewModel::class.java)
    }

    override fun getViewModel(): ManagerMyWorkViewModel {
        return mViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentManagerMyWorkBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        var currentWeek: Int = 0
        var selectedWeek: Int = 0
        fun newInstance(): ManagerMyWorkFragment {
            val bundle = Bundle()
            val fragment = ManagerMyWorkFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    fun init() {
        binding.viewWeekOne.tvNumberOfWeek.text = "1"
        binding.viewWeekTwo.tvNumberOfWeek.text = "2"
        binding.viewWeekThree.tvNumberOfWeek.text = "3"
        binding.viewWeekFour.tvNumberOfWeek.text = "4"
        binding.viewWeekFive.tvNumberOfWeek.text = "5"

        binding.viewWeekOne.setOnClickListener(this)
        binding.viewWeekTwo.setOnClickListener(this)
        binding.viewWeekThree.setOnClickListener(this)
        binding.viewWeekFour.setOnClickListener(this)

        val month_date = SimpleDateFormat("MMMM")
        binding.tvTitleMonth.text = month_date.format(instance.time)


        currentWeek = AppUtils.getCurrentWeek().toInt()
        if (currentWeek >= 4) {
            currentWeek = 4
        }

        selectedWeek = AppUtils.getCurrentWeek().toInt()
        if (selectedWeek >= 4) {
            selectedWeek = 4
        }
        val maxWeeknumber = instance.getActualMaximum(Calendar.WEEK_OF_MONTH)
        Log.e("WEEK OF MOTHNS", "---->" + maxWeeknumber)
        Log.e("SELECTED_WEEK", "---->" + currentWeek)
        setSelectedWeek(
            binding.viewWeekOne,
            binding.viewWeekTwo,
            binding.viewWeekThree,
            binding.viewWeekFour,
            binding.viewWeekFive,
            binding.viewWeekFive,
            currentWeek
        )

        binding.rvManagerMyWork.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            ) as RecyclerView.LayoutManager?
        val adapter = ManagerMyWorkAdapter()
        adapter.itemClickListener = this
        binding.rvManagerMyWork.adapter = adapter

        mViewModel.getResponse().observe(this, observeMyWork)
        callApi()

    }

    private val observeMyWork = Observer<WorkListResponse> {
        if (it.status) {
            allReadyCheckInSchId = it.checkin_sch_id
            allReadyCheckInSchName = it.checkin_sch_name
            (parentFragment as ManagerWorkAssignmentFragment).allReadyCheckInSchId=allReadyCheckInSchId
            (parentFragment as ManagerWorkAssignmentFragment).allReadyCheckInSchName=allReadyCheckInSchName
            if(allReadyCheckInSchId==""){

                (binding.rvManagerMyWork.adapter as ManagerMyWorkAdapter).setItem(it.result as java.util.ArrayList<WorkListResponse.Result?>)
                (binding.rvManagerMyWork.adapter as ManagerMyWorkAdapter).notifyDataSetChanged()
                binding.noDataCl.visibility = View.GONE
                initSpinnerCommunity(it.communities)
            }
        } else {
            binding.noDataCl.visibility = View.VISIBLE
            (binding.rvManagerMyWork.adapter as ManagerMyWorkAdapter).clear()
        }

    }

    /*Calling an API from viewModel class*/
    private fun callApi(progressShow: Boolean = true) {
        val param: HashMap<String, Any?> = HashMap()
        param[ApiParams.TYPE] = AppConstants.WORK_TYPE_WEEK
        param[ApiParams.WEEK_NO] = currentWeek
        param[ApiParams.COMM_ID] = communitesId
        param[ApiParams.MONTH] =
            "" + (instance.get(Calendar.MONTH) + 1) + "-" + (instance.get(Calendar.YEAR))
        param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE

        mViewModel.callApi(param, progressShow)

    }


    private fun setSelectedWeek(
        viewWeekOne: View,
        viewWeekTwo: View,
        viewWeekThree: View,
        viewWeekFour: View,
        viewWeekFive: View,
        selectedView: View,
        setDefaultSelected: Int = 0
    ) {

        context?.let {
            viewWeekOne.background =
                ContextCompat.getDrawable(it, R.drawable.background_shape_unselected)
            viewWeekOne.tvWeek.setTextColor(ContextCompat.getColor(it, R.color.black))
            viewWeekOne.tvNumberOfWeek.setTextColor(ContextCompat.getColor(it, R.color.black))

            viewWeekTwo.background =
                ContextCompat.getDrawable(it, R.drawable.background_shape_unselected)
            viewWeekTwo.tvWeek.setTextColor(ContextCompat.getColor(it, R.color.black))
            viewWeekTwo.tvNumberOfWeek.setTextColor(ContextCompat.getColor(it, R.color.black))

            viewWeekThree.background =
                ContextCompat.getDrawable(it, R.drawable.background_shape_unselected)
            viewWeekThree.tvWeek.setTextColor(ContextCompat.getColor(it, R.color.black))
            viewWeekThree.tvNumberOfWeek.setTextColor(ContextCompat.getColor(it, R.color.black))

            viewWeekFour.background =
                ContextCompat.getDrawable(it, R.drawable.background_shape_unselected)
            viewWeekFour.tvWeek.setTextColor(ContextCompat.getColor(it, R.color.black))
            viewWeekFour.tvNumberOfWeek.setTextColor(ContextCompat.getColor(it, R.color.black))

            viewWeekFive.background =
                ContextCompat.getDrawable(it, R.drawable.background_shape_unselected)
            viewWeekFive.tvWeek.setTextColor(ContextCompat.getColor(it, R.color.black))
            viewWeekFive.tvNumberOfWeek.setTextColor(ContextCompat.getColor(it, R.color.black))

            val textSizeMax = 18f
            val textNameSizeMax = 17f

            val textSizeMin = 14f
            val textNameSizeMin = 13f
            binding.viewWeekOne.tvWeek.textSize = textNameSizeMin
            binding.viewWeekOne.tvNumberOfWeek.textSize = textSizeMin
            binding.viewWeekTwo.tvWeek.textSize = textNameSizeMin
            binding.viewWeekTwo.tvNumberOfWeek.textSize = textSizeMin
            binding.viewWeekThree.tvWeek.textSize = textNameSizeMin
            binding.viewWeekThree.tvNumberOfWeek.textSize = textSizeMin
            binding.viewWeekFour.tvWeek.textSize = textNameSizeMin
            binding.viewWeekFour.tvNumberOfWeek.textSize = textSizeMin


            when (setDefaultSelected) {
                1 -> {
                    viewWeekOne.background =
                        ContextCompat.getDrawable(it, R.drawable.background_shape)
                    viewWeekOne.tvWeek.setTextColor(ContextCompat.getColor(it, R.color.blue_1))
                    viewWeekOne.tvNumberOfWeek.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.blue_1
                        )
                    )
                    viewWeekOne.tvWeek.textSize = textNameSizeMax
                    viewWeekOne.tvNumberOfWeek.textSize = textSizeMax

                }
                2 -> {
                    viewWeekTwo.background =
                        ContextCompat.getDrawable(it, R.drawable.background_shape)
                    viewWeekTwo.tvWeek.setTextColor(ContextCompat.getColor(it, R.color.blue_1))
                    viewWeekTwo.tvNumberOfWeek.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.blue_1
                        )
                    )
                    viewWeekTwo.tvWeek.textSize = textNameSizeMax
                    viewWeekTwo.tvNumberOfWeek.textSize = textSizeMax

                }
                3 -> {
                    viewWeekThree.background =
                        ContextCompat.getDrawable(it, R.drawable.background_shape)
                    viewWeekThree.tvWeek.setTextColor(ContextCompat.getColor(it, R.color.blue_1))
                    viewWeekThree.tvNumberOfWeek.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.blue_1
                        )
                    )
                    viewWeekThree.tvWeek.textSize = textNameSizeMax
                    viewWeekThree.tvNumberOfWeek.textSize = textSizeMax

                }
                4 -> {
                    viewWeekFour.background =
                        ContextCompat.getDrawable(it, R.drawable.background_shape)
                    viewWeekFour.tvWeek.setTextColor(ContextCompat.getColor(it, R.color.blue_1))
                    viewWeekFour.tvNumberOfWeek.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.blue_1
                        )
                    )
                    viewWeekFour.tvWeek.textSize = textNameSizeMax
                    viewWeekFour.tvNumberOfWeek.textSize = textSizeMax

                }
                5 -> {
                    viewWeekFive.background =
                        ContextCompat.getDrawable(it, R.drawable.background_shape)
                    viewWeekFive.tvWeek.setTextColor(ContextCompat.getColor(it, R.color.blue_1))
                    viewWeekFive.tvNumberOfWeek.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.blue_1
                        )
                    )
                }
                else -> {
                    selectedView.background =
                        ContextCompat.getDrawable(it, R.drawable.background_shape)
                    selectedView.tvWeek.setTextColor(ContextCompat.getColor(it, R.color.blue_1))
                    selectedView.tvNumberOfWeek.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.blue_1
                        )
                    )
                    selectedView.tvWeek.textSize = textNameSizeMax
                    selectedView.tvNumberOfWeek.textSize = textSizeMax
                }
            }
        }
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