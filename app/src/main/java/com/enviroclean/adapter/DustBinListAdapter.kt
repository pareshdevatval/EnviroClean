package com.enviroclean.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.enviroclean.R
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.ItemManagerValetsWorkCommunityBulddingSublistBinding
import com.enviroclean.model.AreaDataModel
import com.enviroclean.utils.AppConstants

/**
 * Created by imobdev on 20/12/19
 */
class DustBinListAdapter : BaseBindingAdapter<AreaDataModel>() {
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemManagerValetsWorkCommunityBulddingSublistBinding.inflate(inflater, parent, false)
    }

    /*1 - remaining 2 - scan 3 - violation*/
    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding: ItemManagerValetsWorkCommunityBulddingSublistBinding = holder.binding as
                ItemManagerValetsWorkCommunityBulddingSublistBinding
        val item = items[position]
        binding.clView.animate().translationY(10f);

        item?.let {
            binding.tvCodeNumber.text=item.qrCode
            if( item.qrType == AppConstants.REMAINING){
                binding.tvName.text=it.username
                binding.ivDustBin.setBackgroundResource(R.drawable.ic_bin_black)
                binding.ivViolate.setBackgroundResource(R.drawable.ic_violate_report_black_big)
                binding.tvCodeNumber.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        binding.root.context
                        , R.drawable.ic_black_right
                    ), null, null, null
                )
            }else{
                binding.tvName.text = "By " + it.username
                binding.ivDustBin.setBackgroundResource(R.drawable.ic_bin_unselected)
                binding.ivViolate.setBackgroundResource(R.drawable.ic_violate_report_unselect_big)
                binding.tvCodeNumber.setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(
                        binding.root.context
                        , R.drawable.ic_green_right
                    ), null, null, null
                )
            }
        }
    }
}