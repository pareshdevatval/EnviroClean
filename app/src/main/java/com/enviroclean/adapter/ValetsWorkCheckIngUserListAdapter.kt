package com.enviroclean.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.ItemManagerValetsWorkChaingUserListBinding
import com.enviroclean.iterfacea.CheckInValetsClick
import com.enviroclean.model.CheckIngResponse1
import com.enviroclean.utils.AppUtils


/**
 * Created by imobdev on 20/12/19
 */
class ValetsWorkCheckIngUserListAdapter(val checkInValetsClick: CheckInValetsClick) :
    BaseBindingAdapter<CheckIngResponse1.Result.CommCheckinValet>() {

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemManagerValetsWorkChaingUserListBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding: ItemManagerValetsWorkChaingUserListBinding =
            holder.binding as ItemManagerValetsWorkChaingUserListBinding
        val item = items[position]
        binding.data = item
        item?.let {

            binding.viewMain.setOnClickListener {
                checkInValetsClick.setValetClick(position, item)
            }
            binding.ivChatUser.setOnClickListener {
                checkInValetsClick.setValetClick(position, item)
            }
            AppUtils.loadImages(binding.root.context, binding.ivChatUser, item.uImage, true)
        }
    }
}