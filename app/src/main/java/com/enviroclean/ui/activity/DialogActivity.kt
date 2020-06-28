package com.enviroclean.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.enviroclean.R
import com.enviroclean.utils.AppConstants
import kotlinx.android.synthetic.main.custom_dialog.*
import kotlinx.android.synthetic.main.fragment_tutorial.tvTitle

/**
 * Created by imobdev-paresh on 23,December,2019
 */
class DialogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_dialog)
        title = ""
        btnOk.setOnClickListener {
            finish()
        }
        tvMsg.text=mMessage
        tvTitle.text=mTitle
    }
    private val mMessage: String by lazy {
        intent.getStringExtra(AppConstants.MESSAGES_FAREBASE)
    }
    private val mTitle: String by lazy {
        intent.getStringExtra(AppConstants.TITLE_FAREBASE)
    }
    companion object {
        fun newInstant(context: Context,msg:String,title:String): Intent {
            val intent = Intent(context, DialogActivity::class.java)
            intent.putExtra(AppConstants.MESSAGES_FAREBASE,msg)
            intent.putExtra(AppConstants.TITLE_FAREBASE,title)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }


}