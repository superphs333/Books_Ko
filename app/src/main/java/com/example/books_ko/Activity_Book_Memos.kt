package com.example.books_ko

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.books_ko.Adapter.AdapterBookMemo
import com.example.books_ko.Data.Data_Book_Memo
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Function.AboutMemo
import com.example.books_ko.databinding.ActivityBookMemosBinding
import kotlinx.coroutines.launch
import java.util.ArrayList

class Activity_Book_Memos : AppCompatActivity() {

    private lateinit var binding: ActivityBookMemosBinding

    var book_idx = 0 // 책 고유값
    val amemo = AboutMemo

    // 리사이클러뷰
    var arrayList: ArrayList<Data_Book_Memo> = ArrayList()
    private lateinit var mainAdapter : AdapterBookMemo
    private lateinit var linearLayoutManager: LinearLayoutManager

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

        // spin 변경 -> 해당 데이터 불러오기
        binding.spinSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val selectedValue = binding.spinSort.selectedItem.toString()
                val selectedIndex = binding.spinSort.selectedItemPosition
                Log.i("정보태그","$selectedIndex : $selectedValue")

                // 메모 데이터 불러오기
                lifecycleScope.launch {
                    email = AboutMember.getEmailFromRoom(applicationContext)
                    mainAdapter.email = email
                    Log.i("정보태그","email->$email")
                    arrayList = amemo.getMemo(applicationContext,email,book_idx,selectedIndex+1)!!
                    mainAdapter.dataList = arrayList!!
                    mainAdapter.notifyDataSetChanged()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        /*
        리사이클러뷰 변수
         */
        linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL,false)
        mainAdapter = AdapterBookMemo(arrayList!!, applicationContext, this,email)
        binding!!.rvBookMemos.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            this@apply.adapter = mainAdapter
        }

    }

    override fun onResume() {
        super.onResume()
    }

    // 메모를 추가 할 수 있는 액티비티로 이동(Activity_Add_Memo)
    fun go_to_Activity_Add_Memo(view: View) {
        val intent = Intent(applicationContext, Activity_Add_Memo::class.java)
        intent.putExtra("book_idx", book_idx)
        intent.putExtra("title", getIntent().getStringExtra("title"))
        startActivity(intent)
    }
}