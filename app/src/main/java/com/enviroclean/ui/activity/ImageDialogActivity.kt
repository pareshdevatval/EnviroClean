package com.enviroclean.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Matrix
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.enviroclean.R
import com.enviroclean.utils.AppConstants
import com.twilio.voice.CallInvite
import kotlinx.android.synthetic.main.custom_dialog.*
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.enviroclean.utils.TouchImageView
import kotlinx.android.synthetic.main.call_custom_dialog.*


/**
 * Created by imobdev-paresh on 23,December,2019
 */
class ImageDialogActivity : AppCompatActivity() {
    private lateinit var imageView:TouchImageView
    private var image:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.call_custom_dialog)
        imageView=findViewById(R.id.ivImage)
        imageView.setMaxZoom(4f)
        image=intent.getStringExtra("IMG")!!
        image.let {
            Glide.with(this)
                .load(it)
                .into(imageView)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }
    companion object {
        fun newInstant(context: Context,image:String): Intent {
            val intent = Intent(context, ImageDialogActivity::class.java)
            intent.putExtra("IMG",image)
            return intent
        }
    }

}