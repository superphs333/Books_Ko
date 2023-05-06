package com.example.books_ko.Adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.Activity_Book_URL


import com.example.books_ko.Data.Data_Search_Book
import com.example.books_ko.Function.AboutBook
import com.example.books_ko.databinding.ItemSearchBookBinding

class AdapterSearchBook(
    var dataSearchBooks: ArrayList<Data_Search_Book> = ArrayList(),
    private val context: Context,
    private val activity : Activity
) : RecyclerView.Adapter<AdapterSearchBook.CustomViewHolder>() {

    val ab = AboutBook



    class CustomViewHolder(private val binding: ItemSearchBookBinding) : RecyclerView.ViewHolder(binding.root) {
        val img_thumbnail: ImageView = binding.imgThumbnail
        val txt_title: TextView = binding.txtTitle
        val txt_authors: TextView = binding.txtAuthors
        val txt_contents: TextView = binding.txtContents
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
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

        /*
        동작
         */
        holder.itemView.setOnClickListener{
            val builder = AlertDialog.Builder(activity)
            val str = arrayOf("자세히 보기", "책 저장하기")
            builder.setTitle("선택하세요")
                .setNegativeButton("취소", null)
                .setItems(
                    str
                )
                { dialog, which ->
                    Log.d("실행", "선택된것=" + str[which])
                    if (str[which] == "자세히 보기") {
                        // 해당 url페이지를 웹뷰로 보여주는 액티비티로 이동
                        val intent = Intent(activity, Activity_Book_URL::class.java)
                        intent.putExtra(
                            "url",
                            dataSearchBooks.get(holder.getAbsoluteAdapterPosition()).url
                        )
                        activity.startActivity(intent)
                    } else {
                        ab.Check_in_mybook(
                            context,
                            activity as LifecycleOwner,
                            dataSearchBooks.get(holder.getAbsoluteAdapterPosition()).url,
                            dataSearchBooks.get(holder.getAbsoluteAdapterPosition())
                        )
                    }
                }
            val dialog = builder.create()
            dialog.show()
        }
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
