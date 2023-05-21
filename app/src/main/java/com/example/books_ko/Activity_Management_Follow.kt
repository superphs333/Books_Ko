package com.example.books_ko

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.books_ko.databinding.ActivityManagementFollowBinding

class Activity_Management_Follow : AppCompatActivity() {

    private lateinit var binding : ActivityManagementFollowBinding
    var email = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagementFollowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email").toString()
        Log.i("정보태그","email->{$email}")

        // 버튼 초기색
        binding.btnFollower.setBackgroundColor(Color.parseColor(getString(R.string.choose_true)))
        binding.btnFollowing.setBackgroundColor(Color.parseColor(getString(R.string.choose_false)))

        /*
        버튼 -> 해당 버튼에 알맞는 follow를 불러온다 and 색상변경
         */

    }

    fun onFollowClick(view: View) {
        var sort = ""
        var btnFollowerColor = Color.parseColor(getString(R.string.choose_true))
        var btnFollowingColor = Color.parseColor(getString(R.string.choose_true))
        when (view.getId()) {
            R.id.btn_follower -> {
                sort = "follower";
                btnFollowerColor = Color.parseColor(getString(R.string.choose_true))
                btnFollowingColor = Color.parseColor(getString(R.string.choose_false))
            }
            R.id.btn_following ->{
                sort = "following";
                btnFollowerColor = Color.parseColor(getString(R.string.choose_false))
                btnFollowingColor = Color.parseColor(getString(R.string.choose_true))
            }
        }
        // 버튼색
        binding.btnFollower.setBackgroundColor(btnFollowerColor)
        binding.btnFollowing.setBackgroundColor(btnFollowingColor)
        // 데이터 불러오기
    }
}