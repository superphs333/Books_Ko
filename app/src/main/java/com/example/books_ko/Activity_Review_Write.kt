package com.example.books_ko

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.databinding.ActivityReviewWriteBinding
import kotlinx.coroutines.launch
import com.example.books_ko.ActivityDetailMyBook


class Activity_Review_Write : AppCompatActivity() {
    private lateinit var binding: ActivityReviewWriteBinding
    var idx = 0 // 도서 idx
    var email = ""

    val fc = FunctionCollection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idx = intent.getIntExtra("idx",0) // idx값
        email = intent.getStringExtra("email")!!
        binding.editReview.setText(intent.getStringExtra("review")) // 리뷰셋팅

    }

    // 리뷰 서버로 보내기
    fun Review_Send(view: View) {
        val map = mapOf(
            "sort" to "review", // 변경할 값
            "input" to binding.editReview.text.toString(), // 변경후 내용
            "email" to email,
            "book_idx" to idx.toString()
        )
        lifecycleScope.launch {
            val goServer = fc.goServer(applicationContext, "edit_my_book",map as MutableMap<String, String>)
            if(goServer){
                Toast.makeText(applicationContext, "리뷰가 변경되었습니다.", Toast.LENGTH_SHORT).show()

                // Intent
                val intent = Intent(applicationContext, ActivityDetailMyBook::class.java)
                intent.putExtra("review", binding.editReview.text.toString())
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

    }
}