package com.example.books_ko.Adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.Data.DataMyBook
import com.example.books_ko.R
import com.example.books_ko.databinding.ItemBookListBinding

class AdapterMyBook (
    var dataMyBooks: ArrayList<DataMyBook> = ArrayList(),
    private val context: Context,
    private val activity : Activity
) : RecyclerView.Adapter<AdapterMyBook.CustomViewHolder>(){

    lateinit var binding : ItemBookListBinding

    inner class CustomViewHolder(val binding: ItemBookListBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemBookListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return CustomViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        val item = dataMyBooks[holder.getAbsoluteAdapterPosition()]

        /*
        데이터 셋팅
         */
        binding.txtTitle.text=item.title // 타이틀
        // 이미지 -> 이미지 없는 경우 기본 이미지
        if (item.thumbnail == "") {
            binding.imgThumbnail.setImageResource(R.drawable.basic_book_cover)
        } else {
            // [개선] book from값에 따라 가져오는 게 나을 듯
            val glideImg = if (item.thumbnail.contains(context.getString(R.string.img_book))) { // 서버이미지
                context.getString(R.string.server_url) + item.thumbnail
            } else { // 검색에서 추가한 경우
                item.thumbnail
            }
            Glide.with(holder.itemView.context).load(glideImg).into(binding.imgThumbnail)
        }
        binding.txtAuthors.setText(item.authors) // 작가
        binding.txtContents.apply {
            text = item?.contents ?: ""
            visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
        } // 내용
        binding.ratingBar.rating = item.rating // 별점
        when (item.status) { // 읽음상태
            3 -> binding.txtStatus.text = context.getString(R.string.read_bucket)
            1 -> binding.txtStatus.text = context.getString(R.string.read_reading)
            2 -> binding.txtStatus.text = context.getString(R.string.read_end)
        }





    }

    override fun getItemCount(): Int {
        return dataMyBooks.size
    }


}