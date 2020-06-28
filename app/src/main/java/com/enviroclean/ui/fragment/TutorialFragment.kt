package com.enviroclean.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.enviroclean.databinding.FragmentTutorialBinding
import com.enviroclean.model.TutorialModel
import com.enviroclean.ui.activity.LoginActivity
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils

/**
 * Created by imobdev on 18/12/19
 */
class TutorialFragment : Fragment() {
    private lateinit var binding: FragmentTutorialBinding

    companion object {
        fun newInstance(bundle: Bundle) = TutorialFragment().apply {
            arguments = bundle
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val introduction: TutorialModel = arguments!!.get(AppConstants.KEY_TUTORIAL) as TutorialModel
        binding.tutorialData = introduction

        binding.tvSkip.setOnClickListener{
            startActivity(LoginActivity.newInstance(context!!,true))
            AppUtils.finishFromLeftToRight(context!!)
        }
    }
}