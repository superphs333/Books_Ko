package com.example.books_ko

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.databinding.ActivityAddChattingRoomBinding
import kotlinx.coroutines.launch

class Activity_Add_Chatting_Room : AppCompatActivity() {

    private lateinit var binding : ActivityAddChattingRoomBinding
    val am = AboutMember
    val fc = FunctionCollection
    var email = ""
    var room_idx = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChattingRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 이메일 값
        lifecycleScope.launch {
            email = AboutMember.getEmailFromRoom(applicationContext)
            Log.i("정보태그","email->$email")
        }

        room_idx = intent.getIntExtra("idx", 0)
        Log.i("정보태그", "room_idx=>$room_idx")

        // 수정인 경우 데이터 셋팅
        if (room_idx != 0) {
            binding.txtTitle.text = "채팅방 수정"
            binding.btnAdd.text = "수정"
            binding.editTitle.setText(intent.getStringExtra("title"))
            binding.editExplain.setText(intent.getStringExtra("room_explain"))
            binding.editCount.setText(intent.getIntExtra("total_count", 0).toString() + "")
        }

        // [개선] 참여 가능 인원이 현재 참여 인원보다 적으면 알림창 or 참여 가능 인원 수정 금지 ?

    }

    override fun onResume() {
        super.onResume()



    }

    fun onClick(view: View) {
        when (view.getId()) {
            // 데이터 저장
            R.id.btn_add -> {
                val map: MutableMap<String, String> = HashMap()
                map["total_count"] = binding.editCount.text.toString() // 총 인원수
                map["room_explain"] = binding.editExplain.text.toString()// 방설명
                map["title"] = binding.editTitle.text.toString() // 제목
                map["email"] = email
                map["idx"] = room_idx.toString()
                val accept_sort = if (room_idx == 0) "save_chatting_room" else "edit_chatting_room"
                lifecycleScope.launch {
                    val goServerForResult = fc.goServerForResult(applicationContext, accept_sort, map)

                    if(goServerForResult["status"]=="success"){
                        if(room_idx==0){ // 방추가
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.save_chatting_room),
                                Toast.LENGTH_SHORT
                            ).show()

                            // 저장한 room_idx가져오기 (해당 룸의 자세히 보기로 이동하기 위해)
                            val data: Map<String, String> = goServerForResult["data"] as Map<String, String>
                            val room_idxString = data["room_idx"] as? String
                            room_idx = room_idxString?.toIntOrNull() ?: 0
                        }else{ // 방수정
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.edit_chatting_room),
                                Toast.LENGTH_SHORT
                            ).show()
                        }


                        /*
                        액티비티 이동
                         */
                        val intent = Intent(applicationContext, Activity_Chatting_Room::class.java)
                        intent.putExtra("room_idx", room_idx)
                        startActivity(intent)
                        finish()

                    }else{
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.toast_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }


}