package com.example.books_ko

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.books_ko.DataBase.UserDatabase
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.FragmentMypageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Fragment_mypage : Fragment() {
    private var binding: FragmentMypageBinding? = null
    private lateinit var context: Context
    private lateinit var activity: Activity
    val am = AboutMember

    private lateinit var database : UserDatabase

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
        내용 셋팅
         */
        // 데이터 불러오기
        database = Room.databaseBuilder(context, UserDatabase::class.java, "app_database").build()
        val userLiveData = database.userDao().getUser()
        CoroutineScope(Dispatchers.IO).launch{
            withContext(Dispatchers.Main){
                userLiveData.observe(activity as FragmentActivity, Observer { userLiveData ->
                    email = userLiveData.email
                    Log.i("정보태그","email->$email")
                    CoroutineScope(Dispatchers.IO).launch{
                        val image_Uri = AboutMember.getMemberInfo(context,email,"profile_url")
                        val nickname = AboutMember.getMemberInfo(context,email,"nickname")
                        if(image_Uri!=null || image_Uri!=""){
                            withContext(Dispatchers.Main){
                                // 프로필 이미지 셋팅
                                val glideImg = if (image_Uri!!.contains(getString(R.string.img_profile))) {
                                    getString(R.string.server_url).removeSuffix("/") + image_Uri
                                } else image_Uri
                                Log.i("정보태그","glideImg->$glideImg")
                                Glide.with(context).load(glideImg).into(binding!!.imgProfile)
                                // 닉네임
                                binding!!.txtNickname.setText(nickname)
                            }
                        }



                    }
                })
            }
        }


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

        /*
        프로필 이미지 변경
         */
        binding!!.imgProfile.setOnClickListener{
            val intent = Intent(context, Activity_Change_Profile::class.java)
            startActivity(intent)
        }

        // Inflate the layout for this fragment
        return binding?.root
    }

    override fun onResume() {
        super.onResume()

        // 닉네임 셋팅

        // 프로필 이미지 셋팅
    }


}