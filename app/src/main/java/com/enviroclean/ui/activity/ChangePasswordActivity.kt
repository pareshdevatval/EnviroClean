package com.enviroclean.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.enviroclean.R
import com.enviroclean.api.BaseResponse
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityChangePasswordBinding
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.utils.validator.ValidationErrorModel
import com.enviroclean.viewmodel.ChangePasswordViewModel

class ChangePasswordActivity :
    BaseActivity<ChangePasswordViewModel>(ChangePasswordActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener {

    override fun onLeftIconClicked() {
        onBackPressed()
    }

    private lateinit var binding: ActivityChangePasswordBinding
    private val mViewModel: ChangePasswordViewModel by lazy {
        ViewModelProviders.of(this).get(ChangePasswordViewModel::class.java)
    }

    override fun getViewModel(): ChangePasswordViewModel {
        return mViewModel
    }
    lateinit var prefs: Prefs
    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, ChangePasswordActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        binding.changePassword = this
        prefs= Prefs.getInstance(this)!!
        init()
        initToolbar()
        setObserver()
    }

    private fun init() {
        mViewModel.getResponse().observe(this, observerChangePassword)
    }

    private val observerChangePassword = Observer<BaseResponse> {
        if (it.status){
            prefs.currentPassword=binding.edtNewPassword.text.toString()
            prefs.userPassword=binding.edtNewPassword.text.toString()
            prefs.password=binding.edtNewPassword.text.toString()
            AppUtils.showToast(this,it.message)
            finish()
        }
    }

    private fun initToolbar() {
        setToolbarBackground(R.color.blue_toolbar)
        setToolbarTitle(R.string.lbl_change_password)
        setToolbarLeftIcon(R.drawable.ic_back_white, this)
    }

    private fun setObserver() {
        mViewModel.getValidationError()
            .observe({ this.lifecycle }, { validationResponse: ValidationErrorModel? ->
                AppUtils.showSnackBar(binding.root, getString(validationResponse!!.msg))
            })
        mViewModel.getChangePwdResponse()
            .observe({ this.lifecycle }, { changePwdResponse: Boolean? ->
                if (changePwdResponse!!) {
                    if(binding.edtCurrentPassword.text.toString()==binding.edtNewPassword.text.toString()){
                        AppUtils.showSnackBar(binding.btnSubmit,"New password and current password is same")
                        return@observe
                    }
                      val param:HashMap<String,Any?> = HashMap()
                      param[ApiParams.CURRENT_PASSWORD]=binding.edtCurrentPassword.text.toString()
                      param[ApiParams.NEW_PASSWORD]=binding.edtNewPassword.text.toString()
                      param[ApiParams.CONFIRM_PASSWORD]=binding.edtConfirmPassword.text.toString()
                      param[ApiParams.DEVICE_TYPE]=AppConstants.ANDROID_DEVICE_TYPE
                        mViewModel.callApi(param)

                }
            })
    }

    fun onClickSubmit() {
        mViewModel.checkValidation(
            binding.edtCurrentPassword.text.toString(),
            binding.edtNewPassword.text.toString(),
            binding.edtConfirmPassword.text.toString()
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }
}
