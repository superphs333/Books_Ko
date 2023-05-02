package com.example.books_ko

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.books_ko.Class.GMailSender
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.ActivityFindPwBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.mail.MessagingException
import javax.mail.SendFailedException

class Activity_Find_Pw : AppCompatActivity() {

    private lateinit var binding: ActivityFindPwBinding
    val am = AboutMember
    var gMailSender: GMailSender? = null // 이메일 보내는 객체
    var temp_string = "" // 임시문자(이메일로 전송할)
    var validate_email = "" // 인증된 이메일(인증코드를 전송할 때의 이메일)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindPwBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 이메일 객체
        gMailSender = GMailSender()

    }

    /*
    인증문자 전송 : 정규식 확인하고, 존재하는 이메일인 경우에만 해당 이메일로 인증번호 전송
     */
    fun send_email(view: View){
        // 이메일 유효성 확인
        if (!am.chk_regex("email", binding.editEmail)) {
            Toast.makeText(applicationContext, "이메일을 확인해주세요", Toast.LENGTH_SHORT).show()
            binding.editEmail.requestFocus()
            return
        }
        // 존재하는 이메일인지 확인
        am.chk_double("email", binding.editEmail, object : AboutMember.VolleyCallback {
            override fun onSuccess(result: Boolean) {
                Log.i("정보태그", "chk_double vollycallback=>$result")
                var set_validate_email = false

                // 중복이 아닌 경우에만 이메일 전송
                if (result) {
                    Log.i("정보태그", "존재하는 이메일")
                    /*
                    이메일 전송
                     */
                    // 임시문자 생성
                    temp_string = gMailSender!!.getEmailCode().toString()
                    Log.i("정보태그","임시문자->${temp_string}")
                    // 이메일 전송
                    val email_title: String = getString(R.string.app_title) + "에서 온 인증문자입니다"
                    val email_content = "다음의 인증문자를 입력하세요 :$temp_string"
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            gMailSender!!.sendMail(
                                email_title,
                                email_content,
                                binding.editEmail.text.toString()
                            )
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    applicationContext,
                                    "이메일이 전송되었습니다!",
                                    Toast.LENGTH_LONG
                                ).show()
                                set_validate_email=true
                            }
                        } catch (e: SendFailedException) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    applicationContext,
                                    "이메일 형식이 잘못되었습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: MessagingException) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    applicationContext,
                                    "인터넷 연결을 확인해주십시오+",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("정보태그", "MessagingException=>" + e.message)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    applicationContext,
                                    "예상치 못한 문제가 발생하였습니다. 다시 한 번 시도해주세요",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("정보태그", "Exception=>" + e.message)
                            }
                        }
                    }
                }else{
                    Log.i("정보태그", "존재하지 않는 이메일")
                    Toast.makeText(
                        applicationContext,
                        "존재하지 않는 이메일입니다",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                if(set_validate_email){
                    // 인증된 이메일 셋팅
                    validate_email = binding.editEmail.text.toString()
                }
            }
        },"findPw")
    }

    /*
    확인 클릭
    - 인증문자가 ""이 아닌지 확인
    - 입력된 인증문자가 맞는지 확인한다
        - 맞다면 -> 비밀번호 변경 액티비티(Activity_Change_Pw)로 이동
        - 틀리다면 -> toast
     */
    fun validate_string_confirm(view: View){
        // 인증문자 빈값인지 확인
        if(temp_string == ""){
            Toast.makeText(
                applicationContext,
                getString(R.string.no_send_email),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 인증문자가 이메일로 전송된 문자와 동일한지 확인
        if (temp_string != binding.editChkChar.text.toString()) {
            Toast.makeText(applicationContext, getString(R.string.sendtext_no), Toast.LENGTH_SHORT)
                .show()
            return
        }

    }
}