package com.example.books_ko

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.books_ko.Adapter.AdapterMyBook
import com.example.books_ko.Data.DataMyBook
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.FragmentChattingRoomBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentChattingRoom : Fragment() {

    private var binding : FragmentChattingRoomBinding? = null
    private lateinit var context: Context
    private lateinit var activity: Activity
    val am = AboutMember
    var email = ""

    var arrayList: ArrayList<DataMyBook>? = ArrayList()
    private lateinit var adapterMyBook : AdapterMyBook
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.Main).launch {
            email = am.getEmailFromRoom(context)
            Log.i("정보태그","email->"+email)
            // 도서 데이터 불러오기
           // arrayList = ab.getMyBook(context,email,getReadStatus(),binding!!.editSearch.getText().toString())
            Log.i("정보태그","arrayList->$arrayList")

            /*
            리사이클러뷰 셋팅
             */
//            linearLayoutManager = LinearLayoutManager(context)
//            adapterMyBook = AdapterMyBook(arrayList!!, context, activity)
//            binding!!.rvMyBooks.apply {
//                setHasFixedSize(true)
//                layoutManager = linearLayoutManager
//                adapter = adapterMyBook
//            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChattingRoomBinding.inflate(layoutInflater, container, false)
        val view: View = binding!!.getRoot()

        activity = requireActivity()
        context = requireContext()

        /*
        spinner셋팅
         */
        val data = context.resources.getStringArray(R.array.select_chatting_room_view)
        var adapter = ArrayAdapter(context,android.R.layout.simple_dropdown_item_1line,data)
        binding!!.spinSort.adapter = adapter


        /*
        btn_add_room => 채팅방 개설 액티비티 이동
         */
        binding!!.btnAddRoom.setOnClickListener {
            val intent = Intent(context, Activity_Add_Chatting_Room::class.java)
            startActivity(intent)
        }


        return binding?.root
    }


}