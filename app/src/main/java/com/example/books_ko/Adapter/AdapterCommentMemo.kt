package com.example.books_ko.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.Activity_Add_Comment
import com.example.books_ko.Data.Data_Book_Memo
import com.example.books_ko.Data.Data_Comment_Memo
import com.example.books_ko.Data.SliderItem
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.Function.SliderAdapterT1
import com.example.books_ko.R
import com.example.books_ko.databinding.ItemBookMemoBinding
import com.example.books_ko.databinding.ItemCommentBinding
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdapterCommentMemo (
    var dataList: ArrayList<Data_Comment_Memo> = ArrayList(),
    private val context: Context,
    private val activity : Activity,
    var email : String // 사용자의 이메일
) : RecyclerView.Adapter<AdapterCommentMemo.CustomViewHolder>(){

    lateinit var binding : ItemCommentBinding
    val fc = FunctionCollection

    inner class CustomViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        val item = dataList[position]



        /*
        데이터 셋팅
         */
//        Glide.with(holder.itemView.context)
//            .load(dataList.get(position).profileUrl)
//            .into(binding.imgProfile)
//        binding.txtNickname.text = item.nickname // 닉네임
//        binding.txtFollow.visibility = if (item.follow==1 || item.email == email) View.GONE else View.VISIBLE // 팔로우(자기자신이 아니거나, 팔로우 상태가 아닌 경우에만)
//        binding.txtPage.text = "${item.page}p" // 페이지
//        binding.txtHeartCount.text = item.countHeart.toString() // 하트개수




    }

    override fun getItemCount(): Int {
        return dataList.size
    }


}