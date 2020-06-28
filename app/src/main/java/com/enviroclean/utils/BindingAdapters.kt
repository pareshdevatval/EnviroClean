package com.enviroclean.utils

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.enviroclean.R

import java.util.*

@BindingAdapter("adapter")
fun setAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    view.adapter = adapter
}

@BindingAdapter("mutableVisibility")
fun setMutableVisibility(view: View, visibility: MutableLiveData<Int>?) {
    val parentActivity: AppCompatActivity? = view.getParentActivity()
    if (parentActivity != null && visibility != null) {
        visibility.observe(parentActivity, androidx.lifecycle.Observer { value ->
            view.visibility = value ?: View.VISIBLE
        })
    }
}

@BindingAdapter("mutableText")
fun setMutableText(view: TextView, text: MutableLiveData<String>?) {
    val parentActivity: AppCompatActivity? = view.getParentActivity()
    if (parentActivity != null && text != null) {
        text.observe(parentActivity, androidx.lifecycle.Observer { value -> view.text = value ?: "" })
    }
}


/**
 * Sets an Image to an ImageView
 * @param view the ImageView on which to set the Image
 * @param url the url to get the image and set to the ImageView
 */
@BindingAdapter("imageUrl")
fun loadImageUrl(view: ImageView, url: String) {
    Glide.with(view.context).load(url).apply(RequestOptions()
            .error(R.mipmap.ic_launcher)
            .placeholder(R.mipmap.ic_launcher)
            .centerCrop())
            .into(view)
}

@BindingAdapter("imageUrl")
fun loadImageUrl(view: ImageView, resourceId: Int) {
    Glide.with(view.context).load(resourceId).apply(
        RequestOptions()
            .error(R.mipmap.ic_launcher)
    )
        .into(view)
}

/**
 * Sets an Image to an ImageView
 * @param view the ImageView on which to set the Image
 * @param url the url to get the image and set to the ImageView
 * @param drawable the drawable image which you want to load as placeholder
 */
@BindingAdapter("imageUrl", "imageDefault")
fun loadImage(view: ImageView, url: String?, drawable: Drawable) {
    if (url != null) {
        Glide.with(view.context).load(url).apply(RequestOptions()
                .error(drawable)
                .placeholder(drawable)
                .centerCrop())
                .into(view)
    }
}


/**
 * Sets an Image to an ImageView
 * @param view the ImageView on which to set the Image
 * @param url the url to get the image and set to the ImageView
 * @param drawable the resource id which you want to load as placeholder
 */
@BindingAdapter("imageUrl", "imageDefault")
fun loadImage(view: ImageView, url: String, resId: Int) {
    var drawable = ContextCompat.getDrawable(view.context, resId)
    Glide.with(view.context).load(url).apply(RequestOptions()
            .error(drawable)
            .placeholder(drawable)
            .centerCrop())
            .into(view)
}

/*@BindingAdapter("isRequestedFlag", "status")
fun setStatus(view: TextView, isRequested: Boolean, status: Int) {
    if (!isRequested && status == RequestStatus.PENDING) {
        view.visibility = View.GONE
    } else {
        view.visibility = View.VISIBLE
        val pair = AppUtils.getRequestStatus(view.context, status)
        view.text = pair.first
        view.setTextColor(pair.second)
    }
}

@BindingAdapter("isRequested", "statusValue")
fun setGroupVisibility(view: View, isRequested: Boolean, status: Int) {
    if (!isRequested && status == RequestStatus.PENDING) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}*/

@BindingAdapter("date")
fun setDate(view: TextView, date: String) {
    if (date.isNotEmpty()) {
        view.text = AppUtils.formatDate(Date(date.toLong() * 1000), "dd MMM")
    }
}


/*
@BindingAdapter("requestType", "status")
fun setFriendStatus(view: TextView, requestType: Int, status: Int) {
    if (requestType == RequestType.RECEIVED_REQUEST.toInt() && status == RequestStatus.PENDING) {
        view.visibility = View.GONE
    } else {
        view.visibility = View.VISIBLE
        val pair = AppUtils.getRequestStatus(view.context, status)
        view.text = pair.first
        view.setTextColor(pair.second)
    }
}

@BindingAdapter("requestType", "statusValue")
fun setFriendGroupVisibility(view: View, requestType: Int, status: Int) {
    if (requestType == 1 && status == RequestStatus.PENDING) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("block", "isRequested", "requestType", "statusValue")
fun setFriendGroupVisibility(view: View, btnBlock: Button, isRequested: Boolean, requestType: Int,
                             status: Int) {
    view.visibility = View.GONE
    btnBlock.visibility = View.GONE
    if (isRequested) { // Is "Request" List
        if (requestType == RequestType.RECEIVED_REQUEST.toInt()) {
            btnBlock.visibility = View.VISIBLE
            if (status == RequestStatus.PENDING) {
                view.visibility = View.VISIBLE
            }
        }
    } else { // Is "Friend" List
        btnBlock.visibility = View.VISIBLE
    }
}*/
