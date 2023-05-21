package com.example.books_ko

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.books_ko.Adapter.AdapterBookMemo
import com.example.books_ko.Data.Data_Book_Memo
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Function.AboutMemo
import com.example.books_ko.databinding.FragmentSnsBinding
import kotlinx.coroutines.launch


class FragmentSns : Fragment() {

    private lateinit var binding : FragmentSnsBinding
    private lateinit var context: Context
    private lateinit var activity: Activity

    // 리사이클러뷰
    var arrayList: ArrayList<Data_Book_Memo> = ArrayList()
    private lateinit var mainAdapter : AdapterBookMemo
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)





    }

    override fun onResume() {
        super.onResume()



    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSnsBinding.inflate(layoutInflater, container, false)
        val view: View = binding!!.getRoot()

        activity = requireActivity()
        context = requireContext()

        /*
        spinner셋팅
         */
        binding.spinnerOpen.adapter = ArrayAdapter.createFromResource(
            context,
            R.array.select_open2,
            android.R.layout.simple_dropdown_item_1line
        )

        /*
        리사이클러뷰 변수
         */
        linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        mainAdapter = AdapterBookMemo(arrayList!!, context, activity,email)
        binding!!.rvBookMemos.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            this@apply.adapter = mainAdapter
        }


        // spinner변경 -> 해당하는 데이터 불러오기
        binding.spinnerOpen.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                bringMemoDatas()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // chk -> 좋아요한 게시글 보기
        binding.chkLikePost.setOnCheckedChangeListener { buttonView, isChecked ->
            bringMemoDatas()
        }




        return binding?.root
    }

     fun bringMemoDatas(){
        val selectedValue = binding.spinnerOpen.selectedItem.toString()
        val selectedIndex = binding.spinnerOpen.selectedItemPosition
        val isCheckBoxChecked = binding.chkLikePost.isChecked
        Log.i("정보태그","$selectedIndex : $selectedValue")
        Log.i("정보태그","likeChk : {$isCheckBoxChecked}")

        // 메모 데이터 불러오기
        lifecycleScope.launch {
            email = AboutMember.getEmailFromRoom(context)
            mainAdapter.email = email
            Log.i("정보태그","email->$email")
            arrayList = AboutMemo.getMemo(context,email,0,selectedIndex+1,isCheckBoxChecked)!!
            mainAdapter.dataList = arrayList!!
            mainAdapter.notifyDataSetChanged()
        }
    }



}