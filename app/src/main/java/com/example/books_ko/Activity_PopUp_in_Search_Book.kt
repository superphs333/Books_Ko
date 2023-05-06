package com.example.books_ko

import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.books_ko.Data.Data_Search_Book
import com.example.books_ko.databinding.ActivityPopUpInSearchBookBinding

class Activity_PopUp_in_Search_Book : AppCompatActivity() {

    private lateinit var binding: ActivityPopUpInSearchBookBinding
    lateinit var book : Data_Search_Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopUpInSearchBookBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 상태바 제거(전체화면 모드)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // sp
    }
}