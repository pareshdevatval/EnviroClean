package com.enviroclean.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.ItemMangerMyWorkBinding
import com.enviroclean.databinding.ItemViolationImagesBinding
import com.enviroclean.model.ManagerWorkModel
import com.enviroclean.model.UploadImagesModel
import com.enviroclean.utils.AppUtils

/**
 * Created by imobdev on 20/12/19
 */
class ViolationSubmitAdapter : BaseBindingAdapter<UploadImagesModel?>() {
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemViolationImagesBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding: ItemViolationImagesBinding = holder.binding as ItemViolationImagesBinding
        val item = items[position]
        if(position==0){
            binding.clView.visibility=View.GONE
            binding.viewAddImages.visibility=View.VISIBLE
        }else{
            binding.clView.visibility=View.VISIBLE
            binding.viewAddImages.visibility=View.GONE
        }
        binding.ivDustBin.post {
            AppUtils.loadImages(binding.root.context,binding.ivDustBin,item!!.images,false,true)
        }
    }
}