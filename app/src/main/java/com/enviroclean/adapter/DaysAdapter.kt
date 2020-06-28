package com.enviroclean.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.enviroclean.R
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.RowDaysBinding
import java.util.*

/**
 * Created by imobdev on 23/12/19
 */
class DaysAdapter(var context: Context) : BaseBindingAdapter<String>() {

    var bgList = arrayOf<Int>(
        R.drawable.pink_solid_rounded,
        R.drawable.yellow_solid_rounded,
        R.drawable.green_solid_rounded,
        R.drawable.bulelight_solid_rounded,
        R.drawable.redlight_solid_rounded,
        R.drawable.pink_solid_rounded,
        R.drawable.green_solid_rounded,
        R.drawable.yellow_solid_rounded,
        R.drawable.bulelight_solid_rounded
    )


    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return RowDaysBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding: RowDaysBinding = holder.binding as RowDaysBinding
        val item = items[position]
        binding.day = item

        val rn = Random()
        val answer = rn.nextInt(6) + 1
        binding.tvDaysName.setTextColor(context.resources.getColor(R.color.white))
        binding.tvDaysName.setBackgroundResource(bgList[answer])
    }
}