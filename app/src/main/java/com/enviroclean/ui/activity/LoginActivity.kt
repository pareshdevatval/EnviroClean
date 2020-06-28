package com.enviroclean.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.core.app.ActivityOptionsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.enviroclean.R
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityLoginBinding
import com.enviroclean.model.LoginResponse1
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.utils.validator.ValidationErrorModel
import com.enviroclean.viewmodel.LoginViewModel


class LoginActivity : BaseActivity<LoginViewModel>(LoginActivity::class.java.simpleName) {
    private lateinit var binding: ActivityLoginBinding
    private var isShowPassword = false
    private val mViewModel: LoginViewModel by lazy {
        ViewModelProviders.of(this).get(LoginViewModel::class.java)
    }
    lateinit var prefs: Prefs
    override fun getViewModel(): LoginViewModel {
        return mViewModel
    }

    /*A static method to gtv_exempt_accountenerate an intent of this activity
    * So there is only one method to call this activity throughout the app.
    * Hence we can easily find out all the required values to statr this activity*/
    companion object {
        fun newInstance(context: Context, backStackClear: Boolean): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            if (backStackClear) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        prefs= Prefs.getInstance(this)!!
        binding.loginActivity = this
        prefs.isTutorialIn=true
        init()
        setObserver()
        setHideShowPassword()
    }


    private fun init() {
        mViewModel.checkRememberMe(prefs)
        mViewModel.getResponse().observe(this, observeLogin)

    }

    private val observeLogin = Observer<LoginResponse1> {
        if(it.status&&it.code==AppConstants.SUCCESSFULLY_CODE){
            Log.e("PASSWORD------->","---->"+binding.edtPassword.text)
            prefs.currentPassword=binding.edtPassword.text.toString()
            prefs.userEmail=binding.edtEmailPhone.text.toString()
            prefs.userPassword=binding.edtPassword.text.toString()
            prefs.isLoggedIn=true
            prefs.userDataModel=it
            prefs.accessToken=it.logindata.token
            prefs.notificationCount=it.logindata.unreadNotificationCount.toString()
            prefs.writeString(AppConstants.KEY_USER_TYPE, it.logindata.uType.toString())
            if (it.logindata.uType == 1) {
                if(it.logindata.checkinFlag==1){
                    prefs.isCheck=true
                    prefs.checkingResponse=it
                }else{
                    prefs.isCheck=false
                }
                startActivity(ManagerHomeActivity.newInstance(this))
                AppUtils.startFromRightToLeft(this)
                Log.e("DONEE","---->doneeeeeeeee")
                finishAffinity()
            } else {
                if(it.logindata.checkinFlag==1){
                    prefs.isCheck=true
                    prefs.checkingResponse=it
                }else{
                    prefs.isCheck=false
                }
                startActivity(ValetHomeActivity.newInstance(this))
                AppUtils.startFromRightToLeft(this)
                finishAffinity()

            }

        }
    }

    private fun setObserver() {
        mViewModel.getValidationError()
            .observe({ this.lifecycle }, { validateResponse: ValidationErrorModel ->
                AppUtils.showSnackBar(binding.root, getString(validateResponse.msg))
            })

        mViewModel.getRememberMe().observe({ this.lifecycle }, { rememberme: Boolean ->
            binding.chbRememberMe.isChecked = rememberme
            if (rememberme) {
                binding.edtEmailPhone.setText(prefs.emailOrPhone)
                binding.edtPassword.setText(prefs.password)
            } else {
                binding.edtEmailPhone.setText("")
                binding.edtPassword.setText("")
            }

        })

        mViewModel.getLoginResponse().observe({ this.lifecycle }, { loginResponse: Boolean? ->
            if (loginResponse!!) {

                AppUtils.showSnackBar(binding.root, "Login Success")
            }
        })
    }

    fun onSignInClick() {
        val isRememberMe = binding.chbRememberMe.isChecked
        mViewModel.validateUserInputs(
            prefs,
            binding.edtEmailPhone.text.toString(),
            binding.edtPassword.text.toString()
            , isRememberMe
        )
    }

    fun onForgotPasswordClick() {
        val p1 = androidx.core.util.Pair(binding.edtEmailPhone as View , "text")
        val options =
            ActivityOptionsCompat.makeSceneTransitionAnimation(this, p1)
        startActivity(ForgotPasswordActivity.newInstance(this,binding.edtEmailPhone.text.toString()), options.toBundle())

    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setHideShowPassword() {
        binding.edtPassword.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {

                val DRAWABLE_RIGHT = 2


                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= binding.edtPassword.right - binding.edtPassword.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        if (isShowPassword) {
                            binding.edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                                getDrawable(R.drawable.ic_password),
                                null,
                                getDrawable(R.drawable.ic_show_password),
                                null
                            )
                            isShowPassword = false
                            binding.edtPassword.clearFocus()
                            binding.edtPassword.inputType =
                                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            binding.edtPassword.setSelection(binding.edtPassword.text!!.length)
                            binding.edtPassword.setTextAppearance(
                                this@LoginActivity,
                                R.style.editTextStyle
                            )
                        } else {
                            binding.edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                                getDrawable(R.drawable.ic_password),
                                null,
                                getDrawable(R.drawable.ic_hide_password),
                                null
                            )
                            isShowPassword = true
                            binding.edtPassword.clearFocus()
                            binding.edtPassword.inputType =
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            binding.edtPassword.setSelection(binding.edtPassword.text!!.length)
                            binding.edtPassword.setTextAppearance(
                                this@LoginActivity,
                                R.style.editTextStyle
                            )

                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }
}
