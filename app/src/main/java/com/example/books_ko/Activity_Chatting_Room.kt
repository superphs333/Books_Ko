package com.example.books_ko

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.books_ko.Adapter.Adapter_Img_Memo
import com.example.books_ko.Data.Data_Img_Memo
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.databinding.ActivityChattingRoomBinding
import kotlinx.coroutines.launch

class Activity_Chatting_Room : AppCompatActivity() {

    private lateinit var binding : ActivityChattingRoomBinding
    var room_idx = 0
    var leader= ""
    var total_count = 0
    var join_count = 0

    var arrayList: ArrayList<Data_Img_Memo> = ArrayList()
    private lateinit var adapterImgMemo : Adapter_Img_Memo
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var helper : ItemTouchHelper

    val fc = FunctionCollection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 방 idx가져오기
        room_idx = intent.getIntExtra("room_idx",0)
        Log.i("정보태그","[Activity_Chatting_Room]room_idx->$room_idx")

        // 리사이클러뷰 셋팅
        linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL,false)
        adapterImgMemo = Adapter_Img_Memo(arrayList!!, applicationContext, this)
        binding!!.rvPeoples.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = adapterImgMemo
            // list변경될 때
            // adapterMyBook.dataMyBooks = arrayList!!
            //                adapterMyBook.notifyDataSetChanged()
        }

        /*
        채팅방 데이터 셋팅
         */
        val map: MutableMap<String, String> = HashMap()
        map["idx"] = room_idx.toString() // room_idx
        lifecycleScope.launch {
            val goServerForResult = fc.goServerForResult(applicationContext, "get_chatting_room_info", map)

            if(goServerForResult["status"]=="success"){

                val data: Map<String, Any> = goServerForResult["data"] as Map<String, Any>
                val room_info : Map<String, String> = data["room_info"] as Map<String, String>
                binding.txtTitle.setText(room_info["title"])
                binding.txtExplain.setText(room_info["room_explain"])
                binding.txtTotal.setText(room_info["total_count"])
                binding.txtCount.setText(room_info["join_count"])
                leader = room_info["leader"]!!
                total_count = room_info["total_count"]!!.toInt()
                join_count = room_info["join_count"]!!.toInt()



            }else{
                Toast.makeText(
                    applicationContext,
                    getString(R.string.toast_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }
}