package com.example.books_ko.Adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.Data.Data_Search_Book
import com.example.books_ko.databinding.ItemSearchBookBinding

class AdapterSearchBook(
    var dataSearchBooks: ArrayList<Data_Search_Book> = ArrayList(),
    context: Context,
    activity: Activity
) : RecyclerView.Adapter<AdapterSearchBook.CustomViewHolder>() {



    class CustomViewHolder(private val binding: ItemSearchBookBinding) : RecyclerView.ViewHolder(binding.root) {
        val img_thumbnail: ImageView = binding.imgThumbnail
        val txt_title: TextView = binding.txtTitle
        val txt_authors: TextView = binding.txtAuthors
        val txt_contents: TextView = binding.txtContents
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemSearchBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        /*
        내용셋팅
         */
        Glide.with(holder.itemView.context)
            .load(dataSearchBooks[position].thumbnail)
            .into(holder.img_thumbnail) // 썸네일 이미지
        holder.txt_title.text = dataSearchBooks[position].title   // 타이틀
        holder.txt_authors.text = dataSearchBooks[position].authors   // 작가
        holder.txt_contents.text = dataSearchBooks[position].contents   // 내용

    }

    override fun getItemCount(): Int {
        return dataSearchBooks.size
    }

    fun updateList(newList: ArrayList<Data_Search_Book>) {
        Log.i("정보태그","newList->${newList}")
        dataSearchBooks.clear()
        dataSearchBooks.addAll(newList)
        Log.i("정보태그","dataSearchBooks->${dataSearchBooks}")
        notifyDataSetChanged()
    }
}
