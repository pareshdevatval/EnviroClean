package com.enviroclean.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.enviroclean.R
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityFilterBinding
import com.enviroclean.model.AreaModel
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.ChangePasswordViewModel
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_filter.*
import android.R.attr.transitionName
import android.app.ActivityOptions
import android.app.ActivityOptions.makeSceneTransitionAnimation
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class FilterActivity :
    BaseActivity<ChangePasswordViewModel>(FilterActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener {
    override fun onLeftIconClicked() {
        onBackPressed()
    }

    private lateinit var binding: ActivityFilterBinding
    private val mViewModel: ChangePasswordViewModel by lazy {
        ViewModelProviders.of(this).get(ChangePasswordViewModel::class.java)
    }

    override fun getViewModel(): ChangePasswordViewModel {
        return mViewModel
    }

    lateinit var prefs: Prefs

    private val mSelectedAreaList: ArrayList<String> by lazy {
        intent.getStringArrayListExtra(AppConstants.SELECTED_AREA_LIST_ID)
    }
    private val mSelectedViolationReason: String by lazy {
        intent.getStringExtra(AppConstants.SELECTED_VIOLATION_REASON_ID)
    }
    private val mSelectedAreaListName: ArrayList<String> by lazy {
        intent.getStringArrayListExtra(AppConstants.SELECTED_AREA_LIST_NAME)
    }

    /*A static method to generate an intent of this activity
   * So there is only one method to call this activity throughout the app.
   * Hence we can easily find out all the required values to statr this activity*/
    companion object {
        fun newInstance(
            context: Context, selectedArealistId: ArrayList<String>,
            selectedArealistName: ArrayList<String>, violationResionId: String,violationReasonName:String
        ): Intent {
            val intent = Intent(context, FilterActivity::class.java)
            intent.putExtra(AppConstants.SELECTED_AREA_LIST_ID, selectedArealistId)
            intent.putExtra(AppConstants.SELECTED_AREA_LIST_NAME, selectedArealistName)
            intent.putExtra(AppConstants.SELECTED_VIOLATION_REASON_ID, violationResionId)
            intent.putExtra(AppConstants.SELECTED_VIOLATION_REASON_NAME, violationReasonName)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_filter)

        prefs = Prefs.getInstance(this)!!
        init()
        initToolbar()
    }

    private fun init() {

        setAreaName()
        setViolationReason()
    }

    val selectedAreaListId: ArrayList<String> = ArrayList()
    val selectedAreaListName: ArrayList<String> = ArrayList()
    val selectedViolatioReason: ArrayList<String> = ArrayList()
    val selectedViolatioReasonName: ArrayList<String> = ArrayList()
    private fun initToolbar() {
        setToolbarBackground(R.color.blue_toolbar)
        setToolbarTitle(R.string.lbl_filter_options)
        setToolbarRightTextTitle(R.string.lbl_apply)
        setToolbarLeftTextTitle(R.string.lbl_cancel)

        binding.toolbar.tvToolbarRightTxtMenu.setOnClickListener {
            selectedAreaListId.clear()
            selectedAreaListName.clear()
            selectedViolatioReason.clear()
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                if (chip.isChecked) {
                    selectedAreaListId.add("" + chip.id)
                    selectedAreaListName.add(chip.text.toString())

                }
            }
            for (i in 0 until binding.radioGroupAds.childCount) {
                val radioButton = binding.radioGroupAds.getChildAt(i) as AppCompatRadioButton
                if (radioButton.isChecked) {
                    selectedViolatioReason.add("" + radioButton.id)
                    selectedViolatioReasonName.add("" + radioButton.text.toString())
                }
            }

            Log.e("AREA_NAME", "" + selectedAreaListId)
            Log.e("VIOLATION_REASON", "" + selectedViolatioReason)
            val returnIntent = Intent()
            returnIntent.putExtra("selectedAraListId", selectedAreaListId)
            returnIntent.putExtra("selectedAraListName", selectedAreaListName)
            returnIntent.putExtra("selectedReason", selectedViolatioReason[0])
            returnIntent.putExtra("selectedReasonName", selectedViolatioReasonName[0])
            setResult(Activity.RESULT_OK, returnIntent)

            finish()
            AppUtils.finishFromLeftToRight(this)
        }
        binding.toolbar.tvToolbarLiftTxtMenu.setOnClickListener {
            onBackPressed()
        }
    }

    val areaList: ArrayList<AreaModel> = ArrayList()
    val violstionRasonList: ArrayList<AreaModel> = ArrayList()

    @SuppressLint("ResourceType")
    var isCheck=true

    fun setAreaName() {

        areaList.clear()
        areaList.add(AreaModel("All", 0))
        val areaListResult=prefs.areaList!!.areas
        for(i in 0 until areaListResult.size){
            areaList.add(AreaModel(areaListResult[i]!!.area_name,areaListResult[i]!!.area_id))
        }
        for (category in areaList) {

            val mChip =
                this.layoutInflater.inflate(R.layout.chip_view_item, null, false) as Chip
            mChip.text = category.name
            mChip.id = category.id!!
            if(mSelectedAreaList.isNotEmpty()){
                for (i in 0 until mSelectedAreaListName.size) {

                    if (category.name==(mSelectedAreaListName[i])) {
                        mChip.isChecked = true
                        if(mChip.id==0){
                            isCheck=true
                        }else{
                            isCheck=false
                        }

                    }
                }

            }
            else{
                if(mChip.id==0){
                    mChip.isChecked=true
                }
            }

            mChip.setOnCheckedChangeListener { _, b ->

                if (mChip.id == 0) {
                    if(!isCheck){
                        binding.chipGroup.clearCheck()
                        isCheck=true
                        mChip.isChecked=true
                    }
                }else{
                    if(isCheck && mChip.id != 0){
                        binding.chipGroup.clearCheck()
                        mChip.isChecked=true
                        isCheck=false
                    }
                }

            }
            binding.chipGroup.addView(mChip)

        }
    }

    fun setViolationReason() {
        violstionRasonList.clear()
        violstionRasonList.add(0,AreaModel("All", 0))
        val violationListResult=prefs.userDataModel!!.managerReason
        val violationListResultValtes=prefs.userDataModel!!.valetReason
        for(i in 0 until violationListResult.size ){
            violstionRasonList.add(i+1,AreaModel(violationListResult[i]!!.reasonName,violationListResult[i]!!.reasonId.toInt()))
        }
        for(i in 0 until violationListResultValtes.size){
            violstionRasonList.add(i+1,AreaModel(violationListResultValtes[i]!!.reasonName,violationListResultValtes[i]!!.reasonId.toInt()))
        }
        val set = LinkedHashSet(violstionRasonList)
        violstionRasonList.clear()
        violstionRasonList.addAll(set)
        for (category in violstionRasonList) {

            val radioButton =
                this.layoutInflater.inflate(
                    R.layout.violation_reason_list,
                    null,
                    false
                ) as AppCompatRadioButton
            radioButton.text = category.name
            radioButton.id = category.id!!
            if(mSelectedViolationReason.isNotEmpty()){
                for (i in 0 until violstionRasonList.size) {
                    if (category.id.toString()==mSelectedViolationReason) {
                        radioButton.isChecked = true
                    }
                }
            }else{
                if(radioButton.id==0){
                    radioButton.isChecked=true
                }
            }



            radioButton.setOnCheckedChangeListener { _, b ->

                if (radioButton.id == 0) {
                    binding.radioGroupAds.clearCheck()
                }
            }
            binding.radioGroupAds.addView(radioButton)

        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }
}
