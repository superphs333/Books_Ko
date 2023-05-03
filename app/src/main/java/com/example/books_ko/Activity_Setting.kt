package com.example.books_ko

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.books_ko.databinding.ActivitySettingBinding

private lateinit var binding: ActivitySettingBinding

class Activity_Setting : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}