package com.example.books_ko.Adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.Activity_Add_Chatting_Room
import com.example.books_ko.Data.Data_Chatting_Room
import com.example.books_ko.Data.Data_Join_People
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.R
import com.example.books_ko.databinding.ItemChattingRoomBinding
import com.example.books_ko.databinding.ItemJoinPeopleBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.notify

class Adapter_Join_People (
    var dataList: ArrayList<Data_Join_People> = ArrayList(),
    private val context: Context,
    private val activity : Activity,
    private val email : String, // 사용자의 이메일
    private val leader : String,
) : RecyclerView.Adapter<Adapter_Join_People.CustomViewHolder>(){

    lateinit var binding : ItemJoinPeopleBinding
    val fc = FunctionCollection

    inner class CustomViewHolder(val binding: ItemJoinPeopleBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemJoinPeopleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
        val glideImg = if (item.profileUrl?.isNotEmpty() == true) {
            if (item.profileUrl.contains(context.getString(R.string.img_profile))) {
                context.getString(R.string.server_url) + item.profileUrl
            } else {
                item.profileUrl
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
        // 팔로우
        binding.txtFollow.visibility = if (item.follow || item.email == email) View.GONE else View.VISIBLE
        // leader라면 -> txt_role Visibility
        binding.txtRole.visibility = if (item.email == leader) View.VISIBLE else View.GONE






    }

    override fun getItemCount(): Int {
        return dataList.size
    }




}