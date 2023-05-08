package com.example.books_ko

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.books_ko.databinding.ActivityDetailMyBookBinding

class ActivityDetailMyBook : AppCompatActivity() {

    private lateinit var binding: ActivityDetailMyBookBinding
    var idx = 0

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

    }

    fun go_to_Activity_Review_Write(view: View) {}
    fun more_memos(view: View) {}
    fun go_to_Activity_Add_Memo(view: View) {}
}