package com.enviroclean.adapter

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.ViewDataBinding
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.RowListOfScheduleBinding
import com.enviroclean.model.ScheduleList
import com.enviroclean.utils.AppUtils
import android.view.animation.AnimationUtils.loadAnimation
import com.enviroclean.R
import android.R.attr.button
import android.view.animation.BounceInterpolator


/**
 * Created by imobdev on 23/12/19
 */
class ListOfScheduleAdapter(var context: Context) : BaseBindingAdapter<ScheduleList>() {


    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return RowListOfScheduleBinding.inflate(inflater, parent, false)
    }
    private var lastPosition = -1

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding: RowListOfScheduleBinding = holder.binding as RowListOfScheduleBinding
        val item = items[position]

        binding.tvCommunityName.text = item!!.sch_name
        binding.tvScheduleName.text = item.sch_comm_name
        var time =
            "Time\n" + AppUtils.getTimeSecond(item.sch_intime) + " to " +
                    AppUtils.getTimeSecond(item.sch_outtime)

        binding.tvTime.text = time
        if (item.sch_days.isNotEmpty()) {
            binding.rvDays.visibility = View.VISIBLE
            var daysAdapter = DaysAdapter(context)
            binding.rvDays.adapter = daysAdapter
            (binding.rvDays.adapter as DaysAdapter).setItem(item.sch_days)
        } else {
            binding.rvDays.visibility = View.GONE
        }

        if (item.sch_valets.isNotEmpty()) {
            binding.rvValet.visibility = View.VISIBLE
            binding.clNoData.visibility=View.GONE
            var valetAdapter = ScheduleUserListAdapter()
            binding.rvValet.adapter = valetAdapter
            (binding.rvValet.adapter as ScheduleUserListAdapter).setItem(item.sch_valets)
        } else {
            binding.rvValet.visibility = View.GONE
            binding.clNoData.visibility=View.VISIBLE

        }

        if (position > lastPosition) {
            val animY = ObjectAnimator.ofFloat( binding.ivArrow, "translationY", -100f, 0f)
            animY.duration = 1000//1sec
            animY.interpolator = BounceInterpolator()
            animY.repeatCount = Animation.INFINITE
            animY.start()
        }

    }
}