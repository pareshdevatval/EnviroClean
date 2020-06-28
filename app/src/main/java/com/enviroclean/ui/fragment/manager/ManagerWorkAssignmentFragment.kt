package com.enviroclean.ui.fragment.manager

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.enviroclean.R
import com.enviroclean.adapter.ViewPagerAdapter
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseFragment
import com.enviroclean.databinding.FragmentMangerWorkAssignmentBinding
import com.enviroclean.iterfacea.MessageInterface
import com.enviroclean.ui.activity.CheckInActivity
import com.enviroclean.ui.activity.ManagerHomeActivity
import com.enviroclean.ui.activity.NotificationListActivity
import com.enviroclean.ui.activity.SplashActivity
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.utils.Prefs
import com.enviroclean.viewmodel.ManagerWorkAssignmentViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject

/**
 * Created by imobdev on 20/12/19
 */
class ManagerWorkAssignmentFragment : BaseFragment<ManagerWorkAssignmentViewModel>(),
    BaseActivity.ToolbarRightMenuClickListener {

    override fun onRightMenuClicked() {

    }
    lateinit var prefs:Prefs
    private lateinit var binding: FragmentMangerWorkAssignmentBinding

    private val mViewModel: ManagerWorkAssignmentViewModel by lazy {
        ViewModelProviders.of(this).get(ManagerWorkAssignmentViewModel::class.java)
    }

    override fun getViewModel(): ManagerWorkAssignmentViewModel {
        return mViewModel
    }

    companion object {


        fun newInstance(): ManagerWorkAssignmentFragment {
            val bundle = Bundle()
            val fragment = ManagerWorkAssignmentFragment()
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
        binding = FragmentMangerWorkAssignmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        init()
        setToolbar()
    }

    override fun onResume() {
        super.onResume()
        setNotificationCount(binding.toolbar,prefs.notificationCount!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onEvent(count: String) {
        if(count== AppConstants.NOTIFICATION_COUNT){
            setNotificationCount(binding.toolbar,prefs.notificationCount!!)
        }
    }
    private fun init() {
        prefs= Prefs.getInstance(context!!)!!
        setTabLayout()

    }

    private fun setTabLayout() {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(
            ManagerValetWorkFragment(),
            getString(R.string.lbl_valets_work)
        )
        adapter.addFragment(
            ManagerMyWorkFragment(),
            getString(R.string.lbl_my_work)
        )

        binding.viewPager.adapter = adapter
        binding.viewPager.offscreenPageLimit = 1
        binding.tabLayoutTop.setupWithViewPager(binding.viewPager)

        binding.viewPager.setOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                selectedPageNumber=position

                if(selectedPageNumber==1&&allReadyCheckInSchId!="") {
                    Handler().postDelayed({
                        selectedPageNumber=0
                        replaceFragment(
                            R.id.frame_container,
                            ManagerMyWorkCheckingFragment.newInstance(),
                            false
                        )
                    },100)

                }

                    Log.e("TabChange Lis","--->"+position)
            }
        })

    }
var selectedPageNumber=0
lateinit var allReadyCheckInSchId:String
lateinit var allReadyCheckInSchName:String
    private fun setToolbar() {
        Log.e("TOOLBAR","-->")
        binding.toolbar.tvToolbarTitle.text = getString(R.string.lbl_work_assignment)
        setNotificationCount(binding.toolbar,prefs.notificationCount!!)
        setToolbarRight1MenuIcon(binding.toolbar, R.drawable.ic_notification_white, object :
            BaseActivity.ToolbarRight1MenuClickListener {
            override fun onRight1MenuClicked() {
                context?.let{
                    startActivity(
                        NotificationListActivity.newInstance(context!!),
                        ActivityOptions.makeSceneTransitionAnimation((activity as BaseActivity<*>).activity!!).toBundle())
                }
            }
        })

    }


}