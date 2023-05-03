package com.example.books_ko

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.ActivityMain2Binding

class Activity_Main2 : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    val am = AboutMember

    var fragmentManager: FragmentManager? = null
    var fragmentTransaction: FragmentTransaction? = null

    // 프래그먼트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}