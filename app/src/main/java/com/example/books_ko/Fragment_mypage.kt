package com.example.books_ko

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.FragmentMypageBinding


class Fragment_mypage : Fragment() {
    private var binding: FragmentMypageBinding? = null
    private lateinit var context: Context
    private lateinit var activity: Activity
    val am = AboutMember


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMypageBinding.inflate(layoutInflater, container, false)
        val view: View = binding!!.getRoot()


        activity = requireActivity()
        context = requireContext()

        /*
        세팅 액티비티로 이동
         */
        binding!!.imgSetting.setOnClickListener {
            val intent = Intent(context, Activity_Setting::class.java)
            startActivity(intent)
        }

        /*
        닉네임 변경 액티비티로 이동
         */
        binding!!.txtNickname.setOnClickListener{
            val intent = Intent(context, Activity_Set_nickname::class.java)
            intent.putExtra("why_change","only_change")
            startActivity(intent)
        }

        // Inflate the layout for this fragment
        return binding?.root
    }


}