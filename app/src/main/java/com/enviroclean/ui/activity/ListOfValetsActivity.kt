package com.enviroclean.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.enviroclean.R
import com.enviroclean.adapter.ListofValetAdapter
import com.enviroclean.base.BaseActivity
import com.enviroclean.base.BaseBindingAdapter
import com.enviroclean.databinding.ActivityListOfValetsBinding
import com.enviroclean.model.ListOfValetsResponse
import com.enviroclean.model.SchValet
import com.enviroclean.model.Valets
import com.enviroclean.utils.AppConstants
import com.enviroclean.utils.AppUtils
import com.enviroclean.viewmodel.ListOfValetsViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class ListOfValetsActivity :
    BaseActivity<ListOfValetsViewModel>(ListOfValetsActivity::class.java.simpleName),
    BaseActivity.ToolbarLeftMenuClickListener, BaseActivity.ToolbarRightMenuClickListener,
    BaseBindingAdapter.ItemClickListener<Valets> {
    override fun onItemClick(view: View, data: Valets?, position: Int) {
        when (view.id) {
            R.id.cbValet -> {
                val valetData = data
                valetData!!.isSelecte = !valetData.isSelecte

                val valet = SchValet(data.u_first_name, data.u_id, data.u_image, data.u_last_name)

                if (sch_valets.any { data.u_id == it!!.u_id }) {
                    sch_valets.remove(valet)
                    println("in list")
                } else {
                    sch_valets.add(valet)
                }

                adapter.notifyItemChanged(position, valetData)
            }
        }
    }

    override fun onLeftIconClicked() {
        onBackPressed()
    }

    override fun onRightMenuClicked() {
        val valetId: ArrayList<String?> = ArrayList()
        for (item in sch_valets) {

            valetId.add(item!!.u_id.toString())

        }
        if (valetId.isNotEmpty()) {
            val id = valetId.joinToString(",")
            Log.e("SELETED", "" + id)
            mViewModel.callAssignValet(mCommId, mSchId, id)
        } else {
            AppUtils.showSnackBar(binding.root, getString(R.string.lbl_selected_valets))
        }
    }

    private lateinit var binding: ActivityListOfValetsBinding
    private lateinit var adapter: ListofValetAdapter

    private var currentPage = 1
    private var canLoadMore = true
    private var isLoading = false
    private val mViewModel: ListOfValetsViewModel by lazy {
        ViewModelProviders.of(this).get(ListOfValetsViewModel::class.java)
    }

    override fun getViewModel(): ListOfValetsViewModel {
        return mViewModel
    }

    private val mCommId: String by lazy {
        intent.getStringExtra(AppConstants.COMM_ID)
    }
    private val mSchId: String by lazy {
        intent.getStringExtra(AppConstants.SCHID)
    }

    private lateinit var sch_valets: ArrayList<SchValet?>

    companion object {
        fun newInstance(
            context: Context,
            commId: String,
            schId: String, valetsData: String
        ): Intent {
            val intent = Intent(context, ListOfValetsActivity::class.java)
            intent.putExtra(AppConstants.COMM_ID, commId)
            intent.putExtra(AppConstants.SCHID, schId)
            intent.putExtra(AppConstants.VALETDATA, valetsData)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_list_of_valets)
        var valet = intent.getStringExtra(AppConstants.VALETDATA)
        val gson = Gson()
        val type = object : TypeToken<ArrayList<SchValet?>>() {}.type
        sch_valets = gson.fromJson(valet, type)
        init()
    }

    private fun init() {
        initToolBar()
        setObserver()
        setValetsAdapter()
        setTouch()

        mViewModel.getValetList(
            mCommId,
            mSchId,
            currentPage.toString(),
            binding.edtSearch.text.toString()
        )
    }

    private fun initToolBar() {
        setToolbarBackground(R.color.blue_toolbar)
        setToolbarTitle(R.string.lbl_list_of_valets)
        setToolbarLeftIcon(R.drawable.ic_back_white, this)
        setToolbarRightMenuIcon(R.drawable.ic_assign_text, this)
    }

    private fun setValetsAdapter() {
        adapter = ListofValetAdapter(this, sch_valets)
        adapter.filterable=true
        binding.rvValet.adapter = adapter
        adapter.itemClickListener = this
    }

    private fun setObserver() {
        mViewModel.getResponse().observe({ this.lifecycle }, { response: ListOfValetsResponse? ->
            if (response!!.status) {
                binding.NoData.visibility=View.GONE
                binding.rvValet.visibility=View.VISIBLE
                adapter.setItem(response!!.result)
                adapter.notifyDataSetChanged()
            } else {
                canLoadMore = false
                if (currentPage==1){
                    binding.NoData.text=response.message
                    binding.NoData.visibility=View.VISIBLE
                    binding.rvValet.visibility=View.GONE
                }

            }
        })

        mViewModel.getValetAssignResponse().observe({ this.lifecycle }, { response: Boolean? ->
            val returnIntent = Intent()
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouch() {

        binding.edtSearch.addTextChangedListener(object : TextWatcher {

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

                (binding.rvValet.adapter as ListofValetAdapter).filter(s.toString().toLowerCase())

            }
        })

    }

}
