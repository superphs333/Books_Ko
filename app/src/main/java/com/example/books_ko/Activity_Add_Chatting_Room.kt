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
                lifecycleScope.launch {
                    val goServerForResult = fc.goServerForResult(applicationContext, "save_chatting_room", map)

                    if(goServerForResult["status"]=="success"){
                        Toast.makeText(applicationContext, R.string.save_chatting_room, Toast.LENGTH_SHORT).show()
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