package com.enviroclean.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.ItemViolationImagesSecondBinding
import com.enviroclean.model.ViolationList
import com.enviroclean.utils.AppUtils

/**
 * Created by imobdev on 20/12/19
 */
class ViolationImagesAdapter(
    val position: Int,
    val mItem: ViolationList.Result,
    val mbinding: RecyclerView
) : BaseBindingAdapter<ViolationList.Result.VioImage?>() {
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return ItemViolationImagesSecondBinding.inflate(inflater, parent, false)
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding: ItemViolationImagesSecondBinding = holder.binding as ItemViolationImagesSecondBinding
        val item = items[position]

        binding.ivIsSelectedOrNot.setOnCheckedChangeListener(null)
        binding.ivIsSelectedOrNot.isChecked=item!!.isCheck

        binding.ivIsSelectedOrNot.setOnCheckedChangeListener { _, b ->
            mItem.isCheck=true
            item.isCheck = b
            (mbinding.adapter as ViolationReportListAdapter).notifyItemRangeChanged(0,(mbinding.adapter as ViolationReportListAdapter).items.size)

        }
        binding.ivDustBin.setOnClickListener {
            binding.ivIsSelectedOrNot.performClick()
        }
        binding.ivIsSelectedOrNot.tag = position
        binding.ivDustBin.post {
            AppUtils.loadImages(binding.root.context,binding.ivDustBin,item!!.vioImageUrl,true,true)
        }
    }
}