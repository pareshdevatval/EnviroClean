package com.enviroclean.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.AnimationUtils.loadAnimation
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.enviroclean.R
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.RowCheckUserListBinding
import com.enviroclean.databinding.RowScheduleUserListBinding
import com.enviroclean.model.CheckInUser
import com.enviroclean.model.SchValet
import com.enviroclean.utils.AppUtils
import kotlinx.android.synthetic.main.item_manager_valets_work_chaing_user_list.view.*

/**
 * Created by imobdev on 20/12/19
 */
class CommunityCheckInUserListAdapter : BaseBindingAdapter<CheckInUser>() {

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return RowCheckUserListBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding: RowCheckUserListBinding = holder.binding as RowCheckUserListBinding
        val item = items[position]
        holder.itemView.visibility = View.VISIBLE
        item?.let {
            AppUtils.loadImages(binding.root.context, binding.ivChatUser, item.u_image, true)
            if(item.isSelected){
                val anim = loadAnimation(binding.root.context, R.anim.scale_in_tv)
                binding. root.startAnimation(anim)
                anim.setFillAfter(true)
                binding.ivChatUser.borderColor= ContextCompat.getColor(binding.root.context, R.color.red_2)
                binding.ivChatUser.borderWidth=3
                binding.ivChatUser.elevation=10f
            }else{
                val anim = loadAnimation( binding.root.context, R.anim.scale_out_tv)
                binding. root.startAnimation(anim)
                anim.setFillAfter(true)
                binding.ivChatUser.borderWidth=0
                binding.ivChatUser.elevation=0f

            }
            val name = item.u_first_name + "\n" + item.u_last_name
            binding.tvName.text = name
        }

    }
}