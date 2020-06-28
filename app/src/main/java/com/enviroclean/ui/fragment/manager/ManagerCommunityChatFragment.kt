package com.enviroclean.ui.fragment.manager

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enviroclean.adapter.ManagerCommunityChatAdapter
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseFragment
import com.enviroclean.databinding.FragmentManagerCommunityChatBinding
import com.enviroclean.model.CurrentCommunityUsers
import com.enviroclean.ui.activity.ChatActivity
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.ManagerCommuntyChatViewModel
import java.util.*

/**
 * Created by imobdev on 20/12/19
 */
class ManagerCommunityChatFragment : BaseFragment<ManagerCommuntyChatViewModel>(),
    BaseBindingAdapter.ItemClickListener<CurrentCommunityUsers.Result> {
    override fun onItemClick(view: View, data: CurrentCommunityUsers.Result?, position: Int) {
        isChatOpen = true
        startActivity(
            ChatActivity.newInstance(
                context!!,
                data!!.uFirstName + " " + data.uLastName
            , data.u_channel_id, data.uId.toString(), "enviroclean-"+data.uId, data.uImage, data.uId
            )
        )
    }

    private lateinit var binding: FragmentManagerCommunityChatBinding
    private lateinit var prefs: Prefs
    private var channelId=""

    private var isChatOpen = false
    private val mViewModel: ManagerCommuntyChatViewModel by lazy {
        ViewModelProviders.of(this).get(ManagerCommuntyChatViewModel::class.java)
    }

    override fun getViewModel(): ManagerCommuntyChatViewModel {
        return mViewModel
    }

    companion object {
        fun newInstance(channelId: String = ""): ManagerCommunityChatFragment {
            val bundle = Bundle()
            bundle.putString("CHANNEL_ID", channelId)
            val fragment = ManagerCommunityChatFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentManagerCommunityChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = this.context?.let { Prefs.getInstance(it) }!!
        init()
    }

    private fun init() {
        channelId=(parentFragment as ManagerChatFragment).channelId
        mViewModel.getResponse().observe(this,observeChatUserList)
        binding.rvCommunityChat.layoutManager =
            LinearLayoutManager(context) as RecyclerView.LayoutManager?
        val adapter = ManagerCommunityChatAdapter()
        binding.rvCommunityChat.adapter = adapter
        adapter.itemClickListener=this

        val param: HashMap<String, Any?> = HashMap()
        param[ApiParams.TYPE] = AppConstants.COMMUNITY_USER_CHAT
        if (prefs.checkingResponse!=null && prefs.checkingResponse!!.result!!.commId.toString().isNotEmpty()){
            param[ApiParams.COMM_ID] =prefs.checkingResponse!!.result!!.commId.toString()
        }else{
            param[ApiParams.COMM_ID] =""
        }

        param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
        mViewModel.callApi(param)

        (parentFragment as ManagerChatFragment).binding.etSearch.addTextChangedListener(object :
            TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {

                (binding.rvCommunityChat.adapter as ManagerCommunityChatAdapter).filter(s.toString().toLowerCase())
            }
        })
    }

    private val observeChatUserList=Observer<CurrentCommunityUsers>{
        if(it.status){
            if (it.result.isNotEmpty()){
                (binding.rvCommunityChat.adapter as ManagerCommunityChatAdapter).setItem(it.result)
                (binding.rvCommunityChat.adapter as ManagerCommunityChatAdapter).notifyDataSetChanged()

            }else{
                binding.tvNoData.visibility=View.VISIBLE
                binding.tvNoData.text=it.message
            }

        }else{
            binding.tvNoData.visibility=View.VISIBLE
            binding.tvNoData.text=it.message
        }
    }

    override fun onResume() {
        super.onResume()
        if (isChatOpen) {
            isChatOpen = false
            val param: HashMap<String, Any?> = HashMap()
            param[ApiParams.TYPE] = AppConstants.COMMUNITY_USER_CHAT
            if (prefs.checkingResponse != null && prefs.checkingResponse!!.result!!.commId.toString().isNotEmpty()/*&&prefs.isCheck*/) {
                param[ApiParams.COMM_ID] = prefs.checkingResponse!!.result!!.commId.toString()
            } else {
                param[ApiParams.COMM_ID] = ""
            }

            param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
            mViewModel.callApi(param)
        }
    }

}