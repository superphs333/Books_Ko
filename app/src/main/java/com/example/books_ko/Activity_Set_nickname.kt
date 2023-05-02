package com.example.books_ko

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.ActivitySetNicknameBinding

private lateinit var binding: ActivitySetNicknameBinding


// login정보
var sns_id="";
var profile_url="";
var login_email="";

var why_change = ""; // 닉네임 변경 목적

val am = AboutMember;

var nick_no_double=false


class Activity_Set_nickname : AppCompatActivity() {

    val activity = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_nickname)
        binding = ActivitySetNicknameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
        닉네임 변경 목적 확인 : 회원가입(signup)
         */
        why_change = intent.getStringExtra("why_change")!!
        Log.i("정보태그", "why_change=>$why_change")
        when (why_change) {
            "signup" -> {
                // 회원의 정보값 셋팅
                sns_id = intent.getStringExtra("sns_id")!!
                profile_url = intent.getStringExtra("profile_url")!!
                login_email = intent.getStringExtra("login_email")!!

            }
            "reason2" -> {

            }
        }

        /*
        중복 확인 후에(nick_no_double = true)
        닉네임 입력칸에 입력값이 있을 경우 -> 중복 확인을 다시 해줘야 함
         */
        binding.editNick.addTextChangedListener(object : TextWatcher {
            var before_nick = binding.editNick.text.toString()
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (nick_no_double == true) { // 중복확인 체크가 끝난경우
                    // 만약 이전 닉네임과 같은 경우가 아니라면 nick_no_double=false
                    // + txt_nick_info 변경
                    if (!binding.editNick.equals(before_nick)) {
                        binding.txtNickInfo.text = "중복확인 문구"
                        nick_no_double = false
                    }
                }
            }
        })

    }

    /*
    회원가입 마무리 or 닉네임 변경
     */
    fun sign_up(view : View){


        // 만약, nick_no_double = false인경우 toast하고 함수 빠져나오기
        if (nick_no_double == false) {
            Toast.makeText(applicationContext, "닉네임을 확인해 주세요", Toast.LENGTH_SHORT).show()
            return
        }
        if (why_change == "signup") { // 회원가입
            am.google_sign_up(activity,applicationContext,login_email, sns_id, binding.editNick.text.toString(), profile_url)
        } else if (why_change == "only_change") { // 닉네임변경
           // am.Change_Member_Info("nickname", binding.editNick.text.toString(), login_email)
        }
    }

    fun Check_Nick_Double(view: View){
        // 닉네임 정규식 확인
        if (!am.chk_regex("nickname", binding.editNick)) {
            Toast.makeText(
                applicationContext,
                getString(R.string.toast_nickname),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 닉네임 중복 확인

        // 닉네임 중복 확인
        am.chk_double("nickname", binding.editNick, object : AboutMember.VolleyCallback {
            override fun onSuccess(result: Boolean) {
                Log.i("정보태그", "(닉네임)chk_double vollycallback=>$result")
                if (result) { // 중복 o
                    nick_no_double = false
                    binding.txtNickInfo.text = "사용 불가능한 닉네임입니다"
//                    if (binding.editNick.text.toString().equals(now_nickname)) {
//                        binding.txtNickInfo.text = "현재 닉네임과 같은 닉네임입니다"
//                    }
                } else { // 중복x
                    nick_no_double = true
                    binding.txtNickInfo.text = "사용 가능한 닉네임입니다"
                }
            }
        })
    }
}

