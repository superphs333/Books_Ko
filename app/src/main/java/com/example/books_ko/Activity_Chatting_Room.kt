package com.example.books_ko

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.books_ko.Adapter.Adapter_Img_Memo
import com.example.books_ko.Data.Data_Img_Memo
import com.example.books_ko.databinding.ActivityAddChattingRoomBinding
import com.example.books_ko.databinding.ActivityChattingRoomBinding
import java.util.ArrayList
import kotlin.properties.Delegates

class Activity_Chatting_Room : AppCompatActivity() {

    private lateinit var binding : ActivityChattingRoomBinding
    var room_idx = 0

    var arrayList: ArrayList<Data_Img_Memo> = ArrayList()
    private lateinit var adapterImgMemo : Adapter_Img_Memo
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var helper : ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 방 idx가져오기
        room_idx = intent.getIntExtra("room_idx",0)
        Log.i("정보태그","room_idx->$room_idx")

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
    }
}