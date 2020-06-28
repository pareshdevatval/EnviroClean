package com.enviroclean.base

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.enviroclean.utils.AppUtils
import com.enviroclean.R
import com.enviroclean.databinding.ToolbarBinding
import com.google.android.material.snackbar.Snackbar

/*A common Activity class that all activities of app will extend
* Common operation of all activities will go in this class
* like all customization of toolbar
*
* accepts
* @tag = tag for message logging in logcat for every activity
* */
@SuppressLint("Registered")
abstract class BaseActivity<T : BaseViewModel>(tag: String): AppCompatActivity() {
    /*TAG to log the messages on logcat
    * It would basically mInputCharacter name of the activity of fragment
    * printing TAG of activity/fragment in log is mInputCharacter best practice for debug
    * When app becames complicated in structure and when one API is called from multiple screens
    * at that time this TAG helps us to quickly debug*/
    val TAG = tag

    private lateinit var baseViewModel: T
    var activity: Activity?=null
    /*An abstract method that all the sub-activities will implement and return the respective viewModel*/
    abstract fun getViewModel(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseViewModel = getViewModel()

        /*Observing progress bar to show and hide throughout all the activities and fragments*/
        baseViewModel.loadingVisibility.observe(this, Observer { loadingVisibility ->
            loadingVisibility?.let { if (loadingVisibility) showProgress() else hideProgress() }
        })

        /*Observing error message to show throughout all the activities and fragments*/
        baseViewModel.errorMessage.observe(this, Observer { errorMessage ->
            if (errorMessage != null) showError(errorMessage) else hideError()
        })

        /*Observing api error message to show throughout all the activities and fragments*/
        baseViewModel.apiErrorMessage.observe(this, Observer { apiError ->
            apiError?.let { AppUtils.showSnackBar(getRootView(), it) }
        })

        observeToolbarMenuState().observe(this, Observer{ showIconsMenu ->
            if(showIconsMenu) {
                toolbarBinding.grMenuIcons.visibility = View.VISIBLE
                toolbarBinding.tvToolbarRightTxtMenu.visibility = View.GONE
            } else {
                toolbarBinding.grMenuIcons.visibility = View.GONE
                toolbarBinding.tvToolbarRightTxtMenu.visibility = View.VISIBLE
            }
        })

    }

    /*OnCreate of BaseActivity gets called before the onCreate of any sub-activity
    * Hence, If we declared the toolbarBinding as "lateinint var",
    * then it will crash as this will not be able to find the binding layout for toolbar
    * before the onCreate of sub-activity called.
    *
    * by lazy will initialize the variable on the first time when compiler try to use this variable
    * So in that time onCreate of sub-activity would already be called
    * and we will not face any issues*/
    private val toolbarBinding: ToolbarBinding by lazy {
        DataBindingUtil.bind<ToolbarBinding>(findViewById(R.id.toolbar)) as ToolbarBinding
    }

    /*The appTheme color background is set by default from xml file
    * Call this method only if you want any other color for toolbar
    * Otherwise no need to call this method*/
    fun setToolbarBackground(bgColor: Int?) {
        bgColor?.let {
            toolbarBinding.toolbarLayout.setBackgroundResource(it)
        }
    }

    @SuppressLint("NewApi")
    fun setToolbarTitle(title: Int?) {
        title?.let {
            toolbarBinding.tvToolbarTitle.text = getString(it)
        }
    }

    @SuppressLint("NewApi")
    fun setToolbarRightTextTitle(title: Int?) {
        title?.let {
            toolbarBinding.tvToolbarRightTxtMenu.visibility=View.VISIBLE
            toolbarBinding.tvToolbarRightTxtMenu.text = getString(it)
        }
    }
    @SuppressLint("NewApi")
    fun setToolbarLeftTextTitle(title: Int?) {
        title?.let {
            toolbarBinding.tvToolbarLiftTxtMenu.visibility=View.VISIBLE
            toolbarBinding.tvToolbarLiftTxtMenu.text = getString(it)
        }
    }

     fun setToolbarTitle(title: String?) {
        title?.let {
            toolbarBinding.tvToolbarTitle.text = it
        }
    }

    /*Toolbar left icon and its click listenr
    * We know most of the times left icon would be mInputCharacter back icon and thats why we have written it as default
    * So we do not need to pass that every time*/
    fun setToolbarLeftIcon(drawable: Int = R.drawable.ic_back_black, listener: ToolbarLeftMenuClickListener?) {
        toolbarBinding.ivLeft.setImageResource(drawable)
        toolbarBinding.ivLeft.setOnClickListener { listener?.onLeftIconClicked() }

    }

    interface ToolbarLeftMenuClickListener {
        fun onLeftIconClicked()
    }

    /*Toolbar right menu icon and its click listener*/
    fun setToolbarRightMenuIcon(drawable: Int, listener: ToolbarRightMenuClickListener?) {
        toolbarBinding.ivRight.setImageResource(drawable)
        showIconsMenu.value = true
        toolbarBinding.ivRight.setOnClickListener { listener?.onRightMenuClicked() }
    }

    interface ToolbarRightMenuClickListener {
        fun onRightMenuClicked()
    }

    /*Toolbar right1 menu icon and its click listener*/
    fun setToolbarRight1MenuIcon(drawable: Int, listener: ToolbarRight1MenuClickListener?) {
        toolbarBinding.ivRight1.setImageResource(drawable)
        showIconsMenu.value = true
        toolbarBinding.ivRight1.setOnClickListener { listener?.onRight1MenuClicked() }
    }

    interface ToolbarRight1MenuClickListener {
        fun onRight1MenuClicked()
    }

    /*Toolbar right1 menu icon and its click listener*/
    fun setToolbarTextMenu(menuName: String, listener: ToolbarRightMenuClickListener?) {
        toolbarBinding.tvToolbarRightTxtMenu.text = menuName
        showIconsMenu.value = false
        toolbarBinding.tvToolbarRightTxtMenu.setOnClickListener { listener?.onRightMenuClicked() }

    }

    /*LiveData boolean variable to toggle icons and text menu on toolbar*/
    private val showIconsMenu: MutableLiveData<Boolean>by lazy {
        MutableLiveData<Boolean>()
    }

    private fun observeToolbarMenuState(): LiveData<Boolean> {
        return showIconsMenu
    }

    /*[START] Error Snackbar and its Button listener*/
    private var errorSnackbar: Snackbar? = null
    //private val errorClickListener = View.OnClickListener { errorRetryClicked() }

    private fun showError(@StringRes errorMessage: Int) {
        errorSnackbar = Snackbar.make(getRootView(), errorMessage, Snackbar.LENGTH_LONG)
        //errorSnackbar?.setAction(R.string.action_retry, errorClickListener)
        errorSnackbar?.show()
    }

    private fun hideError() {
        errorSnackbar?.dismiss()
    }

     fun getRootView(): View {
        val contentViewGroup = findViewById<View>(android.R.id.content) as ViewGroup
        var rootView = contentViewGroup.getChildAt(0)
        if (rootView == null) rootView = window.decorView.rootView
        return rootView!!
    }

    //abstract fun errorRetryClicked()
    /*[END] Error Snackbar and its Button listener*/

    /*[START] Showing progress bar throughout the app*/
     var progressDialog: Dialog? = null
    /**
     * A method to show progress dialog on center of screen during api calls
     */
     fun showProgress() {
        if (progressDialog == null) {
            progressDialog = Dialog(this)
        }/* else {
            return
        }*/
        val view = LayoutInflater.from(this).inflate(R.layout.app_loading_dialog, null, false)
        val imageView1 = view.findViewById<ImageView>(R.id.imageView2)
        val a1 = AnimationUtils.loadAnimation(this, R.anim.progress_anim)
        a1.duration = 1500
        imageView1.startAnimation(a1)

        progressDialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        progressDialog?.setContentView(view)
        val window = progressDialog?.window
        window?.setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.transparent))
        progressDialog?.setCancelable(false)
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.show()
    }

    /**
     * A method to hide the progress dialog
     */
     fun hideProgress() {
        if (progressDialog != null) {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }
    /*[END] Showing progress bar throughout the app*/


    /* Accepts
* containerId : Frame Layout id to replace the fragment. Different activities may have different id for
* the container. Hence this id is needed
* fragment : Fragment to replace
* addToBackStack : Want to add to backstack or not
* */
    fun replaceFragment(containerId: Int, fragment: Fragment, addToBackStack: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
       // supportFragmentManager.beginTransaction().remove(fragment).commit()
        /* We are showing the shared elements transitions from API version 21
        * And if any fragment has not any shared elements.
        * Then also we will not show custom animations to change mInputCharacter fragment.
        * Becuase then our app will have mInputCharacter mix of animations. And we do not want that
        * We want to use single type of animations throughout the app
        * and Hence showing custom animations of below API version 21(Where we can not use shared element transitions)
        * */
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            *//*setting mInputCharacter custom animation to fragment transaction
            new fragment will come from right to left and
            an older fragment will remove from left*//*
            fragmentTransaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out,
                R.anim.slide_left_in, R.anim.slide_right_out)
        }*/
        // replacing mInputCharacter current fragment
        fragmentTransaction.replace(containerId, fragment)
        // Add current fragment to backstack or not
        if (addToBackStack)
            fragmentTransaction.addToBackStack(fragment.javaClass.name)
        // commiting the transaction
        fragmentTransaction.commitNow()
    }

    fun addFragment(containerId: Int, fragment: Fragment, addToBackStack: Boolean) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()

        if (fragment.isAdded) {
            val currentFragment = supportFragmentManager.findFragmentById(containerId)
            fragmentTransaction.show(fragment)
            fragmentTransaction.hide(currentFragment!!)
            fragmentTransaction.commit()
            return
        }
        /* We are showing the shared elements transitions from API version 21
        * And if any fragment has not any shared elements.
        * Then also we will not show custom animations to change mInputCharacter fragment.
        * Becuase then our app will have mInputCharacter mix of animations. And we do not want that
        * We want to use single type of animations throughout the app
        * and Hence showing custom animations of below API version 21(Where we can not use shared element transitions)
        * */
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            *//*setting mInputCharacter custom animation to fragment transaction
            new fragment will come from right to left and
            an older fragment will remove from left*//*
            fragmentTransaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out,
                R.anim.slide_left_in, R.anim.slide_right_out)
        }*/
        // replacing mInputCharacter current fragment
        fragmentTransaction.add(containerId, fragment)
        // Add current fragment to backstack or not
        if (addToBackStack)
            fragmentTransaction.addToBackStack(fragment.javaClass.name)
        // commiting the transaction
        fragmentTransaction.commit()
    }

    fun transparentStatusBar(transparent: Boolean) {

        val window = window
        if (transparent) {
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.blue_toolbar)
        }

    }

    fun setStatusbarColor(color: Int) {
        greyIcons(color)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = color

    }
    fun greyIcons(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (window.decorView != null) {
                if (color == -1) {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    window.decorView.systemUiVisibility = 0
                }
            }
        }
    }
}