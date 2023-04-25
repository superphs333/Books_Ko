package com.example.books_ko

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun login(view: View) {}
    // 회원 가입 페이지 이동
    fun go_to_Signup_Page(view: View) {
        //val Intent = Intent(this,)
        val intent = Intent(this,Activity_Signup::class.java);
        startActivity(intent);
    }
    fun find_pw(view: View) {}
}
