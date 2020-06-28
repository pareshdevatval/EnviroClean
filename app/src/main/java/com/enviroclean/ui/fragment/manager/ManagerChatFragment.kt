package com.enviroclean.ui.fragment.manager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProviders
import com.enviroclean.R
import com.enviroclean.adapter.ViewPagerAdapter
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseFragment
import com.enviroclean.databinding.FragmentManagerChatBinding
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.ManagerChatViewModel
import androidx.constraintlayout.solver.widgets.WidgetContainer.getBounds



/**
 * Created by imobdev on 20/12/19
 */
class ManagerChatFragment : BaseFragment<ManagerChatViewModel>() {

     lateinit var binding: FragmentManagerChatBinding
    private lateinit var prefs: Prefs

     var channelId=""

    private val mViewModel: ManagerChatViewModel by lazy {
        ViewModelProviders.of(this).get(ManagerChatViewModel::class.java)
    }

    override fun getViewModel(): ManagerChatViewModel {
        return mViewModel
    }

    companion object {
        fun newInstance(chanel:String=""): ManagerChatFragment {
            val bundle = Bundle()
            bundle.putString("CHANNEL_ID",chanel)
            val fragment = ManagerChatFragment()
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
        binding = FragmentManagerChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs.getInstance(context!!)!!
        setTabLayout()
        setToolBar()
        setObserver()
        mViewModel.callGetTwilioAccessTokenAPI("enviroclean-" + prefs.userDataModel!!.logindata.uId)

        arguments?.getString("CHANNEL_ID")?.let {
            channelId = it
        }

        Log.e("CHANNELID CHAT",channelId)
        
    }

    private fun setToolBar() {

        binding.toolbar.tvToolbarTitle.text = getString(R.string.lbl_chat)

        setToolbarRight1MenuIcon(binding.toolbar, R.drawable.ic_search_white, object :
            BaseActivity.ToolbarRight1MenuClickListener {
            override fun onRight1MenuClicked() {
                context?.let{

                    binding.viewToolbar.visibility=View.GONE
                    binding.tabLayoutTop.visibility=View.GONE
                    binding.viewSearch.visibility=View.VISIBLE

                    binding.viewPager
                    showSearchView()
                }
            }
        })

        binding.etSearch.setOnClickListener {

        }

        binding.etSearch.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val DRAWABLE_LEFT = 0


                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >=   binding.etSearch.left -   binding.etSearch.compoundDrawables[DRAWABLE_LEFT].bounds.width()) {
                        AppUtils.loge("CLICK ICON")
                        closeSearchView()
                        binding.tabLayoutTop.visibility=View.VISIBLE
                        return true
                    }
                }
                return false
            }
        })

    }

    fun showSearchView() {
        binding.viewPager.setOnTouchListener { v, _ ->
            true
        }
        val shake = AnimationUtils.loadAnimation(
            context!!,
            R.anim.shake
        )

        binding.etSearch.startAnimation(shake)
        binding.etSearch.performClick()
        binding.etSearch.requestFocus()

        AppUtils.showKeyboard((activity as BaseActivity<*>).activity, binding.etSearch)
    }

    fun closeSearchView() {
        binding.viewPager.setOnTouchListener { v, _ ->
            false
        }
        binding.viewSearch.visibility = View.GONE
        binding.viewToolbar.visibility = View.VISIBLE
        val shake = AnimationUtils.loadAnimation(
            context,
            R.anim.return_shake
        )

        binding.etSearch.startAnimation(shake)
        binding.etSearch.clearFocus()
        binding.etSearch.setText("")
        AppUtils.hideKeyboard((activity as BaseActivity<*>).activity!!)
    }
    private fun setObserver() {
        mViewModel.getTwilioAccessResponse().observe({ this.lifecycle }, { accessToken: String? ->
            prefs.twilioAccessToken = accessToken
        })
    }
    private fun setTabLayout() {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(
            ManagerCommunityChatFragment.newInstance(channelId),
            getString(R.string.lbl_current_community)
        )
        adapter.addFragment(
            ManagerOtherChatFragment.newInstance(channelId),
            getString(R.string.lbl_others)
        )

        binding.viewPager.adapter = adapter
        binding.tabLayoutTop.setupWithViewPager(binding.viewPager)
        binding.viewPager.offscreenPageLimit = 2
    }

}