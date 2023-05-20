package com.example.books_ko

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.books_ko.databinding.ActivityUnderlinePictureBinding

class Activity_Underline_Picture : AppCompatActivity() {

    private lateinit var binding: ActivityUnderlinePictureBinding


    var img_url = "" // 이미지 주소(Intent에서 받아옴)
    var position: Int = 0 // 이전 액티비티에서 받아온 이미지 위치


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnderlinePictureBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())


        // 값받아오기 : img_url, position
        img_url = intent.getStringExtra("img_url")!!
        position = intent.getIntExtra("position", 0)
        Log.i("정보태그", "[Activity_Underline_Picture-onCreate]position=>$position, img_url=>$img_url")

        // 뷰에 이미지 셋팅
        binding.myCanvas.m_filename = img_url
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.image_back -> {
                finish()
            }
            R.id.btn_ok -> {
                val intent = Intent()
                intent.putExtra("result", binding.myCanvas.Save_Send())
                intent.putExtra("position", position)
                setResult(RESULT_OK, intent)
                finish()
            }
            R.id.btn_reset -> {
                binding.myCanvas.eraser()
            }
        }

    }
}