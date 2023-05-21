package com.example.books_ko

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.books_ko.Adapter.AdapterBookMemo
import com.example.books_ko.Adapter.AdapterFollowPeople
import com.example.books_ko.Data.DataFollowPeople
import com.example.books_ko.Data.Data_Book_Memo
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Interface.JsonPlaceHolderApi
import com.example.books_ko.databinding.ActivityManagementFollowBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.ArrayList

class Activity_Management_Follow : AppCompatActivity() {

    private lateinit var binding : ActivityManagementFollowBinding
    var email = ""
    var sort = "follower"

    // 리사이클러뷰
    var arrayList: ArrayList<DataFollowPeople> = ArrayList()
    private lateinit var mainAdapter : AdapterFollowPeople
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManagementFollowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        email = intent.getStringExtra("email").toString()
        Log.i("정보태그","email->{$email}")

        /*
        리사이클러뷰 변수
         */
        linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL,false)
        mainAdapter = AdapterFollowPeople(arrayList!!, applicationContext, this,email)
        binding!!.rvFollows.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            this@apply.adapter = mainAdapter
        }

        // 버튼 초기색
        binding.btnFollower.setBackgroundColor(Color.parseColor(getString(R.string.choose_true)))
        binding.btnFollowing.setBackgroundColor(Color.parseColor(getString(R.string.choose_false)))


    }

    override fun onResume() {
        super.onResume()

        // 데이터 불러오기
        lifecycleScope.launch {
            getFollowPeoples()
        }

    }

    fun onFollowClick(view: View) {
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
        lifecycleScope.launch {
            getFollowPeoples()
        }

    }

    suspend fun  getFollowPeoples()= withContext(Dispatchers.IO) {
        val retrofit = Retrofit.Builder()
            .baseUrl(applicationContext.getString(R.string.server_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val myApi = retrofit.create(JsonPlaceHolderApi::class.java)
        val accept_sort = "Get_Follow_People"

        // 보내는 값 확인
        Log.d("정보태그", "email: $email, sort: $sort")


        try {
            val response = myApi.Get_Follow(accept_sort, email,sort).execute()
            Log.i("정보태그",response.body()!!.data!!.followList.toString())
            if (response.isSuccessful) {
                val result = response.body()
                if (result?.status == "success"){
                    withContext(Dispatchers.Main) {
                        mainAdapter.dataList = response.body()?.data?.followList as ArrayList<DataFollowPeople>
                        mainAdapter.notifyDataSetChanged()

                        // 명수 셋팅
                        val size = mainAdapter?.dataList?.size?.toString() ?: "0"
                        binding.txtCount.setText("$size"+"명")

                    }

                } else {
                    Log.i("정보태그", "[getFollowPeoples]서버에 연결은 되었으나 오류발생")
                    null
                }
            } else {
                Log.i("정보태그", "[getMemo]result.staus isSuccessful X")
                null
            }
        } catch (e: IOException) {
            null
        }
    }
}