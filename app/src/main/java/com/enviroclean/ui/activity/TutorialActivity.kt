package com.enviroclean.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.enviroclean.R
import com.enviroclean.adapter.TutorialViewPagerAdapter
import com.enviroclean.base.BaseActivity
import com.enviroclean.databinding.ActivityTutorialBinding
import com.enviroclean.model.TutorialModel
import com.enviroclean.ui.fragment.TutorialFragment
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.viewmodel.TutorialViewModel
import java.util.*

class TutorialActivity : BaseActivity<TutorialViewModel>(TutorialActivity::class.java.simpleName) {


    private var handler: Handler? = null
    private val timer = Timer()
    private val mViewModel: TutorialViewModel by lazy {
        ViewModelProviders.of(this).get(TutorialViewModel::class.java)
    }

    override fun getViewModel(): TutorialViewModel {
        return mViewModel
    }

    private lateinit var binding: ActivityTutorialBinding

    companion object {
        fun newInstance(context: Context, clearBackStack: Boolean = false): Intent {
            val intent = Intent(context, TutorialActivity::class.java)
            if (clearBackStack) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tutorial)

        setupViewPagerAdapter()
    }


    /**
     * Set Tutorial Viewpager adapter
     */

    private fun setupViewPagerAdapter() {
        val tutorialViewPagerAdapter = TutorialViewPagerAdapter(supportFragmentManager)

        val bundle1 = Bundle()
        val tutorialOne = TutorialModel(
            getString(R.string.tutorial_one),
            getString(R.string.tutorial_description_one),
            R.drawable.tutorial_one
        )
        bundle1.putParcelable(AppConstants.KEY_TUTORIAL, tutorialOne)

        val bundle2 = Bundle()
        val tutorialTwo = TutorialModel(
            getString(R.string.tutorial_two),
            getString(R.string.tutorial_description_two),
            R.drawable.tutorial_two
        )
        bundle2.putParcelable(AppConstants.KEY_TUTORIAL, tutorialTwo)

        val bundle3 = Bundle()
        val tutorialThree = TutorialModel(
            getString(R.string.tutorial_three),
            getString(R.string.tutorial_description_three),
            R.drawable.tutorial_three
        )
        bundle3.putParcelable(AppConstants.KEY_TUTORIAL, tutorialThree)

        val tutorialFragment1 = TutorialFragment.newInstance(bundle1)
        val tutorialFragment2 = TutorialFragment.newInstance(bundle2)
        val tutorialFragment3 = TutorialFragment.newInstance(bundle3)

        tutorialViewPagerAdapter.addFragment(tutorialFragment1)
        tutorialViewPagerAdapter.addFragment(tutorialFragment2)
        tutorialViewPagerAdapter.addFragment(tutorialFragment3)

        binding.vpIntroduction.adapter = tutorialViewPagerAdapter

        binding.tabLayout.setupWithViewPager(binding.vpIntroduction, true)
        binding.vpIntroduction.requestTransparentRegion(binding.vpIntroduction)

        binding.vpIntroduction.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {

            }

        })
        setupAutoPager(3)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        AppUtils.finishFromLeftToRight(this)
    }

    private var currentPage = 0
    private fun setupAutoPager(size: Int) {
        handler = Handler()

        val update = Runnable {
            binding.vpIntroduction.setCurrentItem(currentPage, true)
            if (currentPage == size) {
                startActivity(LoginActivity.newInstance(this,true))
                finish()
                AppUtils.finishFromLeftToRight(this)
            } else {
                ++currentPage
            }
        }



        timer.schedule(object : TimerTask() {
            override fun run() {
                handler!!.post(update)
            }
        }, 500, 2500)
    }


    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    override fun onPause() {
        super.onPause()
        timer.cancel()
    }
}
