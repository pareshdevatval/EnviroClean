package com.enviroclean.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.RowScheduleUserListBinding
import com.enviroclean.model.SchValet
import com.enviroclean.utils.AppUtils

/**
 * Created by imobdev on 20/12/19
 */
class ScheduleUserListAdapter : BaseBindingAdapter<SchValet>() {

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return RowScheduleUserListBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding: RowScheduleUserListBinding = holder.binding as RowScheduleUserListBinding
        val item = items[position]
        holder.itemView.visibility = View.VISIBLE
        item?.let {
            AppUtils.loadImages(binding.root.context, binding.ivChatUser, item.u_image, true)
        }
        var name = item!!.u_first_name + "\n" + item.u_last_name
        binding.tvName.text = name
    }
}