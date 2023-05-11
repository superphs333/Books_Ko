package com.example.books_ko.Adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.Data.Data_Book_Memo
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.databinding.ItemBookMemoBinding

class AdapterBookMemo (
    var dataList: ArrayList<Data_Book_Memo> = ArrayList(),
    private val context: Context,
    private val activity : Activity,
    private val email : String // 사용자의 이메일
) : RecyclerView.Adapter<AdapterBookMemo.CustomViewHolder>(){

    lateinit var binding : ItemBookMemoBinding
    val fc = FunctionCollection

    inner class CustomViewHolder(val binding: ItemBookMemoBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemBookMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        val item = dataList[position]

        /*
        데이터 셋팅
         */
        // 프로필 이미지 셋팅
        Glide.with(holder.itemView.context)
            .load(dataList.get(position).profileUrl)
            .into(binding.imgProfile)
        binding.txtNickname.text = item.nickname // 닉네임
        binding.txtFollow.visibility = if (item.follow || item.email == email) View.GONE else View.VISIBLE // 팔로우(자기자신이 아니거나, 팔로우 상태가 아닌 경우에만)
        binding.txtPage.text = "${item.page}p" // 페이지
        binding.txtHeartCount.text = item.countHeart.toString() // 하트개수
        binding.txtCommentCount.text = item.countComment.toString() // 댓글개수
        binding.txtBook.text = item.title // 책 제목
        binding.txtDateTime.text = item.dateTime // 날짜, 시간
        binding.txtMemo.text = item.memo // 메모
        // open : spinner셋팅 -> 값에 따라 spinner 셋팅
        binding.spinnerSelectOpen.visibility = View.GONE
        when (item.open) {
            "all" -> binding.txtOpen.text = "전체"
            "follow" -> binding.txtOpen.text = "팔로우"
            else -> binding.txtOpen.text = "비공개"
        }
        /*
        이미지 가져오기(슬라이드 셋팅)
         */



    }

    override fun getItemCount(): Int {
        return dataList.size
    }


}