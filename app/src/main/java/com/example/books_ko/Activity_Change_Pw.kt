package com.example.books_ko

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Function.AboutMember.Change_Member_Info
import com.example.books_ko.databinding.ActivityChangePwBinding

class Activity_Change_Pw : AppCompatActivity() {
    private lateinit var binding: ActivityChangePwBinding

    val am=AboutMember
    var email=""
    var from=""
    var activity = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePwBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 어디서 왔는지 분기
        from = intent.getStringExtra("from")!!
        Log.i("정보태그","from->{$from}")
        email = intent.getStringExtra("email")!!
        Log.i("정보태그","email->{$email}")

        /*
        비밀번호 정규식에 일치하는지 확인하기
         */
        binding.editPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                // 비밀번호 정규식 확인
                if (am.chk_regex("pw", binding.editPw)) {
                    binding.txtPwInfo.text = "사용가능한 비밀번호 입니다."
                } else {
                    binding.txtPwInfo.text = "비밀번호 형식을 확인해주세요."
                }

                // 비밀번호 = 비밀번호 확인 문구 일치 여부 (비밀번호 확인 문구 부분이 빈칸이 아닌 경우에)
                if (!binding.editPwChk.text.toString().equals("")) {
                    if (binding.editPw.text.toString()
                            .equals(binding.editPwChk.text.toString())
                    ) {
                        binding.txtChkInfo.text = "비밀번호가 일치합니다"
                    } else {
                        binding.txtChkInfo.text = "입력한 비밀번호를 확인해주세요"
                    }
                }
            }
        })


        /*
        비밀번호=비밀번호 확인 체크하기
         */
        binding.editPwChk.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (binding.editPw.text.toString().equals(binding.editPwChk.text.toString())) {
                    binding.txtChkInfo.text = "비밀번호가 일치합니다"
                } else {
                    binding.txtChkInfo.text = "입력한 비밀번호를 확인해주세요"
                }
            }
        })


    }

    /*
    변경버튼(btn_chk) 클릭
     */
    fun Pw_Change(view : View){
        // 비밀번호 정규식 확인
        if (!am.chk_regex("pw", binding.editPw)) {
            Toast.makeText(applicationContext, getString(R.string.toast_pw), Toast.LENGTH_SHORT)
                .show()
            return
        }

        // 비밀번호 = 비밀번호 확인 문구 일치 여부
        if (!binding.editPw.text.toString().equals(binding.editPwChk.text.toString())) {
            Toast.makeText(
                applicationContext,
                getString(R.string.toast_pw_double_chk),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 비밀번호 변경
        am.Change_Member_Info(applicationContext,activity,"pw", binding.editPw.text.toString(), email)
    }


}


