package com.enviroclean.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.ItemManagerValetsWorkBinding
import com.enviroclean.databinding.ItemMangerMyWorkBinding
import com.enviroclean.databinding.LoadMoreProgressBinding
import com.enviroclean.model.ManagerWorkModel
import com.enviroclean.model.WorkListResponse
import com.enviroclean.ui.fragment.manager.ManagerMyWorkFragment
import com.enviroclean.utils.AppUtils

/**
 * Created by imobdev on 20/12/19
 */
class ManagerMyWorkAdapter : BaseBindingAdapter<WorkListResponse.Result?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemMangerMyWorkBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM -> {
                val binding: ItemMangerMyWorkBinding = holder.binding as ItemMangerMyWorkBinding
                val item = items[position]
                if(ManagerMyWorkFragment.currentWeek==ManagerMyWorkFragment.selectedWeek){
                    binding.ivCheckIn.visibility=View.VISIBLE
                    binding.ivLocation.visibility=View.VISIBLE
                }else{
                    binding.ivCheckIn.visibility=View.GONE
                    binding.ivLocation.visibility=View.VISIBLE
                }
                binding.data=item
                item?.let {
                }
            }
            LOADING -> {

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

    private fun getItem(position: Int): WorkListResponse.Result? {
        return items[position]
    }
}