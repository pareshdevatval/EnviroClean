package com.enviroclean.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enviroclean.R
import com.enviroclean.adapter.NotificationAdapter
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseActivity
import com.enviroclean.customeview.PaginationScrollListenerLinear
import com.enviroclean.databinding.ActivityNotificationListBinding
import com.enviroclean.model.NotificationListResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.NotificationListViewModel
import java.util.*

class NotificationListActivity :
    BaseActivity<NotificationListViewModel>(LoginActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener {
    override fun onLeftIconClicked() {
        onBackPressed()
    }

    private lateinit var binding: ActivityNotificationListBinding

    private val mViewModel: NotificationListViewModel by lazy {
        ViewModelProviders.of(this).get(NotificationListViewModel::class.java)
    }

    override fun getViewModel(): NotificationListViewModel {
        return mViewModel
    }

    /*variable for current page in API pagination*/
    var CURRENT_PAGE: Int = 1
    /*Total pages for API pagination*/
    var TOTAL_PAGE: Int = 1

    /*loading flag*/
    var isLoading: Boolean = true
    private var noDataFound = false
    lateinit var prefs: Prefs
    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    val mIsNotification: Boolean by lazy {
        intent.getBooleanExtra(AppConstants.IS_FIREBASE_NOTIFICATION, false)
    }
    companion object {
        fun newInstance(context: Context, isNotification: Boolean = false): Intent {
            val intent = Intent(context, NotificationListActivity::class.java)
            intent.putExtra(AppConstants.IS_FIREBASE_NOTIFICATION, isNotification)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification_list)
        prefs = Prefs.getInstance(this)!!
        initToolBar()
        init()
    }

    fun init() {
        prefs.notificationCount=""
        binding.rvNotification.layoutManager =
            LinearLayoutManager(this) as RecyclerView.LayoutManager?
        val adapter = NotificationAdapter()
        binding.rvNotification.adapter = adapter
        mViewModel.getResponse().observe(this, observeNotificationList)
        mViewModel.getResponseClearAll().observe(this, observeClearAllNotificationList)
        loadMore()
        callApi()

    }

    private fun initToolBar() {
        setToolbarTitle(R.string.lbl_notifications)
        setToolbarLeftIcon(R.drawable.ic_back_white, this)
        setToolbarBackground(R.color.blue_toolbar)


        binding.toolbar.tvToolbarRightTxtMenu.setOnClickListener {

            showSendToClientDialog()
        }
    }

    private val observeClearAllNotificationList = Observer<BaseResponse> {
        if (it.status) {
            (binding.rvNotification.adapter as NotificationAdapter).clear()
            (binding.rvNotification.adapter as NotificationAdapter).notifyDataSetChanged()
            binding.ivEmpty.visibility = View.VISIBLE
            binding.tvDes.visibility = View.VISIBLE
            binding.tvReadTitle.visibility = View.GONE
            binding.toolbar.tvToolbarRightTxtMenu.visibility=View.GONE
        }

    }
    private val observeNotificationList = Observer<NotificationListResponse> {
        if (it.status) {
            binding.ivEmpty.visibility = View.GONE
            binding.tvDes.visibility = View.GONE
            /*If current page is 1 then just add the data to adapter*/
            if (it.currentPage == 1) {
                setToolbarRightTextTitle(R.string.lbl_clear_all)
                if (it.unreadCount != 0) {
                    binding.tvReadTitle.visibility = View.VISIBLE
                    binding.tvReadTitle.text =
                        "You have " + it.unreadCount + " Unread Notifications"
                } else {
                    binding.tvReadTitle.visibility = View.GONE
                }
                //    total pages key
                TOTAL_PAGE = it.totalPages
                //  setting items to ListView
                (binding.rvNotification.adapter as NotificationAdapter).setItem(it.result)
                (binding.rvNotification.adapter as NotificationAdapter).notifyDataSetChanged()
                noDataFound = false


            } else {
                // Removing a dummy item that we added to show a footer progress
                (binding.rvNotification.adapter as NotificationAdapter).removeLoadingFooter(true)
                val adapterCount = (binding.rvNotification.adapter as NotificationAdapter).itemCount
                (binding.rvNotification.adapter as NotificationAdapter).removeItem(adapterCount - 1)

                // Adding new items that we received in response via item range
                val sizeList = (binding.rvNotification.adapter as NotificationAdapter).itemCount
                (binding.rvNotification.adapter as NotificationAdapter).addItems(it.result)
                (binding.rvNotification.adapter as NotificationAdapter).notifyItemRangeInserted(
                    sizeList,
                    it.result.size
                )
            }
            isLoading = true
        } else {
            if (it.currentPage == 1) {
                noDataFound = true
                binding.toolbar.tvToolbarRightTxtMenu.visibility=View.GONE
                binding.ivEmpty.visibility = View.VISIBLE
                binding.tvDes.visibility = View.VISIBLE
            }
        }

    }

    /*A load More method to detect the end of the recyclerview and logic to check if we need to go for load more or not*/
    private fun loadMore() {
        /*recyclerview scroll listener*/
        binding.rvNotification.addOnScrollListener(object :
            PaginationScrollListenerLinear(
                binding.rvNotification.layoutManager as LinearLayoutManager,
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
                        (binding.rvNotification.adapter as NotificationAdapter).addItem(
                            null
                        )
                        /*animate the footer view*/
                        val adapterCount =
                            (binding.rvNotification.adapter as NotificationAdapter).itemCount
                        (binding.rvNotification.adapter as NotificationAdapter).notifyItemInserted(
                            adapterCount - 1
                        )
                        (binding.rvNotification.adapter as NotificationAdapter).addLoadingFooter()

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

    override fun onBackPressed() {
        super.onBackPressed()
        if (mIsNotification) {
            if (prefs.readString(AppConstants.KEY_USER_TYPE).toInt() == 1) {
                startActivity(ManagerHomeActivity.newInstance(this))
                AppUtils.startFromRightToLeft(this)
                finishAffinity()
            } else {
                startActivity(ValetHomeActivity.newInstance(this))
                AppUtils.startFromRightToLeft(this)
                finishAffinity()
            }
        }
        AppUtils.finishFromLeftToRight(this)
    }

    fun showSendToClientDialog() {
        val alert = AlertDialog.Builder(this)

        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.notification_conforamtion_dialog, null)
        val btnSubmit = alertLayout.findViewById<AppCompatTextView>(R.id.tvOk)
        val btnCancel = alertLayout.findViewById<AppCompatTextView>(R.id.tvCancel)

        alert.setView(alertLayout)
        alert.setCancelable(false)

        val dialog = alert.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        btnSubmit.setOnClickListener {
            mViewModel.callApiClearAll()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }


    }
}
