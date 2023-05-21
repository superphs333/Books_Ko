package com.example.books_ko

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.ActivityMain2Binding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Activity_Main2 : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    val am = AboutMember

    var fragmentManager: FragmentManager? = null
    var fragmentTransaction: FragmentTransaction? = null

    // 프래그먼트
    var fragment_books: Fragment_Books? = null
    var fragment_mypage: Fragment_mypage? = null
    var fragment_chatting_room: FragmentChattingRoom? = null
    var fragment_sns: FragmentSns? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
        FCM - 현재 등록 토큰 가져오기
         */
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener<String> { task ->
                if (!task.isSuccessful) {
                    Log.w("정보태그", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.i("정보태그", "token=>$token")

                // email
                GlobalScope.launch {
                    email = AboutMember.getEmailFromRoom(applicationContext)
                    Log.i("정보태그","(AboutMember.getEmailFromRoom로 가져온)email->$email")
                    am.Change_Member_Info(applicationContext,this@Activity_Main2,"sender_id", token, email)
                }

            })

        // 프래그먼트 매니저 선언 : 프래그먼트 트랜잭션 수행(프래그먼트 추가/삭제/교체)을 하기 위해 필요
        fragmentManager = supportFragmentManager
        // 추가 시켜 줄 프래그먼트 객체 생성
        fragment_books = Fragment_Books()
        fragment_mypage = Fragment_mypage()
        fragment_chatting_room = FragmentChattingRoom()
        fragment_sns = FragmentSns()
        // 프래그먼트 트랜잭션 시작 : 프래그먼트 트랜잭션, 백스택, 애니메이션 등을 설정함
        fragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction!!.replace(R.id.frameLayout, fragment_books!!).commitAllowingStateLoss()
    }

    // 각 버튼을 클릭하면 해당 프래그먼트들이 보인다
    fun clickHandler(view: View){
        fragmentTransaction = fragmentManager!!.beginTransaction()
        Log.d("정보태그", "clickHandler: ${fragmentTransaction}")
        when (view.id) {
            R.id.btn_books -> fragmentTransaction!!.replace(
                R.id.frameLayout,
                fragment_books!!
            ).commitAllowingStateLoss()

            R.id.btn_mypage -> fragmentTransaction!!.replace(R.id.frameLayout, fragment_mypage!!)
                .commitAllowingStateLoss()

            R.id.btn_gathering -> fragmentTransaction!!.replace(
                R.id.frameLayout,
                fragment_chatting_room!!
            ).commitAllowingStateLoss()

            R.id.btn_feed -> fragmentTransaction!!.replace(
                R.id.frameLayout,
                fragment_sns!!
            ).commitAllowingStateLoss()
//
//            R.id.btn_feed -> fragmentTransaction!!.replace(R.id.frameLayout, fragment_sns)
//                .commitAllowingStateLoss()
        }
    }
}