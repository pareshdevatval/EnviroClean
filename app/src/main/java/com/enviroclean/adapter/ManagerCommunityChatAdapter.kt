package com.enviroclean.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.ItemChatBinding
import com.enviroclean.model.CurrentCommunityUsers
import com.enviroclean.model.ManagerChatModel
import com.enviroclean.utils.AppUtils

/**
 * Created by imobdev on 20/12/19
 */
class ManagerCommunityChatAdapter : BaseBindingAdapter<CurrentCommunityUsers.Result>() {

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemChatBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding: ItemChatBinding = holder.binding as ItemChatBinding
        val item = items[position]
        binding.data=item

        binding.ivChatUser.post{
            AppUtils.loadImages(binding.root.context,binding.ivChatUser,item!!.uImage,true)
        }
    }
    fun filter(string: String) {
        items = ArrayList(allItems.filter { it!!.uFirstName.toLowerCase().contains(string.toLowerCase()) })
        notifyDataSetChanged()
    }
}