package com.enviroclean.ui.fragment.manager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.enviroclean.R
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseFragment
import com.enviroclean.databinding.FragmentManagerProfileBinding
import com.enviroclean.model.Reminder
import com.enviroclean.model.UserDetailsResponse
import com.enviroclean.ui.activity.*
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.ManagerProfileViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.layout_profile_item.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


/**
 * Created by imobdev on 20/12/19
 */
class ManagerProfileFragment : BaseFragment<ManagerProfileViewModel>() {

    private lateinit var binding: FragmentManagerProfileBinding

    private val mViewModel: ManagerProfileViewModel by lazy {
        ViewModelProviders.of(this).get(ManagerProfileViewModel::class.java)
    }

    override fun getViewModel(): ManagerProfileViewModel {
        return mViewModel
    }

    lateinit var prefs: Prefs

    companion object {
        fun newInstance(): ManagerProfileFragment {
            val bundle = Bundle()
            val fragment = ManagerProfileFragment()
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
        binding = FragmentManagerProfileBinding.inflate(inflater, container, false)

        binding.profileData = this
        prefs = this.context?.let { Prefs.getInstance(it) }!!
        init()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbar()
    }

    fun init() {
        if (prefs.userDetailsUpdate) {
            setUserProfileData(prefs.userDetails!!.result)
        } else {
            mViewModel.callApi()
        }
        mViewModel.getResponse().observe(this, observeUserDetails)
        mViewModel.getResponseLogout().observe(this, observeUserLogOut)
    }

    private val observeUserDetails = Observer<UserDetailsResponse> {
        prefs.userDetails = it
        setUserProfileData(it.result)
        prefs.userDetailsUpdate = true
    }
    private val observeUserLogOut = Observer<BaseResponse> {
        if(it.status){
            prefs.violationAreaSize=""

            for (reminder in getRepository().getAll()) {
                removeReminder(reminder)
            }
            prefs.clearPrefs()
            prefs.clearCheckinDate()
            prefs.isLoggedIn = false
            if(prefs.isTouchId){
                startActivity(FinagerPrintActivity.newInstance(context!!,true,false,true))
            }else{
                startActivity(LoginActivity.newInstance(context!!, true))
            }

            AppUtils.finishFromLeftToRight(context!!)
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUserProfileData(result: UserDetailsResponse.Result) {
        if (result.u_paystub_type==1){
            binding.clDays.visibility=View.VISIBLE
        }else{
            binding.clDays.visibility=View.GONE
        }
        binding.tvUserName.text = result.uFirstName.capitalize() + " " + result.uLastName.capitalize()
        binding.tvAmount.paintFlags = binding.tvAmount.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.tvAmount.text = "$ " + result.uTotalEarn
        binding.tvTotalHours.text = "" + result.uTotalHrs
        binding.tvAvg.text = "$ " + result.uAvgDays
        binding.tvTotalDay.text = "" + result.uTotalDays
        binding.viewEmail.findViewById<AppCompatTextView>(R.id.tvValue).text = result.uEmail
        binding.viewEmail.findViewById<AppCompatImageView>(R.id.valueImage)
            .setImageDrawable(resources.getDrawable(R.drawable.ic_mail_black))

        binding.viewMobileNo.findViewById<AppCompatTextView>(R.id.tvValue).text =
            result.uMobileNumber
        binding.viewMobileNo.findViewById<AppCompatImageView>(R.id.valueImage)
            .setImageDrawable(resources.getDrawable(R.drawable.ic_mobile_black))

        if(result.uDob.isNotEmpty()){
            binding.viewDob.visibility=View.VISIBLE
            binding.viewDob.findViewById<AppCompatTextView>(R.id.tvValue).text =
                AppUtils.convertDate(result.uDob)
        }else{
            binding.viewDob.visibility=View.GONE
        }
        binding.viewDob.findViewById<AppCompatImageView>(R.id.valueImage)
            .setImageDrawable(resources.getDrawable(R.drawable.ic_dob_black))

        binding.viewGender.findViewById<AppCompatTextView>(R.id.tvValue).text =
            if (result.uGender == 1) {
                "Male"
            } else if(result.uGender == 2) {
                "Female"
            }else{
                "Other"
            }
        binding.viewGender.findViewById<AppCompatImageView>(R.id.valueImage)
            .setImageDrawable(resources.getDrawable(R.drawable.ic_gender_black))
        context?.let {
            AppUtils.loadImages(it, binding.ivUser, result.uImage, true)
        }
        if(prefs.isTouchId){
            binding.tvEnableTouchId.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_enable_touch, 0);
        }else{
            binding.tvEnableTouchId.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_enable_touch_black, 0);
        }

    }


    fun onEditClick() {
        val p1 = androidx.core.util.Pair(binding.ivUser as View , "profile")
        val p2 = androidx.core.util.Pair(binding.tvUserName as View , "text")
        val p3 = androidx.core.util.Pair(binding.toolbar.toolbarLayout as View , "toolbar")
        val options =
            ActivityOptionsCompat.makeSceneTransitionAnimation((activity as BaseActivity<*>).activity!!, p1,p2,p3)

        startActivityForResult(EditProfileActivity.newInstance(context!!), 9002, options.toBundle())

    }

    fun onPayStubClick() {
        startActivityForResult(PayStubActivity.newInstance(context!!), 9001)
        AppUtils.startFromRightToLeft(context!!)
    }

    fun onChangesPassword() {
        startActivity(ChangePasswordActivity.newInstance(context!!))
        AppUtils.startFromRightToLeft(context!!)
    }

    fun onEnableTouchId() {
        if(prefs.isTouchId){

            startActivityForResult(FinagerPrintActivity.newInstance(context!!,false,true),9004)
            AppUtils.startFromRightToLeft(context!!)

        }else{
            startActivityForResult(FinagerPrintActivity.newInstance(context!!),9003)
            AppUtils.startFromRightToLeft(context!!)
        }

    }
    private fun setToolbar() {
        binding.toolbar.tvToolbarTitle.text = getString(R.string.lbl_my_profile)
        setToolbarRight1MenuIcon(binding.toolbar, R.drawable.ic_logout_white, object :
            BaseActivity.ToolbarRight1MenuClickListener {
            override fun onRight1MenuClicked() {

                context?.let {
                   mViewModel.logOutApi()
                }
            }
        })

    }
    private fun removeReminder(reminder: Reminder) {
        getRepository().remove(
            reminder,
            success = {
                Log.e("removeReminder","success")
            },
            failure = {
                Log.e("removeReminder","failure->"+it)
            })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 9002) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data!!.getStringExtra("result")

                setUserProfileData(prefs.userDetails!!.result)

            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        } else if (requestCode == 9001) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data!!.getStringExtra("result")
                mViewModel.callApi()
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }else if(requestCode == 9003){
            if (resultCode == Activity.RESULT_OK) {
                val result = data!!.getBooleanExtra("status",false)
                if(result){
                    prefs.isTouchId=true
                    binding.tvEnableTouchId.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_enable_touch, 0);

                }else{
                    prefs.isTouchId=false
                    binding.tvEnableTouchId.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_enable_touch_black, 0);
                }
            }
        }else if(requestCode == 9004){
            if (resultCode == Activity.RESULT_OK) {
                val result = data!!.getBooleanExtra("status",false)
                if(!result){
                    prefs.isTouchId=true
                    binding.tvEnableTouchId.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_enable_touch, 0);

                }else {
                    prefs.isTouchId = false
                    binding.tvEnableTouchId.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_enable_touch_black,
                        0
                    );
                }
            }
        }
    }
}