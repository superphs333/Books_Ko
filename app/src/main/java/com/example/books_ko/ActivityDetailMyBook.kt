package com.example.books_ko

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.books_ko.Adapter.Adapter_Img_Memo
import com.example.books_ko.Data.Data_Img_Memo
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Function.AboutMember.getEmailFromRoom
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.databinding.ActivityDetailMyBookBinding
import kotlinx.coroutines.launch
import java.util.ArrayList

class ActivityDetailMyBook : AppCompatActivity() {

    private lateinit var binding: ActivityDetailMyBookBinding
    var idx = 0
    var email = ""

    private var isInitializedSpinner = false // spinner 초기화 여부
    private var isInitializedStar = false // 별점 초기화 여부

    val am = AboutMember
    val fc = FunctionCollection

    private lateinit var go_review_write: ActivityResultLauncher<Intent>

    // 리사이클러뷰
    var arrayList: ArrayList<Data_Img_Memo> = ArrayList()
    private lateinit var adapterImgMemo : Adapter_Img_Memo
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailMyBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Spinner셋팅
        val data = resources.getStringArray(R.array.read_status)
        val adapter = ArrayAdapter<String>(
            applicationContext, android.R.layout.simple_dropdown_item_1line, data
        )
        binding.categoryReadStatus.adapter = adapter

        // intent에서 정보 불러오기
        idx = intent.getIntExtra("idx", 0)
        Log.i("정보태그","[ActivityDetailMyBook]idx->"+idx)

        //go_review_write
        go_review_write = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                binding.txtReview.text = result.data?.getStringExtra("review") ?: ""
            }
        }

        // email
        lifecycleScope.launch {
            email = getEmailFromRoom(applicationContext)
            Log.i("정보태그","email->$email")
        }

        /*
        값 셋팅
         */
        // 제목
        binding.txtTitle.text = intent.getStringExtra("title")
        // 책 이미지
        var imgBookCover = intent.getStringExtra("thumbnail")
        if (imgBookCover.isNullOrEmpty()) {
            binding.imgBook.setImageResource(R.drawable.basic_book_cover)
        } else {
            if (imgBookCover.contains(getString(R.string.img_book))) {
                imgBookCover = getString(R.string.server_url) + imgBookCover
            }
            Glide.with(applicationContext).load(imgBookCover).into(binding.imgBook)
        }
        // 작가
        binding.txtAuthors.text = intent.getStringExtra("authors")
        // 출판사
        binding.txtPublisher.text = intent.getStringExtra("publisher")
        // 내용
        binding.txtContents.text = intent.getStringExtra("contents")
        // 리뷰
        binding.txtReview.text = intent.getStringExtra("review")
        // 별점
        binding.ratingBar.rating = intent.getFloatExtra("rating", 0f)
        // 읽음상태
        when (intent.getIntExtra("status", 0)) {
            3 -> binding.categoryReadStatus.setSelection(0) // 읽고싶은
            1 -> binding.categoryReadStatus.setSelection(1) // 읽는중
            2 -> binding.categoryReadStatus.setSelection(2) // 읽음
        }

        /*
        리사이클러뷰 변수
         */
//        linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL,false)
//        adapterImgMemo = Adapter_Img_Memo(arrayList!!, applicationContext, this)
//        binding!!.rvBookMemos.apply {
//            setHasFixedSize(true)
//            layoutManager = linearLayoutManager
//            adapter = adapterImgMemo
//            // list변경될 때
//            // adapterMyBook.dataMyBooks = arrayList!!
//            //                adapterMyBook.notifyDataSetChanged()
//        }

        /*
        카테고리 변경시 -> 반영
         */
        binding.categoryReadStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, i: Int, l: Long) {
                if (isInitializedSpinner) { // 초기화 이후에만 동작하도록 함
                    val status: Int = when (binding.categoryReadStatus.selectedItem.toString()) {
                        getString(R.string.read_bucket) -> 3
                        getString(R.string.read_reading) -> 1
                        else -> 2
                    }
                    /*
                    데이터베이스 반영
                     */
                    // 보낼값
                    val map: MutableMap<String, String> = HashMap()
                    map["sort"] = "status" // 변경할 값
                    map["book_idx"] = idx.toString() // 책 idx
                    map["input"] = status.toString() // 변경할 값
                    map["email"] = email
                    lifecycleScope.launch {
                        val goServer = fc.goServer(applicationContext, "edit_my_book", map)
                        if(goServer){
                            Toast.makeText(applicationContext, "읽음 상태가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    isInitializedSpinner = true // 초기화 완료
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                // 보낼값
                val map: MutableMap<String, String> = HashMap()
                map["sort"] = "rating" // 변경할 값
                map["book_idx"] = idx.toString() // 책 idx
                map["input"] = binding.ratingBar.rating.toString() // 변경할 값
                map["email"] = email
                lifecycleScope.launch {
                    val goServer = fc.goServer(applicationContext, "edit_my_book", map)
                    if(goServer){
                        Toast.makeText(applicationContext, "별점 상태가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        }



    }

    override fun onResume() {
        super.onResume()

        /*
        메모 데이터 불러오기
         */

    }

    /*
    서평쓰기 액티비티 이동
     */
    fun go_to_Activity_Review_Write(view: View) {
        val intent = Intent(applicationContext, Activity_Review_Write::class.java)
        intent.putExtra("idx", idx)
        intent.putExtra("email", email)
        intent.putExtra("review", binding.txtReview.text.toString())
        go_review_write.launch(intent)
    }

    // 메모 엑티비티로 이동
    fun more_memos(view: View) {
        val intent = Intent(applicationContext, Activity_Book_Memos::class.java)
        intent.putExtra("book_idx", idx)
        intent.putExtra("title", binding.txtTitle.text.toString())
        startActivity(intent)
    }
    fun go_to_Activity_Add_Memo(view: View) {}
}