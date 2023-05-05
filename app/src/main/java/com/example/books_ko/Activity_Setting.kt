package com.example.books_ko

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.books_ko.Data.UserData
import com.example.books_ko.DataBase.UserDatabase
import com.example.books_ko.databinding.ActivitySettingBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private lateinit var binding: ActivitySettingBinding
private lateinit var database : UserDatabase
var email=""
private var mAuth: FirebaseAuth? = null


class Activity_Setting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            // 데이터베이스에 접근하고 원하는 데이터를 가져옴
            withContext(Dispatchers.IO) {
                database = Room.databaseBuilder(applicationContext, UserDatabase::class.java, "app_database").build()
                val userData: UserData = database.userDao().getUser2()
                Log.i("정보태그","userData->${userData}")
                email = userData.email
                val platformType = userData.platform_type
                if(platformType!=applicationContext.getString(R.string.normal)){ // 일반 로그인 외에는 비밀번호 변경 버튼 숨김
                    withContext(Dispatchers.Main) {
                        binding.btnChangePw.visibility = View.GONE
                    }
                }
            }
        }


    }

    /*
    비밀번호 변경
    : 현재 비밀번호 입력 -> (일치하면) Change_Pw 액티비티로 이동(비밀번호 변경)
    1. 비밀번호 서버에서 가져오기
    2. alert창 불러와서 비교
    3. 일치하는 경우에만 비밀번호 변경 페이지로 이동
     */
    fun change_pw(view: View) {

        GlobalScope.launch {
            val pw = am.getMemberInfo(applicationContext, email, "pw")
            Log.i("정보태그","서버에서 가져온 pw -> $pw")

            /*
            alert창 불러와서 비교
             */
            withContext(Dispatchers.Main) { // UI스레드에서 작업 수행ㅜ
                // UI 스레드에서 실행될 코드
                val alert = AlertDialog.Builder(this@Activity_Setting)
                alert.setTitle("알림")
                alert.setMessage("현재 비밀번호를 입력하세요")

                val edit_pw = EditText(this@Activity_Setting)
                alert.setView(edit_pw)
                alert.setPositiveButton("확인",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        val input_pw = edit_pw.text.toString()
                        Log.i("정보태그", "input_pw=>$input_pw")

                        // 현재 비밀번호와 입력값 일치시에만 페이지 이동
                        if (pw != input_pw) { // 불일치
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.toast_pw),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@OnClickListener
                        }

                        // 페이지 이동
                        val intent = Intent(applicationContext, Activity_Change_Pw::class.java)
                        intent.putExtra("email",email)
                        intent.putExtra("from","setting")
                        startActivity(intent)
                    })
                alert.show()
            }
        }
    }

    // 로그아웃
    fun logout(view: View){
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth!!.currentUser
        if(currentUser!=null){
            Log.i("정보태그","구글로 로그인 한 회원")
            // 구글로 로그인 한 회원
            FirebaseAuth.getInstance().signOut()
        }else{ // 이 아래부분은 일반 로그인 분기 잘 타는지 확인 후 지워도 됨
            Log.i("정보태그","일반 로그인 한 회원")

        }
        delete_and_intent()
    }

    private fun delete_and_intent() {
        // 데이터베이스에 저장되어 있는 user정보 삭제
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                database = Room.databaseBuilder(applicationContext, UserDatabase::class.java, "app_database").build()
                database.userDao().clearAllUsers()
            }
            // MainAcitvity 페이지로 이동
            val intent = Intent(applicationContext, MainActivity::class.java)
            ActivityCompat.finishAffinity(this@Activity_Setting)
            startActivity(intent)
        }
    }
}