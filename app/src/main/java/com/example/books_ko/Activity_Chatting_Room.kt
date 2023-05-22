package com.example.books_ko

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.books_ko.Adapter.Adapter_Img_Memo
import com.example.books_ko.Adapter.Adapter_Join_People
import com.example.books_ko.Data.Data_Chatting_Room
import com.example.books_ko.Data.Data_Img_Memo
import com.example.books_ko.Data.Data_Join_People
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.Interface.JsonPlaceHolderApi
import com.example.books_ko.databinding.ActivityChattingRoomBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class Activity_Chatting_Room : AppCompatActivity() {

    private lateinit var binding : ActivityChattingRoomBinding
    var room_idx = 0
    var leader : String?= ""
    var total_count = 0
    var join_count = 0
    var email = ""
    var joinState :Boolean = false

    val activity = this

    var arrayList: ArrayList<Data_Join_People> = ArrayList()
    private lateinit var adapterJoinPeople : Adapter_Join_People
    private lateinit var linearLayoutManager: LinearLayoutManager


    val fc = FunctionCollection
    val am = AboutMember

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 리사이클러뷰 셋팅
        lifecycleScope.launch {
            room_idx = intent.getIntExtra("room_idx",0)             // 방 idx가져오기
            Log.i("정보태그","[Activity_Chatting_Room]room_idx->$room_idx")
            // leader정보 intent에 없는 경우는 -> 추가에서 온 경우 : 사용자의 email넣어줌

            leader= intent.getStringExtra("leader")
            if (leader == null) {
                leader = email
            }
            Log.i("정보태그","[Activity_Chatting_Room]leader->$leader")
            email =am.getEmailFromRoom(applicationContext)
            linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL,false)
            adapterJoinPeople = Adapter_Join_People(arrayList!!, applicationContext, activity, email,leader!!)
            binding!!.rvPeoples.apply {
                setHasFixedSize(true)
                layoutManager = linearLayoutManager
                adapter = adapterJoinPeople
            }
        }


        /*
        채팅방 데이터 셋팅
         */
        val map: MutableMap<String, String> = HashMap()
        map["idx"] = room_idx.toString() // room_idx
        lifecycleScope.launch {

            val goServerForResult = fc.goServerForResult(applicationContext, "get_chatting_room_info", map)

            if(goServerForResult["status"]=="success"){

                val data: Map<String, Any> = goServerForResult["data"] as Map<String, Any>
                val room_info : Map<String, String> = data["room_info"] as Map<String, String>
                binding.txtTitle.setText(room_info["title"])
                binding.txtExplain.setText(room_info["room_explain"])
                binding.txtTotal.setText(room_info["total_count"])
                binding.txtCount.setText(room_info["join_count"])
                leader = room_info["leader"]!!
                total_count = room_info["total_count"]!!.toInt()
                join_count = room_info["join_count"]!!.toInt()



            }else{
                Toast.makeText(
                    applicationContext,
                    getString(R.string.toast_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            email =am.getEmailFromRoom(applicationContext)
            arrayList = getJoinPeoples()!!
            adapterJoinPeople.dataList = arrayList!!
            adapterJoinPeople.notifyDataSetChanged()

            // joinState 셋팅, [버튼]채팅방 입장 VISIBIITY 설정, [버튼]btnJoin 텍스트 셋팅, join_count값 셋팅
            // 참여인원 중 내가 포함되어 있으면 -> state=true, btn_join=나가기
            val isExistingUser = adapterJoinPeople.dataList.any { it.email == email }
            if (isExistingUser) {
                binding.btnJoin.text = "나가기"
                binding.btnEnter.visibility = View.VISIBLE
                joinState = true
            } else {
                binding.btnEnter.visibility = View.GONE
                binding.btnJoin.text = if (total_count == join_count) "대기하기" else "참여하기"
                joinState = false
            }
            // join_count없데이트
            binding.txtCount.setText(adapterJoinPeople.dataList.size.toString())

        }




    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_join -> {
                val map: MutableMap<String, String> = HashMap()
                map["room_idx"] = room_idx.toString() // 방
                map["email"] = email
                map["joinState"] = joinState.toString() // 상태(참여중인지 아닌지)
                lifecycleScope.launch {
                    map["email"] = am.getEmailFromRoom(applicationContext) // 참여자
                    val goServer = fc.goServer(applicationContext, "out_join_room",map as MutableMap<String, String>)
                    if(goServer){
                        Toast.makeText(applicationContext, "리뷰가 변경되었습니다.", Toast.LENGTH_SHORT).show()

                        // Intent
//                        val intent = Intent(applicationContext, ActivityDetailMyBook::class.java)
//                        intent.putExtra("review", binding.editReview.text.toString())
//                        setResult(Activity.RESULT_OK, intent)
//                        finish()
                    }
                }
            }
            R.id.btn_enter -> {
//                val  intenttemp = intent(applicationContext, Activity_Chatting::class.java)
//                intent.putExtra("room_idx", room_idx)
//                intent.putExtra("title", binding.txtTitle.text.toString())
//                startActivity(intent)
            }
        }
    }

    // 채팅방 참여자 불러오기
    suspend fun getJoinPeoples(): ArrayList<Data_Join_People>? = withContext(Dispatchers.IO) {
        val retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.server_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val myApi = retrofit.create(JsonPlaceHolderApi::class.java)


        try {
            val response = myApi.Get_join_chatting_room_people("Get_join_chatting_room_people", room_idx,email).execute()
            if (response.isSuccessful) {
                val result = response.body()
                if (result?.status == "success"){
                    val dataJoinPeopleList =
                        response.body()?.data?.dataJoinPeopleList as ArrayList<Data_Join_People>?
                    Log.i("정보태그","[채팅방 참여 리스트]=>"+dataJoinPeopleList)
                    dataJoinPeopleList


                } else {
                    Log.i("정보태그", "[getChattingRooms]서버에 연결은 되었으나 오류발생")
                    null
                }
            } else {
                Log.i("정보태그", "[getChattingRooms]result.staus isSuccessful X")
                null
            }
        } catch (e: IOException) {
            null
        }
    }

}