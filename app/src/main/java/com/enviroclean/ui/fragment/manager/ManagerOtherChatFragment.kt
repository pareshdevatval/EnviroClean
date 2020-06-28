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
import com.enviroclean.adapter.ListofValetAdapter
import com.enviroclean.adapter.ManagerCommunityChatAdapter
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.base.BaseFragment
import com.enviroclean.databinding.FragmentManagerOtherChatBinding
import com.enviroclean.model.CurrentCommunityUsers
import com.enviroclean.ui.activity.ChatActivity
import com.enviroclean.utils.ApiParams
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.viewmodel.ManagerOtherChatViewModel
import java.util.*

/**
 * Created by imobdev on 20/12/19
 */
class ManagerOtherChatFragment : BaseFragment<ManagerOtherChatViewModel>(),
    BaseBindingAdapter.ItemClickListener<CurrentCommunityUsers.Result> {
    override fun onItemClick(view: View, data: CurrentCommunityUsers.Result?, position: Int) {
        isChatOpen = true
        startActivity(
            ChatActivity.newInstance(
                context!!,
                data!!.uFirstName + " " + data.uLastName
                , data.u_channel_id, data.uId.toString(), "enviroclean-" + data.uId
                , data.uImage, data.uId
            )
        )
    }

    private var isChatOpen = false
    private lateinit var binding: FragmentManagerOtherChatBinding
    private var channelId = ""

    private val mViewModel: ManagerOtherChatViewModel by lazy {
        ViewModelProviders.of(this).get(ManagerOtherChatViewModel::class.java)
    }

    override fun getViewModel(): ManagerOtherChatViewModel {
        return mViewModel
    }

    companion object {
        fun newInstance(ChannelId: String = ""): ManagerOtherChatFragment {
            val bundle = Bundle()
            bundle.putString("CHANNEL_ID", ChannelId)
            val fragment = ManagerOtherChatFragment()
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
        binding = FragmentManagerOtherChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        channelId = (parentFragment as ManagerChatFragment).channelId

        mViewModel.getResponse().observe(this, observeChatUserList)
        binding.rvOtherChat.layoutManager =
            LinearLayoutManager(context) as RecyclerView.LayoutManager?
        val adapter = ManagerCommunityChatAdapter()
        binding.rvOtherChat.adapter = adapter
        adapter.itemClickListener = this

        val param: HashMap<String, Any?> = HashMap()
        //param[ApiParams.PAGE] = "1"
        param[ApiParams.TYPE] = AppConstants.OTHER_USER_CHAT
        // param[ApiParams.COMM_ID] = "1"
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

                (binding.rvOtherChat.adapter as ManagerCommunityChatAdapter).filter(
                    s.toString().toLowerCase()
                )
            }
        })

    }

    private val observeChatUserList = Observer<CurrentCommunityUsers> {
        if (it.status) {
            //it.result.remove(it.result[it.result.size - 1])
            (binding.rvOtherChat.adapter as ManagerCommunityChatAdapter).setItem(it.result)
            (binding.rvOtherChat.adapter as ManagerCommunityChatAdapter).notifyDataSetChanged()
            if (channelId.isNotEmpty()) {
                Log.e("CHANNELID 2", channelId)
                for (data in it.result) {
                    if (data!!.u_channel_id == channelId) {

                        channelId = ""
                        isChatOpen = true
                        startActivity(
                            ChatActivity.newInstance(
                                context!!,
                                data.uFirstName + " " + data.uLastName
                                ,
                                data.u_channel_id,
                                data.uId.toString(),
                                "enviroclean-" + data.uId,
                                data.uImage,
                                data.uId
                            )
                        )
                        break
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isChatOpen) {
            isChatOpen = false
            val param: HashMap<String, Any?> = HashMap()
            param[ApiParams.TYPE] = AppConstants.OTHER_USER_CHAT
            param[ApiParams.DEVICE_TYPE] = AppConstants.ANDROID_DEVICE_TYPE
            mViewModel.callApi(param)
        }
    }


}