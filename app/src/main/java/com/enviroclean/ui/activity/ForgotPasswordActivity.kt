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
import com.enviroclean.databinding.ActivityForgotPasswordBinding
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.validator.ValidationErrorModel
import com.enviroclean.viewmodel.ForgotPasswordViewModel

class ForgotPasswordActivity :
    BaseActivity<ForgotPasswordViewModel>(ForgotPasswordActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener {
    override fun onLeftIconClicked() {
        onBackPressed()
    }

    private lateinit var binding: ActivityForgotPasswordBinding

    private val mViewModel: ForgotPasswordViewModel by lazy {
        ViewModelProviders.of(this).get(ForgotPasswordViewModel::class.java)
    }

    override fun getViewModel(): ForgotPasswordViewModel {
        return mViewModel
    }
    val mEmail :String by lazy {
        intent.getStringExtra(AppConstants.FORGOTE_EMAIL)
    }

    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    companion object {
        fun newInstance(context: Context, email: String): Intent {
            val intent = Intent(context, ForgotPasswordActivity::class.java)
            intent.putExtra(AppConstants.FORGOTE_EMAIL,email)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)
        binding.forgotPassword = this
        initToolBar()
        setObserver()
        inti()
    }

    private fun inti() {
        binding.edtEmailPhone.setText(mEmail)
        mViewModel.getResponse().observe(this, observeForgotePassword)

    }

    private val observeForgotePassword = Observer<BaseResponse> {
        if (it.status) {
            AppUtils.showSnackBar(binding.btnSend, it.message)
        }
    }

    private fun initToolBar() {
        setToolbarBackground(R.color.transprent)
        setToolbarLeftIcon(R.drawable.ic_back_black, this)

    }

    fun onSendClick() {
        mViewModel.validateUserInputs(binding.edtEmailPhone.text.toString())
    }

    private fun setObserver() {

        mViewModel.getValidationError()
            .observe({ this.lifecycle }, { validarionResponse: ValidationErrorModel? ->
                AppUtils.showSnackBar(binding.root, getString(validarionResponse!!.msg))
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }
}
