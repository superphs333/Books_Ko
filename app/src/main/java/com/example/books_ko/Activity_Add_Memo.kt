package com.example.books_ko

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.books_ko.Adapter.AdapterMyBook
import com.example.books_ko.Adapter.Adapter_Img_Memo
import com.example.books_ko.Data.DataMyBook
import com.example.books_ko.Data.Data_Img_Memo
import com.example.books_ko.Function.AboutPicture
import com.example.books_ko.databinding.ActivityAddMemoBinding

class Activity_Add_Memo : AppCompatActivity() {

    // 이건 oncreate 밖에
    private lateinit var binding: ActivityAddMemoBinding

    val ap = AboutPicture

    var book_idx = 0 // 책 idx
    var memo_idx = 0 // 메모 idx

    // 리사이클러뷰
    var arrayList: ArrayList<Data_Img_Memo>? = ArrayList()
    private lateinit var adapterMyBook : Adapter_Img_Memo
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        book_idx = intent.getIntExtra("book_idx", 0)
        Log.i("정보태그", "book_idx=>$book_idx")
        binding.txtTitle.text = intent.getStringExtra("title") // 제목셋팅
        // spinner셋팅
        val data = resources.getStringArray(R.array.select_memo_view)
        val adapterSpinner = ArrayAdapter(applicationContext, android.R.layout.simple_dropdown_item_1line, data)
        binding.spinnerSelectOpen.adapter = adapterSpinner

        /*
        리사이클러뷰 셋팅
         */
        // 변수 초기화
        linearLayoutManager = LinearLayoutManager(applicationContext)
        adapterMyBook = Adapter_Img_Memo(arrayList!!, applicationContext, this)
        binding!!.rvMemoImgs.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = adapterMyBook
            // list변경될 때
            // adapterMyBook.dataMyBooks = arrayList!!
            //                adapterMyBook.notifyDataSetChanged()
        }


    }

    fun send_to_SERVER(view: View) {}
    fun Pick_From_Camera(view: View) {}
    fun Pick_From_Gallery(view: View) {}
}