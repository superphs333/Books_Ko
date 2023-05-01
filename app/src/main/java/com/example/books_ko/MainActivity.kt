package com.example.books_ko

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val am = AboutMember

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }

    /*
    로그인 버튼 -> 아이디, 비밀번호 확인후
        - 맞으면 -> 다음 페이지로 이동
        - 틀리면 -> 다시 한 번 확인해주세요 알림
     */
    fun login(view: View) {
        // 이메일 유효성 확인
        binding.editEmail.takeIf { !am.chk_regex("email", it) }?.let {
            Toast.makeText(applicationContext, "이메일을 확인해주세요", Toast.LENGTH_SHORT).show()
            it.requestFocus()
            return
        }

        // 비밀번호 유효성 확인
        binding.editPw.takeIf { !am.chk_regex("pw", it) }?.let {
            Toast.makeText(applicationContext, "비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show()
            it.requestFocus()
            return
        }

        /*
        로그인 정보 있는지 확인
         */

        /*
        로그인 정보 있는지 확인
         */
        am.chk_login(
            binding.editEmail,
            binding.editPw,
            binding.chkAutologin.isChecked
        )

    }
    // 회원 가입 페이지 이동
    fun go_to_Signup_Page(view: View) {
        //val Intent = Intent(this,)
        val intent = Intent(this,Activity_Signup::class.java);
        startActivity(intent);
    }
    fun find_pw(view: View) {}
}
