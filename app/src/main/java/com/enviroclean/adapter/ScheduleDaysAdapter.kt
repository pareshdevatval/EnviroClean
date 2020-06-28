package com.enviroclean.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.databinding.ViewDataBinding
import com.enviroclean.R
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.RowDaysNameBinding
import com.enviroclean.model.DaysModel

/**
 * Created by imobdev on 23/12/19
 */
class ScheduleDaysAdapter(
    var context: Context,
   val tvDayOfWeek: AppCompatCheckBox
) : BaseBindingAdapter<DaysModel>() {

    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return RowDaysNameBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding: RowDaysNameBinding = holder.binding as RowDaysNameBinding
        val item = items[position]

        binding.daysData = item
        if (item!!.isSelected) {
            binding.tvDaysName.setBackgroundResource(item.color)
            binding.tvDaysName.setTextColor(context.resources.getColor(R.color.white))
        }else{
            binding.tvDaysName.setBackgroundResource(R.drawable.gray_solid_rounded)
            binding.tvDaysName.setTextColor(context.resources.getColor(R.color.Black_title))
        }
    }
}