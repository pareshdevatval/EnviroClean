package com.enviroclean.base

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.enviroclean.utils.AppUtils
import com.enviroclean.R
import com.enviroclean.databinding.ToolbarBinding
import com.enviroclean.ui.activity.ManagerHomeActivity
import com.google.android.material.snackbar.Snackbar


abstract class BaseFragment<T : BaseViewModel> : Fragment() {
    fun getRepository() = (activity!!.application as EnviroClean).getRepository()

    private lateinit var mViewModel: T
     var progressDialog: Dialog? = null
    private var errorSnackbar: Snackbar? = null
    private val errorClickListener = View.OnClickListener {
        // internetErrorRetryClicked()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = getViewModel()

        mViewModel.errorMessage.observe(this, Observer { errorMessage ->
            if (errorMessage != null) showError(errorMessage) else hideError()
        })

        mViewModel.loadingVisibility.observe(this, Observer { loadingVisibility ->
            loadingVisibility?.let { if (loadingVisibility) showProgress() else hideProgress() }
        })

        mViewModel.apiErrorMessage.observe(this, Observer { apiError ->
            apiError?.let { AppUtils.showSnackBar(this.view!!, it) }
        })

    }

    private fun showError(@StringRes errorMessage: Int) {
        errorSnackbar = Snackbar.make(this.view!!, errorMessage, Snackbar.LENGTH_LONG)
        errorSnackbar?.setAction(R.string.action_retry, errorClickListener)
        errorSnackbar?.show()
    }

    private fun hideError() {
        errorSnackbar?.dismiss()
    }

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    abstract fun getViewModel(): T

    //abstract fun internetErrorRetryClicked()

    /* [START] show progress bar*/
    @SuppressLint("InflateParams")
    fun showProgress() {
        if (progressDialog == null) {
            progressDialog = context?.let { Dialog(it) }
            // dialog without title
            progressDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        }

        // inflating and seeting view of custom dialog
        val view = LayoutInflater.from(context).inflate(R.layout.app_loading_dialog, null, false)
        val imageView = view.findViewById<ImageView>(R.id.imageView2)

        // applying rotate animation
        ObjectAnimator.ofFloat(imageView, View.ROTATION, 360f, 0f).apply {
            repeatCount = ObjectAnimator.INFINITE
            duration = 1500
            interpolator = LinearInterpolator()
            start()
        }
        progressDialog?.setContentView(view)

        // setting background of dialog as transparent
        val window = progressDialog?.window
        window?.setBackgroundDrawable(
            ContextCompat.getDrawable(
                context!!, android.R.color
                    .transparent
            )
        )

        // preventing outside touch and setting cancelable false
        progressDialog?.setCancelable(false)
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.show()
    }
    /* [END] show progress bar*/

     fun hideProgress() {
        progressDialog?.dismiss()
    }

    /* [START] Check if an active internet connection is present or not*/
    /* return boolean value true or false */
    fun isInternetAvailable(): Boolean {
        // getting Connectivity service as ConnectivityManager
        return AppUtils.hasInternet(context!!)
    }



    fun replaceFragment(containerId: Int, fragment: Fragment, addToBackStack: Boolean) {
        (activity as BaseActivity<*>).replaceFragment(containerId, fragment, addToBackStack)
    }

    fun addFragment(containerId: Int, fragment: Fragment, addToBackStack: Boolean) {
        (activity as BaseActivity<*>).addFragment(containerId, fragment, addToBackStack)
    }
    /*show toolbar title*/
    fun setToolbarBackground(bgColor: Int?) {
        (activity as BaseActivity<*>).setToolbarBackground(bgColor)
    }

    fun setToolbarTitle(title:String) {
        (activity as BaseActivity<*>).setToolbarTitle(title)
    }
    fun setToolbarTitle(title:Int) {
        (activity as BaseActivity<*>).setToolbarTitle(title)
    }

    /*Toolbar right menu icon and its click listener*/
    fun setToolbarRightMenuIcon(view : ToolbarBinding, drawable: Int, listener: BaseActivity.ToolbarRightMenuClickListener?) {

        view.ivRight.setImageResource(drawable)
        view.ivRight.setOnClickListener { listener?.onRightMenuClicked() }
    }

    /*Toolbar right1 menu icon and its click listener*/
    fun setToolbarRight1MenuIcon(view :ToolbarBinding,drawable: Int, listener: BaseActivity.ToolbarRight1MenuClickListener?) {
        view.ivRight1.setImageResource(drawable)
        view.ivRight1.setOnClickListener { listener?.onRight1MenuClicked() }
    }

    fun setNotificationCount(view:ToolbarBinding,count:String){

        val refresh = Handler(Looper.getMainLooper())
        refresh.post(Runnable {
            if (count!="0"&&count.isNotEmpty()){
                AppUtils.doBounceAnimation(view.ivRight1,context!!)
                view.tvNotificationCount.text=count
                view.tvNotificationCount.visibility=View.VISIBLE
            }else{
                view.tvNotificationCount.visibility=View.GONE
            }
        })
    }

    fun setToolbarLeftIcon(view :ToolbarBinding,drawable: Int, listener: BaseActivity.ToolbarLeftMenuClickListener?) {
        view.ivLeft.setImageResource(drawable)
        view.ivLeft.setOnClickListener { listener?.onLeftIconClicked() }
    }
    fun onBackPressed() {

        AppUtils.webSocket.cancel()
        AppUtils.client!!.dispatcher.executorService.shutdown()
        ManagerHomeActivity.socketConnectedManager = false
        val fm = activity!!.supportFragmentManager
        fm.popBackStack()
    }
}