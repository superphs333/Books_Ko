package com.example.books_ko

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.books_ko.Class.GMailSender
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.ActivitySignupBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.mail.MessagingException
import javax.mail.SendFailedException

class Activity_Signup : AppCompatActivity() {

    // 바인딩 변수
    private lateinit var binding: ActivitySignupBinding // Declare the binding variable
    val am = AboutMember;


    /*
    이메일용 변수
     */
    var gMailSender: GMailSender? = null // 이메일 보내는 객체
    var temp_email_string=""; // 이메일 인증 문자
    var email_no_double = false; // 이메일 중복 체크 여부
    var temp_email_not_duplication = ""; //  중복 확인 체크한(중복이 아닌) 이메일



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
         */
        am.chk_double("email", binding.editEmail, object : AboutMember.VolleyCallback {
            override fun onSuccess(result: Boolean) {
                Log.i("정보태그", "chk_double vollycallback=>$result")

                // 중복이 아닌 경우에만 이메일 전송
                if(!result){
                    // 중복 확인 여부 셋팅
                    email_no_double = true;
                    // 인증된 이메일 셋팅
                    temp_email_not_duplication = binding.editEmail.text.toString();
                    /*
                    이메일 전송
                     */
                    gMailSender = GMailSender();
                    // 임시문자 생성
                    temp_email_string = gMailSender!!.getEmailCode().toString()
                    // 이메일 전송
                    // 이메일 전송
                    val email_title: String = getString(R.string.app_title) + "에서 온 인증문자입니다"
                    val email_content = "다음의 인증문자를 입력하세요 :$temp_email_string"
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            gMailSender!!.sendMail(
                                email_title,
                                email_content,
                                binding.editEmail.text.toString()
                            )
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "이메일이 전송되었습니다!", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: SendFailedException) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "이메일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: MessagingException) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "인터넷 연결을 확인해주십시오+", Toast.LENGTH_SHORT).show()
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


                }
            }
        })


    }
}