package com.enviroclean.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.enviroclean.R
import com.enviroclean.adapter.ViolationViewPagerAdapter
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityViolationReportDetailsBinding
import com.enviroclean.model.Banner
import com.enviroclean.model.ViolationDetailsResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.viewmodel.ViolationReportDetailsViewModel
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ViolationReportDetailsActivity :
    BaseActivity<ViolationReportDetailsViewModel>(LoginActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener, View.OnClickListener {
    override fun onClick(p0: View?) {

        val param:HashMap<String,Any?> = HashMap()
        param[ApiParams.VIO_ID] = mViolationId
        mViewModel.callApiSendToClient(param)
    }

    override fun onLeftIconClicked() {
        onBackPressed()
    }

    private lateinit var binding: ActivityViolationReportDetailsBinding

    private val mViewModel: ViolationReportDetailsViewModel by lazy {
        ViewModelProviders.of(this).get(ViolationReportDetailsViewModel::class.java)
    }

    override fun getViewModel(): ViolationReportDetailsViewModel {
        return mViewModel
    }

    private val mViolationId: String by lazy {
        intent.getStringExtra(AppConstants.VIOLATION_ID)
    }

    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    companion object {
        fun newInstance(context: Context, vioId: String): Intent {
            val intent = Intent(context, ViolationReportDetailsActivity::class.java)
            intent.putExtra(AppConstants.VIOLATION_ID, vioId)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_violation_report_details)
        initToolBar()
        init()

    }

    fun init(){
        val param:HashMap<String,Any?> = HashMap()
        param[ApiParams.VIO_ID] = mViolationId
        binding.btnSendToClient.setOnClickListener(this)
        mViewModel.callApi(param)
        mViewModel.getResponse().observe(this, observerViolationDetails)
        mViewModel.getResponseSendToClient().observe(this, observerSendToClient)
    }
    private val observerViolationDetails=Observer<ViolationDetailsResponse>{
        if(it.status){

            if(it.result.vio_sent_flag==1){
                binding.btnSendToClient.visibility=View.GONE
            }else{
                binding.btnSendToClient.visibility=View.VISIBLE
            }
            binding.tvViolationName.text="Violation: "+it.result.vioName
            binding.tvUnitNo.text="Unit No: "+it.result.vioUnitNumber
            binding.tvDescription.text=it.result.vioDescription
            setupViewPagerAdapter(it.result)
            binding.mainView.visibility=View.VISIBLE
        }
    }
    private val observerSendToClient=Observer<BaseResponse>{
        if(it.status){
            AppUtils.showToast(this@ViolationReportDetailsActivity,it.message)
          finish()
        }
    }



    private fun initToolBar() {
        setToolbarTitle(R.string.lbl_violation)
        setToolbarLeftIcon(R.drawable.ic_back_white, this)
        setToolbarBackground(R.color.blue_toolbar)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }

    private fun setupViewPagerAdapter(banners: ViolationDetailsResponse.Result) {
        binding.vpViolation.adapter = ViolationViewPagerAdapter(this, banners.vioImages)

        binding.indicator.setViewPager(binding.vpViolation)

        val density = resources.displayMetrics.density
        //Set circle indicator radius
        binding.indicator.radius = 3 * density

        // Pager listener over indicator
        binding.indicator.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageSelected(position: Int) {
                currentPage = position

            }

            override fun onPageScrolled(pos: Int, arg1: Float, arg2: Int) {

            }

            override fun onPageScrollStateChanged(pos: Int) {

            }
        })
        setupAutoPager(banners.vioImages.size)
    }

    var timer: Timer? = null
    private var currentPage = 0
    private fun setupAutoPager(size: Int) {
        timer = Timer()
        val handler = Handler()

        val update = Runnable {
            binding.vpViolation.setCurrentItem(currentPage, true)
            if (currentPage == size) {
                currentPage = 0
            } else {
                ++currentPage
            }
        }


        timer!!.schedule(object : TimerTask() {
            override fun run() {
                handler.post(update)
            }
        }, 500, 4500)
    }
}
