package com.enviroclean.ui.activity

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.enviroclean.R
import com.enviroclean.adapter.ScheduleDaysAdapter
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.databinding.ActivityCreateScheduleBinding
import com.enviroclean.model.CommunityListResponse
import com.enviroclean.model.DaysModel
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.CreateScheduleViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CreateScheduleActivity :
    BaseActivity<CreateScheduleViewModel>(CreateScheduleActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener, BaseActivity.ToolbarRightMenuClickListener,
    BaseBindingAdapter.ItemClickListener<DaysModel>, View.OnClickListener {
    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.btnSubmit -> {
                AppUtils.hideKeyboard(this)
                mViewModel.checkValidation(
                    comminityID,
                    binding.etSchName.text.toString(),
                    inTime,
                    outTime
                )
            }

            R.id.tvInTime -> {
                AppUtils.hideKeyboard(this)
                val cal = Calendar.getInstance()
                val timeSetListener =
                    TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->

                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        binding.tvInTime.text = SimpleDateFormat("hh:mm a").format(cal.time)
                        inTime=SimpleDateFormat("HH:mm").format(cal.time)
                       Log.e("INTIME",inTime)

                    }
                TimePickerDialog(
                    this,
                    timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false
                ).show()
            }

            R.id.tvOutTime -> {
                AppUtils.hideKeyboard(this)
                val cal = Calendar.getInstance()
                val timeSetListener =
                    TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->


                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        binding.tvOutTime.text = SimpleDateFormat("hh:mm a").format(cal.time)

                        outTime = SimpleDateFormat("HH:mm").format(cal.time)

                        AppUtils.localToGMT(cal.time)
                        Log.e("TIME_SELCTED","1---->"+cal.time)
                        Log.e("outTime",outTime)
                    }
                TimePickerDialog(
                    this,
                    timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    false
                ).show()
            }
        }
    }
    var dayCounter=0
    var isSingleClick=false
    override fun onItemClick(view: View, data: DaysModel?, position: Int) {
        AppUtils.hideKeyboard(this)
        var days: DaysModel = data!!
        days.daysName = data.daysName
        data.color = data.color
        days.isSelected = !data.isSelected
        dayCounter=0
        for (i in 0 until (binding.rvDays.adapter as ScheduleDaysAdapter).items.size){
            if((binding.rvDays.adapter as ScheduleDaysAdapter).items[i]!!.isSelected){
                dayCounter += 1
            }
        }
        isSingleClick=true
        if(dayCounter==7){
            binding.tvDayOfWeek.isChecked = true
        }else{
            binding.tvDayOfWeek.isChecked = false
        }
        daysAdapter.items[position] = days
        daysAdapter.notifyItemChanged(position,days)
        isSingleClick=false
    }

    override fun onRightMenuClicked() {
        startActivity(ListOfScheduleActivity.newInstance(this))
        AppUtils.startFromRightToLeft(this)
    }

    override fun onLeftIconClicked() {
      AppUtils.hideKeyboard(this)
        onBackPressed()
    }

    private lateinit var binding: ActivityCreateScheduleBinding
    private var comminityID: String = ""
    private var inTime: String = ""
    private var outTime: String = ""
    private val mViewModel: CreateScheduleViewModel by lazy {
        ViewModelProviders.of(this).get(CreateScheduleViewModel::class.java)
    }

    override fun getViewModel(): CreateScheduleViewModel {
        return mViewModel
    }


    private lateinit var daysAdapter: ScheduleDaysAdapter

    lateinit var prefs: Prefs
    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/

    companion object {
        fun newInstance(
            context: Context
        ): Intent {
            val intent = Intent(context, CreateScheduleActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_schedule)
        prefs = Prefs.getInstance(this)!!
        inti()
        initToolbar()
    }

    private fun inti() {
        mViewModel.callApi()
        mViewModel.getResponse().observe(this,observeCommunityList)
        mViewModel.getDaysList()
        setDaysAdapter()
        setObserver()

        binding.btnSubmit.setOnClickListener(this)
        binding.tvInTime.setOnClickListener(this)
        binding.tvOutTime.setOnClickListener(this)

    }
    private fun initToolbar() {
        setToolbarBackground(R.color.blue_toolbar)
        setToolbarTitle(R.string.lbl_create_schedule)
        setToolbarLeftIcon(R.drawable.ic_cancel_createschedule, this)
        setToolbarRightMenuIcon(R.drawable.ic_list_creatschedule, this)
    }
    private val observeCommunityList= Observer<CommunityListResponse>{
        initSpinnerSelectedCom(it.result)
    }

    private fun initSpinnerSelectedCom(result: ArrayList<CommunityListResponse.Result>) {
        val commList:ArrayList<String> = ArrayList()
        val commIds: ArrayList<String> = ArrayList()
        for(i in 0 until result.size){
            commList.add(result[i].commName)
            commIds.add(result[i].commId.toString())
        }
        val selectedComPopupWindow = ListPopupWindow(this)
        selectedComPopupWindow.setAdapter(
            ArrayAdapter(
                this,
                R.layout.spinner_drop_down_item,
                commList
            )
        )
        selectedComPopupWindow.anchorView = binding.tvSelectedCom
        selectedComPopupWindow.isModal = true

        selectedComPopupWindow.setOnItemClickListener { _, _, position, _ ->
            val selectedSortBy = commList[position]
            comminityID = commIds[position]
            binding.tvSelectedCom.text = selectedSortBy
            selectedComPopupWindow.dismiss()
        }
        binding.tvSelectedCom.setOnClickListener {
            AppUtils.hideKeyboard(this)
            selectedComPopupWindow.show() }
    }

    private fun setDaysAdapter() {
        val chipsLayoutManager = ChipsLayoutManager.newBuilder(this)
            .setChildGravity(Gravity.RIGHT)
            .setScrollingEnabled(true)
            .setGravityResolver { Gravity.LEFT }
            .setOrientation(ChipsLayoutManager.HORIZONTAL)
            .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
            .build()
        binding.rvDays.layoutManager = chipsLayoutManager
        daysAdapter = ScheduleDaysAdapter(this, binding.tvDayOfWeek)
        binding.rvDays.adapter = daysAdapter
        daysAdapter.itemClickListener = this
        selectedAllDay()

        mViewModel.getDayListResponse()
            .observe({ this.lifecycle }, { response: ArrayList<DaysModel?>? ->
                daysAdapter.setItem(response!!)
                daysAdapter.notifyDataSetChanged()
            })
    }

    private fun selectedAllDay() {

        binding.tvDayOfWeek.setOnCheckedChangeListener { _, b ->
            if (!isSingleClick){
                for(i in 0 until (binding.rvDays.adapter as ScheduleDaysAdapter).items.size){
                    (binding.rvDays.adapter as ScheduleDaysAdapter).items[i]!!.isSelected = b
                }
                (binding.rvDays.adapter as ScheduleDaysAdapter).notifyItemRangeChanged(0,(binding.rvDays.adapter as ScheduleDaysAdapter).items.size)
            }else{
                isSingleClick=false
            }
        }
    }


    private fun setObserver() {
        mViewModel.getValidationError().observe({ this.lifecycle }, { msg: String? ->
            AppUtils.showSnackBar(binding.root, msg!!)
        })

        mViewModel.getCreateScheduleResponse().observe({ this.lifecycle }, { response: Boolean? ->
            clearData()
        })
    }

    private fun clearData() {
        binding.tvDayOfWeek.isChecked=false
        comminityID = ""
        binding.tvSelectedCom.text = getString(R.string.lbl_selected_community)
        binding.etSchName.setText("")
        binding.tvInTime.text = getString(R.string.lbl_clock_in_time)
        inTime = ""
        binding.tvOutTime.text = getString(R.string.lbl_clock_out_time)
        outTime = ""
        mViewModel.getDaysList()
    }
}
