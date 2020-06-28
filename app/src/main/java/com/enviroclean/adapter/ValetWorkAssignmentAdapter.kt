package com.enviroclean.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.enviroclean.R
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.ItemManagerValetsWorkBinding
import com.enviroclean.databinding.ItemMangerMyWorkBinding
import com.enviroclean.databinding.ItemValetsWorkAssignmentBinding
import com.enviroclean.databinding.LoadMoreProgressBinding
import com.enviroclean.model.ValetWorkAssignmentModel
import com.enviroclean.model.WorkListResponse
import com.enviroclean.ui.fragment.manager.ManagerValetWorkFragment
import com.enviroclean.ui.fragment.valet.ValetWorkAssignmentFragment
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils

/**
 * Created by imobdev on 20/12/19
 */
class ValetWorkAssignmentAdapter : BaseBindingAdapter<WorkListResponse.Result?>() {
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemValetsWorkAssignmentBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {

        when (getItemViewType(position)) {
            ITEM -> {
                val binding: ItemValetsWorkAssignmentBinding = holder.binding as ItemValetsWorkAssignmentBinding
                val item = items[position]
                if(ValetWorkAssignmentFragment.currentDate==ValetWorkAssignmentFragment.selectedDate){
                    binding.ivLocation.visibility=View.VISIBLE
                    binding.ivCheckIn.visibility=View.VISIBLE
                }else{
                    binding.ivCheckIn.visibility=View.GONE
                }
                binding.data = item
                item?.let {
                    binding.tvTime.text= "Time \n"+AppUtils.getTimeSecond(item.commIntime)+"\n to \n"+ AppUtils.getTimeSecond(item.commOuttime)

                    if(ValetWorkAssignmentFragment.currentDate==ValetWorkAssignmentFragment.selectedDate){
                        if(AppConstants.GREEN_BACKGROUND==AppUtils.onTimeSetSecond(item.commIntime,item.commOuttime))
                        {
                            binding.clView.setBackgroundColor(binding.root.context.resources.getColor(R.color.green_2))
                            setIcon(true,binding,
                                R.drawable.ic_reminder_white,
                                R.drawable.ic_checkin_white,
                                R.drawable.ic_location_white,true)

                            binding.tvCommunityName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                            binding.tvCommunityCount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                            binding.tvCommunityStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                            binding.tvTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))


                        }else if(AppConstants.RED_BACKGROUND==AppUtils.onTimeSetSecond(item.commIntime,item.commOuttime)){
                            binding.clView.setBackgroundColor(binding.root.context.resources.getColor(R.color.red_1))
                            setIcon(
                                false,
                                binding,
                                R.drawable.ic_reminder_white,
                                R.drawable.ic_checkin_white,
                                R.drawable.ic_location_white
                            )
                            binding.tvCommunityName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                            binding.tvCommunityCount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                            binding.tvCommunityStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                            binding.tvTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))

                        } else{
                            binding.clView.setBackgroundColor(binding.root.context.resources.getColor(R.color.white))
                            setIcon(
                                false,
                                binding,
                                R.drawable.ic_reminder,
                                R.drawable.ic_checkin_green,
                                R.drawable.ic_location_gray,true
                            )
                            binding.tvCommunityName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.Black_title))
                            binding.tvCommunityCount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.Black_title))
                            binding.tvCommunityStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.Black_title))
                            binding.tvTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.Black_title))

                        }
                    }else{
                        binding.clView.setBackgroundColor(binding.root.context.resources.getColor(R.color.white))
                        setIcon(
                            false,
                            binding,
                            R.drawable.ic_reminder,
                            R.drawable.ic_checkin_green,
                            R.drawable.ic_location_gray,true
                        )
                        binding.tvCommunityName.setTextColor(ContextCompat.getColor(binding.root.context, R.color.Black_title))
                        binding.tvCommunityCount.setTextColor(ContextCompat.getColor(binding.root.context, R.color.Black_title))
                        binding.tvCommunityStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.Black_title))
                        binding.tvTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.Black_title))

                    }

                }
            }
            LOADING -> {

            }
        }
    }
    private fun setIcon(
        visible: Boolean,
        binding: ItemValetsWorkAssignmentBinding,
        url: Int,
        icCheckinWhite: Int,
        icLocationWhite: Int,
        isFutureTime:Boolean=false,
        isPastDate:Boolean=false
    ) {
        if(visible){
            if(ValetWorkAssignmentFragment.currentDate==ValetWorkAssignmentFragment.selectedDate){
                binding.ivCheckIn.visibility= View.VISIBLE
                binding.ivLocation.visibility= View.VISIBLE

            }else{
                binding.ivCheckIn.visibility= View.GONE
            }
        }else{

            binding.ivCheckIn.visibility=View.GONE
        }

        binding.ivClock. setImageResource(url)
        binding.ivCheckIn. setImageResource(icCheckinWhite)
        binding.ivLocation. setImageResource(icLocationWhite)
        binding.ivLocation.visibility=View.VISIBLE
    }

    override
    fun getItemViewType(position: Int): Int {
        return if (position == items.size - 1 && isLoadingAdded) LOADING else ITEM
    }

}