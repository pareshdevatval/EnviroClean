package com.enviroclean.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.enviroclean.R
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityEditProfileBinding
import com.enviroclean.databinding.DialogImageSelectionBinding
import com.enviroclean.model.UserDetailsResponse
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.utils.filePick.FilePickUtils
import com.enviroclean.viewmodel.EditProfileViewModel
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.material.bottomsheet.BottomSheetDialog


class EditProfileActivity :
    BaseActivity<EditProfileViewModel>(EditProfileActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener,
    View.OnClickListener {

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.btnSubmit -> {
                    if(binding.tvUserName.text.toString().isEmpty()){
                        AppUtils.showSnackBar(binding.root,getString(R.string.lbl_enter_user_name))
                        return
                    }
                    val param: HashMap<String, String?> = HashMap()
                    param[ApiParams.USER_MOBILE] = binding.edtPhone.text.toString()
                    param[ApiParams.USER_F_NAME] = binding.tvUserName.text.toString()
                    param[ApiParams.USER_EMAIL] = binding.edtEmail.text.toString()
                    if(AppUtils.mSelectedDate==""){
                        param[ApiParams.USER_DOB] = prefs.userDetails!!.result.uDob
                    }else{
                        param[ApiParams.USER_DOB] = AppUtils.mSelectedDate
                    }
                    param[ApiParams.USER_GENDER] = mSelectedGander
                    param[ApiParams.USER_ADDRESS] = binding.edtAddress.text.toString()
                    param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
                    mViewModel.callApi(param)
                }
                R.id.ivSelected -> {
                    showImageSelectionDialog()
                }
                R.id.tvSelectCamera -> {
                    filePickUtils?.requestImageCamera(
                        AppConstants.PICK_IMAGE_CAMERA_REQUEST_CODE,
                        false,
                        false
                    )
                    mImageSelectionDialog.cancel()
                }
                R.id.tvSelectGallery -> {
                    filePickUtils?.requestImageGallery(
                        AppConstants.PICK_IMAGE_GALLERY_REQUEST_CODE,
                        false,
                        false
                    )
                    mImageSelectionDialog.cancel()
                }
            }
        }
    }

    lateinit var mDOB:String

    override fun onLeftIconClicked() {
        AppUtils.hideKeyboard(this)
        onBackPressed()
    }

    private var filePickUtils: FilePickUtils? = null
    private lateinit var binding: ActivityEditProfileBinding

    private val mViewModel: EditProfileViewModel by lazy {
        ViewModelProviders.of(this).get(EditProfileViewModel::class.java)
    }

    override fun getViewModel(): EditProfileViewModel {
        return mViewModel
    }

    private lateinit var mImageSelectionDialog: BottomSheetDialog
    lateinit var mSelectedGander: String
    lateinit var prefs: Prefs

    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, EditProfileActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        prefs = Prefs.getInstance(this)!!
        initToolbar()
        inti()
    }

    private fun initToolbar() {
        setToolbarBackground(R.color.blue_toolbar)
        setToolbarTitle(R.string.lbl_edit_profile)
        setToolbarLeftIcon(R.drawable.ic_back_white, this)
    }

    private val mOnFileChoose = object : FilePickUtils.OnFileChoose {
        override fun onFileChoose(fileUri: String, requestCode: Int) {
            Glide.with(this@EditProfileActivity).load(fileUri).into(binding.ivUser)
            mViewModel.userImages = fileUri

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun inti() {
        setUserDetails()
        filePickUtils = FilePickUtils(this, mOnFileChoose)
        binding.btnSubmit.setOnClickListener(this)
        binding.ivSelected.setOnClickListener(this)
        mViewModel.getResponse().observe(this, observUpdateDetails)
        binding.edtDob.setOnClickListener {
            AppUtils.openDatePicker(this@EditProfileActivity, binding.edtDob)
        }

        binding.rbGroup.setOnCheckedChangeListener { group, checkedId ->

            if (R.id.rbMale == checkedId) {
                mSelectedGander = "1"
            } else if (R.id.rbFeMale == checkedId) {
                mSelectedGander = "2"
            } else if(R.id.transGender==checkedId){
                mSelectedGander="3"
            }
        }
    }

    private val observUpdateDetails = Observer<UserDetailsResponse> {
        prefs.userDetails=it
        val returnIntent = Intent()
        returnIntent.putExtra("result", "result")
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
        AppUtils.startFromRightToLeft(this)
        AppUtils.hideKeyboard(this)
    }

    private fun setUserDetails() {
        val userDetails = prefs.userDetails!!.result
        mSelectedGander = userDetails.uGender.toString()
        binding.tvUserName.setText(userDetails.uFirstName + " " + userDetails.uLastName)
        binding.edtEmail.setText(userDetails.uEmail)
        binding.edtPhone.setText(userDetails.uMobileNumber)
        binding.edtAddress.setText(userDetails.u_location)
        if(userDetails.uDob.isNotEmpty()){
            binding.edtDob.setText(AppUtils.convertDate(userDetails.uDob))
        }
        AppUtils.loadImages(this, binding.ivUser, userDetails.uImage, true)
        if(userDetails.uGender==1){
            binding.rbMale.isChecked=true
        }else if(userDetails.uGender==2){
            binding.rbFeMale.isChecked=true
        }else{
            binding.transGender.isChecked=true
        }
    }

    private fun showImageSelectionDialog() {
        mImageSelectionDialog = BottomSheetDialog(this)
        val dialogImageSelectionBinding = DialogImageSelectionBinding.inflate(layoutInflater)
        mImageSelectionDialog.setContentView(dialogImageSelectionBinding.root)

        dialogImageSelectionBinding.tvSelectCamera.setOnClickListener(this)
        dialogImageSelectionBinding.tvSelectGallery.setOnClickListener(this)
        mImageSelectionDialog.show()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        filePickUtils?.onActivityResult(requestCode, resultCode, intent)

    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        filePickUtils?.onRequestPermissionsResult(
            requestCode,
            permissions as Array<String>,
            grantResults
        )
    }
}
