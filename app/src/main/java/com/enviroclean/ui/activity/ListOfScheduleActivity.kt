package com.enviroclean.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enviroclean.R
import com.enviroclean.adapter.ListOfScheduleAdapter
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.databinding.ActivityListOfScheduleBinding
import com.enviroclean.model.Communities
import com.enviroclean.model.ListOfScheduleResponse
import com.enviroclean.model.ScheduleList
import com.enviroclean.utils.AppUtils
import com.enviroclean.viewmodel.ListOfScheduleViewModel
import com.google.gson.Gson
import kotlin.collections.ArrayList


class ListOfScheduleActivity :
    BaseActivity<ListOfScheduleViewModel>(ListOfScheduleActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener, BaseBindingAdapter.ItemClickListener<ScheduleList> {
    override fun onItemClick(view: View, data: ScheduleList?, position: Int) {
        when (view.id) {
            R.id.btnAssign -> {
                val gson = Gson()

                val valets = gson.toJson(data!!.sch_valets)
                startActivityForResult(
                    ListOfValetsActivity.newInstance(
                        this,
                        data!!.sch_comm_id.toString(),
                        data.sch_id.toString(),valets
                    ),1010
                )
            }
        }
    }

    override fun onLeftIconClicked() {
        onBackPressed()
    }

    override fun getViewModel(): ListOfScheduleViewModel {
        return mViewModel
    }

    private val mViewModel: ListOfScheduleViewModel by lazy {
        ViewModelProviders.of(this).get(ListOfScheduleViewModel::class.java)
    }
    private lateinit var binding: ActivityListOfScheduleBinding
    private lateinit var adapter: ListOfScheduleAdapter
    private var currentPage = 1
    private var canLoadMore = true
    private var isLoading = false
    companion object {
        fun newInstance(context: Context): Intent {
            val intent = Intent(context, ListOfScheduleActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list_of_schedule)
        init()
    }

    private fun init() {
        initToolbar()
        setObserver()
        setAdapter()
        setLoadMore()
        mViewModel.getScheduleListData(currentPage,"")
    }

    private fun initToolbar() {
        setToolbarBackground(R.color.blue_toolbar)
        setToolbarTitle(R.string.lbl_list_of_schedule)
        setToolbarLeftIcon(R.drawable.ic_back_white, this)
    }

    private fun setAdapter() {
        binding.rvSchedule.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        adapter = ListOfScheduleAdapter(this)
        binding.rvSchedule.adapter = adapter
        binding.rvSchedule.scheduleLayoutAnimation()
        adapter.itemClickListener = this
    }
     var TOTAL_PAGES=0
    private fun setObserver() {
        mViewModel.getResponse().observe({ this.lifecycle }) { response: ListOfScheduleResponse? ->

            if (response!!.status) {
                TOTAL_PAGES=response.total_pages
                binding.rvSchedule.visibility=View.VISIBLE
                binding.tvNoData.visibility=View.GONE
                adapter.addItems(response.result)
                adapter.notifyDataSetChanged()
                initSpinnerCommunity(response.communities)
            } else {
                canLoadMore = false
                if (canLoadMore && currentPage==1){
                    binding.rvSchedule.visibility=View.GONE
                    binding.tvNoData.visibility=View.VISIBLE
                    binding.tvNoData.text=response.message
                }
            }

        }
    }

    private fun setLoadMore() {

        binding.rvSchedule.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val dataSize = (binding.rvSchedule.adapter as ListOfScheduleAdapter).itemCount
                if(TOTAL_PAGES!=currentPage){
                    if ((binding.rvSchedule.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition() == dataSize - 1) {
                        if (canLoadMore && !isLoading) {
                            currentPage++
                            mViewModel.getScheduleListData(currentPage,communitesId)
                        }
                    }
                }

            }
        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==1010 && resultCode==Activity.RESULT_OK){
            Handler().postDelayed({
                adapter.items.clear()
                currentPage=1
                mViewModel.getScheduleListData(currentPage,communitesId)
            }, 500)

        }
    }

var communitesId:String=""
    var selectedCommId=0
    private fun initSpinnerCommunity(communities: ArrayList<Communities?>) {

        val commnitesNameList: ArrayList<String> = ArrayList()

        for(i in 0 until communities.size){
            commnitesNameList.add(communities[i]!!.comm_name)
            if(communitesId==communities[i]!!.comm_id.toString()){
                selectedCommId= i
            }
        }



        AppUtils.loge(commnitesNameList.toString())
        val selectCommunityPopupWindow = ListPopupWindow(this@ListOfScheduleActivity)
        selectCommunityPopupWindow.setAdapter(
            ArrayAdapter(
                this@ListOfScheduleActivity,
                R.layout.spinner_drop_down_item,
                commnitesNameList
            )
        )
        selectCommunityPopupWindow.anchorView = binding.tvSelectCommunityList
        selectCommunityPopupWindow.isModal = true
        binding.tvSelectCommunityList.text=communities[selectedCommId]!!.comm_name
        selectCommunityPopupWindow.setOnItemClickListener { _, _, position, _ ->
            val selectedSortBy = commnitesNameList[position]
            communitesId = communities[position]!!.comm_id.toString()
            binding.tvSelectCommunityList.text = selectedSortBy
            (binding.rvSchedule.adapter as ListOfScheduleAdapter ).clear()
            mViewModel.getScheduleListData(currentPage,communitesId)
            selectCommunityPopupWindow.dismiss()
        }
        binding.tvSelectCommunityList.setOnClickListener { selectCommunityPopupWindow.show() }


    }

}
