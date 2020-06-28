package com.enviroclean.adapter

import android.text.TextUtils.split
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import com.enviroclean.R
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.ItemNotificationBinding
import com.enviroclean.databinding.ItemValetsWorkAssignmentBinding
import com.enviroclean.databinding.LoadMoreProgressBinding
import com.enviroclean.model.NotificationListResponse
import com.enviroclean.model.NotificationModel
import com.enviroclean.model.WorkListResponse
import com.enviroclean.utils.AppUtils
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by imobdev on 23/12/19
 */
class NotificationAdapter : BaseBindingAdapter<NotificationListResponse.Result>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemNotificationBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM -> {
                val binding: ItemNotificationBinding = holder.binding as ItemNotificationBinding
                val item = items[position]
                binding.notificationData=item

                if(item!!.nReadflag==1){
                    binding.ivStatus.background=binding.root.context.resources.getDrawable(R.drawable.gray_solid_circle)
                }else{
                    binding.ivStatus.background=binding.root.context.resources.getDrawable(R.drawable.blue_solid_circle)
                }
                val c = Calendar.getInstance()
                System.out.println("Current time => " + c.getTime())

                val df = SimpleDateFormat("yyyy-MM-dd")
                val formattedDate = df.format(c.time)

                val date = item.nCreatedAt.split(" ")[0]
               AppUtils.loge("cureent->"+formattedDate)
               AppUtils.loge("response->"+date)
                if(formattedDate==item.nCreatedAt.split(" ")[0]){
                    binding.tvTime.text= AppUtils.convertUTCtoLocal(item.nCreatedAt)
                }else{
                    binding.tvTime.text= AppUtils.convertUTCtoLocal(item.nCreatedAt,true)
                }
            }
            LOADING->{

            }
        }


    }

    override
    fun getItemViewType(position: Int): Int {
        return if (position == items.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    fun addLoadingFooter() {
        isLoadingAdded = true
    }

    fun removeLoadingFooter(isLast: Boolean = false) {
        isLoadingAdded = false
    }

    private fun getItem(position: Int): NotificationListResponse.Result? {
        return items[position]
    }
}