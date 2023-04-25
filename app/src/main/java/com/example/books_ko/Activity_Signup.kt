package com.example.books_ko

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.ActivitySignupBinding

class Activity_Signup : AppCompatActivity() {

    // 바인딩 변수
    private lateinit var binding: ActivitySignupBinding // Declare the binding variable
    val am = AboutMember;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater);
        setContentView(binding.root)


    }

    fun send_email(view: View) {

        // 입력받은 이메일
        val input_email: String = binding.editEmail.text.toString()
        Log.i("정보태그", "input_email=>$input_email")

        /*
        이메일 형식인지 확인
         */


        /*
        이메일 중복 여부 확인
         */am.chk_double("email", binding.editEmail, object : AboutMember.VolleyCallback {
            override fun onSuccess(result: Boolean) {
                Log.i("정보태그", "chk_double vollycallback=>$result")
                println("result$result")

            }
        })


    }
}