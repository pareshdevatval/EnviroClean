package com.enviroclean.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.RowValetsBinding
import com.enviroclean.model.SchValet
import com.enviroclean.model.Valets
import com.enviroclean.utils.AppUtils

/**
 * Created by imobdev on 23/12/19
 */
class ListofValetAdapter(var context: Context, var seletedValets: ArrayList<SchValet?>) :
    BaseBindingAdapter<Valets>() {


    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        return RowValetsBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        val binding: RowValetsBinding = holder.binding as RowValetsBinding
        val item = items[position]

        item?.let {
            AppUtils.loadImages(binding.root.context, binding.ivValets, item.u_image, true)
        }
        binding.view.setOnClickListener {
            binding.cbValet.performClick()
        }
        binding.tvValetName.setOnClickListener {
            binding.cbValet.performClick()
        }
        binding.tvAssignCount.setOnClickListener {
            binding.cbValet.performClick()
        }
        binding.ivValets.setOnClickListener {
            binding.cbValet.performClick()
        }
        binding.tvValetName.text = item!!.u_first_name + " " + item.u_last_name
        when (item.u_comm_count) {
            0 -> binding.tvAssignCount.text = "No Communities Assigned"
            1 -> binding.tvAssignCount.text = item.u_comm_count.toString() + " Community Assigned"
            else -> { // Note the block
                binding.tvAssignCount.text = item.u_comm_count.toString() + " Communities Assigned"
            }
        }

        binding.cbValet.tag=position
        if (seletedValets.any { item.u_id == it!!.u_id }) {
            println("in list")
            println(item.u_first_name)
            binding.cbValet.isChecked = true
            item.isSelecte=true
        }else{
            item.isSelecte=false
            binding.cbValet.isChecked = false
        }
    }

    fun filter(string: String) {
        items = ArrayList(allItems.filter { it!!.u_first_name.toLowerCase().contains(string.toLowerCase()) })
        notifyDataSetChanged()
    }

}