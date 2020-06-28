package com.enviroclean.ui.activity

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enviroclean.R
import com.enviroclean.adapter.MyPaystubAdapter
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.customeview.PaginationScrollListenerLinear
import com.enviroclean.databinding.ActivityPayStubBinding
import com.enviroclean.model.PayStubResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.viewmodel.PayStubViewModel
import java.util.HashMap
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.enviroclean.ui.activity.SplashActivity.Companion.PERMISSION_REQUEST_CODE
import com.enviroclean.services.DownloadService
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.enviroclean.ui.activity.SplashActivity.Companion.MESSAGE_PROGRESS
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import androidx.databinding.adapters.SeekBarBindingAdapter.setProgress
import com.enviroclean.services.Download
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.R.attr.name
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class PayStubActivity : BaseActivity<PayStubViewModel>(PayStubActivity::class.java.simpleName) ,BaseActivity.ToolbarLeftMenuClickListener,
    BaseBindingAdapter.ItemClickListener<PayStubResponse.Result> {
    override fun onItemClick(view: View, data: PayStubResponse.Result?, position: Int) {

        when(view.id){

            R.id.btnDownloadPdf->{
                mPdfUrl=data!!.up_pdf_url
                mId=data.upId
                if(checkPermission()){
                    startDownload()
                } else {
                    requestPermission()
                }
            }
        }

    }

    override fun onLeftIconClicked() {
        onBackPressed()
    }

    var mPdfUrl:String=""
    var mId:Int=0
    private lateinit var binding:ActivityPayStubBinding
    private val mViewModel: PayStubViewModel by lazy {
        ViewModelProviders.of(this).get(PayStubViewModel::class.java)
    }

    override fun getViewModel(): PayStubViewModel {
        return mViewModel
    }

    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, PayStubActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_pay_stub)
        init()
        initToolbar()
    }

    private fun init() {
        binding.rvPayStub.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        val adapter = MyPaystubAdapter()
        binding.rvPayStub.adapter = adapter
        adapter.itemClickListener=this
        registerReceiver()
        loadMore()
        callApi()
        mViewModel.getResponse().observe(this,observePayStub)
    }

    private fun initToolbar() {
        setToolbarBackground(R.color.blue_toolbar)
        setToolbarTitle(R.string.lbl_my_pay_stub)
        setToolbarLeftIcon(R.drawable.ic_back_white, this)
    }

    private fun setPayStubcountData(result: PayStubResponse) {
        binding.tvPrice.text="$"+result.uTotalEarn
        binding.viewTotalHours.findViewById<AppCompatTextView>(R.id.tvCountName).text=resources.getString(R.string.lbl_total_hours)
        binding.viewTotalHours.findViewById<AppCompatTextView>(R.id.tvCountValue).text=""+result.uTotalHrs

        binding.viewAvgPerDay.findViewById<AppCompatTextView>(R.id.tvCountName).text=resources.getString(R.string.lbl_avg_per_day)
        binding.viewAvgPerDay.findViewById<AppCompatTextView>(R.id.tvCountValue).text="$"+result.uAvgDays

        binding.viewTotalDays.findViewById<AppCompatTextView>(R.id.tvCountName).text=resources.getString(R.string.lbl_total_days)
        binding.viewTotalDays.findViewById<AppCompatTextView>(R.id.tvCountValue).text=""+result.uTotalDays
        binding.mainView.visibility= View.VISIBLE
    }

    private var noDataFound = false
    /*loading flag*/
    var isLoading: Boolean = true
    /*variable for current page in API pagination*/
    var CURRENT_PAGE: Int = 1
    /*Total pages for API pagination*/
    var TOTAL_PAGE: Int = 1
    private fun loadMore() {
        /*recyclerview scroll listener*/
        binding.rvPayStub.addOnScrollListener(object :
            PaginationScrollListenerLinear(
                binding.rvPayStub.layoutManager as LinearLayoutManager,
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
                        (binding.rvPayStub.adapter as MyPaystubAdapter).addItem(
                            null
                        )
                        /*animate the footer view*/
                        val adapterCount =
                            (binding.rvPayStub.adapter as MyPaystubAdapter).itemCount
                        (binding.rvPayStub.adapter as MyPaystubAdapter).notifyItemInserted(
                            adapterCount - 1
                        )
                        (binding.rvPayStub.adapter as MyPaystubAdapter).addLoadingFooter()

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


    /*Calling an API from viewModel class*/
    private fun callApi(progressShow: Boolean = true) {
        val param: HashMap<String, Any?> = HashMap()
        param[ApiParams.PAGE] = CURRENT_PAGE
     
        param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE

        mViewModel.callApi(param, progressShow)

    }


    private val observePayStub= Observer<PayStubResponse>{
        if (it.status) {
            /*If current page is 1 then just add the data to adapter*/
            if (it.currentPage == 1) {
                /*total pages key*/
                setPayStubcountData(it)
                TOTAL_PAGE = it.totalPages
                /*setting items to ListView*/
                (binding.rvPayStub.adapter as MyPaystubAdapter).setItem(it.result as java.util.ArrayList<PayStubResponse.Result?>)
                (binding.rvPayStub.adapter as MyPaystubAdapter).notifyDataSetChanged()
                noDataFound = false


            } else {
                /*Removing a dummy item that we added to show a footer progress*/
                (binding.rvPayStub.adapter as MyPaystubAdapter).removeLoadingFooter(true)
                val adapterCount = (binding.rvPayStub.adapter as MyPaystubAdapter).itemCount
                (binding.rvPayStub.adapter as MyPaystubAdapter).removeItem(adapterCount - 1)

                /*Adding new items that we received in response via item range*/
                val sizeList = (binding.rvPayStub.adapter as MyPaystubAdapter).itemCount
                (binding.rvPayStub.adapter as MyPaystubAdapter).addItems(it.result as java.util.ArrayList<PayStubResponse.Result?>)
                (binding.rvPayStub.adapter as MyPaystubAdapter).notifyItemRangeInserted(
                    sizeList,
                    it.result.size
                )
            }
            isLoading=true
        } else {
            if (it.currentPage == 1) {
                noDataFound = true
                binding.mainView.visibility= View.GONE
            }
        }

    }
    override fun onBackPressed() {

        val returnIntent = Intent()
        returnIntent.putExtra("result", "result")
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
        AppUtils.startFromRightToLeft(this)
        AppUtils.hideKeyboard(this)
        super.onBackPressed()
    }

    private fun startDownload() {
        val intent = Intent(this, DownloadService::class.java)
        intent.putExtra(AppConstants.PDF_URL,mPdfUrl)
        intent.putExtra("up_id",mId)
        startService(intent)

    }

    private fun registerReceiver() {

        val bManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(MESSAGE_PROGRESS)
        bManager.registerReceiver(broadcastReceiver, intentFilter)

    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (intent.action == MESSAGE_PROGRESS) {

                val download = intent.getParcelableExtra<Download>("download")

            }
        }
    }
    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            WRITE_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {

        requestPermissions(
            this,
            arrayOf<String>(WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )

    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startDownload()
            }
        }
    }
}
