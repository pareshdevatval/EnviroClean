package com.enviroclean.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.ItemMangerMyWorkBinding
import com.enviroclean.databinding.ItemPayStubBinding
import com.enviroclean.databinding.ItemValetsWorkAssignmentBinding
import com.enviroclean.databinding.LoadMoreProgressBinding
import com.enviroclean.model.ManagerWorkModel
import com.enviroclean.model.MyPaystubModel
import com.enviroclean.model.PayStubResponse
import com.enviroclean.model.WorkListResponse
import com.enviroclean.utils.AppUtils
import java.util.*

/**
 * Created by imobdev on 20/12/19
 */
class MyPaystubAdapter : BaseBindingAdapter<PayStubResponse.Result>() {

    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemPayStubBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding: ItemPayStubBinding = holder.binding as ItemPayStubBinding
                val item = items[position]

                item?.let {
                    val rnd = Random()
                    val currentColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
                    binding.lineView.setBackgroundColor(currentColor)
                    binding.tvAmount.setTextColor(currentColor)

                    binding.tvAmount.text="$ "+item.upAmount
                    binding.tvInvoiceNoHours.text="Invoice : "+item.upInvNo+" | "+"Hours "+item.upHrs

                    binding.tvDate.text=AppUtils.convertDate2(item.upStartDate)+" - "+AppUtils.convertDate2(item.upEndDate)
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

    private fun getItem(position: Int): PayStubResponse.Result? {
        return items[position]
    }

}