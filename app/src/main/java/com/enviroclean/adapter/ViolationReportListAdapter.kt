package com.enviroclean.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.enviroclean.R
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseBindingViewHolder
import com.enviroclean.databinding.ItemViolationReportListBinding
import com.enviroclean.databinding.LoadMoreProgressBinding
import com.enviroclean.model.*
import com.enviroclean.utils.Prefs


/**
 * Created by imobdev on 23/12/19
 */
class ViolationReportListAdapter(val rvViolationReport: RecyclerView) : BaseBindingAdapter<ViolationList.Result>(),
    BaseBindingAdapter.ItemClickListener<ViolationList.Result.VioImage?> {
    override fun onItemClick(view: View, data: ViolationList.Result.VioImage?, position: Int) {

    }
    var isReasonNotFound:Boolean=false
    lateinit var prefs: Prefs
    val ITEM = 0
    val LOADING = 1
    lateinit var viewHolder: ViewDataBinding
    private var isLoadingAdded = false
    override fun bind(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewDataBinding {
        when (viewType) {
            ITEM ->
                viewHolder = ItemViolationReportListBinding.inflate(inflater, parent, false)
            LOADING -> {
                viewHolder = LoadMoreProgressBinding.inflate(inflater, parent, false)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: BaseBindingViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM -> {
                val binding: ItemViolationReportListBinding = holder.binding as ItemViolationReportListBinding
                val item = items[position]
                item?.let {
                    prefs= Prefs.getInstance(binding.root.context)!!
                    binding.tvBuildingName.text= item.vioBulidname+" | Unit No. "+item.vioUnitNumber
                    binding.tvUserName.text= "By "+item.vioUsername
                    val adapter=ViolationImagesAdapter(position,item,rvViolationReport)

                    binding.rvViolationImages.adapter=adapter
                    adapter.itemClickListener=this
                    adapter.setItem(item.vioImages)
                    adapter.notifyDataSetChanged()
                    binding.ivIsSelectedOrNot.setOnCheckedChangeListener(null)
                    binding.ivIsSelectedOrNot.isChecked=item.isCheck

                    binding.ivIsSelectedOrNot.setOnCheckedChangeListener { _, b ->

                        item.isCheck = b
                        if(!b){
                            for (i in 0 until (adapter as ViolationImagesAdapter).items.size ){
                                adapter.items[i]!!.isCheck=false

                            }
                            adapter.notifyDataSetChanged()
                        }
                    }
                    binding.ivIsSelectedOrNot.tag = position
                    if(item.vioDescription.isEmpty()){
                        binding.tvViewDescription.visibility=View.GONE
                    }else{
                        binding.tvViewDescription.visibility=View.VISIBLE
                        binding.tvViewDescription.setOnClickListener {
                            viewDescriptionDialog(binding.root.context,item.vioDescription)
                        }
                    }

                    for(i in 0 until prefs.userDataModel!!.managerReason.size){
                        if(item.vioReason==prefs.userDataModel!!.managerReason[i].reasonId.toInt()){
                            isReasonNotFound=true
                            binding.tvViolationName.text=prefs.userDataModel!!.managerReason[i].reasonName
                            break
                        }
                    }
                    if(!isReasonNotFound){
                        for(i in 0 until prefs.userDataModel!!.valetReason.size){
                            if(item.vioReason==prefs.userDataModel!!.valetReason[i].reasonId.toInt()){
                                isReasonNotFound=true
                                binding.tvViolationName.text=prefs.userDataModel!!.valetReason[i].reasonName
                                break
                            }
                        }
                    }
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



    fun viewDescriptionDialog(context: Context,des:String) {
        val alert = AlertDialog.Builder(context)

        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val alertLayout = li.inflate(R.layout.description_dialog, null)

        val btnCancel = alertLayout.findViewById<AppCompatImageView>(R.id.ivCancel)
        val tvDes = alertLayout.findViewById<AppCompatTextView>(R.id.tvDes)
        tvDes.text=des
        alert.setView(alertLayout)
        alert.setCancelable(false)

        val dialog = alert.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }


    }
}