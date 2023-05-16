package com.example.books_ko

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.books_ko.databinding.ActivityBookMemosBinding

class Activity_Book_Memos : AppCompatActivity() {

    private lateinit var binding: ActivityBookMemosBinding

    var book_idx = 0 // 책 고유값


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookMemosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        book_idx = intent.getIntExtra("book_idx",0) // 책 idx 셋팅
        Log.i("정보태그","book_idx->$book_idx")
        binding.txtTitle.text = intent.getStringExtra("title") // 제목 셋팅

        /*
        spinner셋팅
         */
        val data = resources.getStringArray(R.array.select_memo_view)
        val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_dropdown_item_1line, data)
        binding.spinSort.adapter = adapter

    }

    // 메모를 추가 할 수 있는 액티비티로 이동(Activity_Add_Memo)
    fun go_to_Activity_Add_Memo(view: View) {
        val intent = Intent(applicationContext, Activity_Add_Memo::class.java)
        intent.putExtra("book_idx", book_idx)
        intent.putExtra("title", getIntent().getStringExtra("title"))
        startActivity(intent)
    }
}