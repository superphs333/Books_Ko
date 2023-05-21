package com.example.books_ko.Adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.Data.DataFollowPeople
import com.example.books_ko.Data.Data_Join_People
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.R
import com.example.books_ko.databinding.ItemFollowPeopleBinding
import com.example.books_ko.databinding.ItemJoinPeopleBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdapterFollowPeople (
    var dataList: ArrayList<DataFollowPeople> = ArrayList(),
    private val context: Context,
    private val activity : Activity,
    private val email : String, // 사용자의 이메일
) : RecyclerView.Adapter<AdapterFollowPeople.CustomViewHolder>(){

    lateinit var binding : ItemFollowPeopleBinding
    var mode_follow = "follower"


    inner class CustomViewHolder(val binding: ItemFollowPeopleBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemFollowPeopleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        val item = dataList[position]

        /*
        데이터 셋팅
         */
        // 프로필 이미지
        val defaultImageResId = R.drawable.basic_profile_img
        val glideImg = if (item.profile_url?.isNotEmpty() == true) {
            if (item.profile_url.contains(context.getString(R.string.img_profile))) {
                context.getString(R.string.server_url) + item.profile_url
            } else {
                item.profile_url
            }
        } else {
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + defaultImageResId)
        }
        Glide.with(holder.itemView.context)
            .load(glideImg)
            .placeholder(defaultImageResId)
            .error(defaultImageResId)
            .into(binding.imgProfile)

        // 닉네임
        binding.txtNickname.text = item.nickname



        /*
        팔로우 기능
         */
        binding.txtFunction.setOnClickListener { view ->
            val builder = AlertDialog.Builder(activity)
            var str = arrayOf("A", "B")

            when (mode_follow) {
                "follower" -> str = arrayOf("삭제", "팔로잉")
                "following" -> str = arrayOf("팔로잉 취소")
            }

            builder.setTitle("선택하세요")
                .setNegativeButton("취소", null)
                .setItems(str) { dialog, which ->
                    Log.d("정보태그", "선택된것=${str[which]}")

                    var From_login_value = ""
                    var To_login_value = ""
                    var management = ""

                    when (str[which]) {
                        "삭제" -> {
                            management = "invisible"
                            From_login_value = item.email
                            To_login_value = email
                        }
                        "팔로잉" -> {
                            management = "following"
                            From_login_value = email
                            To_login_value = item.email
                        }
                        "팔로잉 취소" -> {
                            management = "delete_following"
                            From_login_value = email
                            To_login_value = item.email
                        }
                    }

                    Log.d("정보태그", "From_login_value=$From_login_value")
                    Log.d("정보태그", "To_login_value=$To_login_value")
                    Log.d("정보태그", "management=$management")

//                    fs.Management_Follow(From_login_value, To_login_value, management) { result ->
//                        Log.d("실행", "(in Adapter)result=${result.trim()}")
//                        val string_array = result.trim().split("§")
//
//                        if (string_array[0] == "success") {
//                            if (management != "following") {
//                                arrayList.removeAt(holder.adapterPosition)
//                                notifyItemRemoved(holder.adapterPosition)
//                            }
//                        }
//                    }
                }

            val dialog = builder.create()
            dialog.show()
        }





    }

    override fun getItemCount(): Int {
        return dataList.size
    }




}