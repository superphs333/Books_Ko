package com.example.books_ko.Adapter

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.Data.DataMyBook
import com.example.books_ko.Data.Data_Img_Memo
import com.example.books_ko.R
import com.example.books_ko.databinding.ItemBookListBinding
import com.example.books_ko.databinding.ItemImgMemoBinding

class Adapter_Img_Memo(
    var dataList: ArrayList<Data_Img_Memo> = ArrayList(),
    private val context: Context,
    private val activity : Activity
) : RecyclerView.Adapter<Adapter_Img_Memo.CustomViewHolder>() {

    lateinit var binding : ItemImgMemoBinding

    inner class CustomViewHolder(val binding: ItemImgMemoBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemImgMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        val item = dataList[position]

        // 데이터 셋팅 :  binding.txtTitle.text=item.title
        /*
        삭제시
        notifyItemRemoved(position)
        // 아이템 삭제 후, 삭제한 위치 이후의 아이템 위치를 재설정
        if (position < dataMyBooks.size) {
            notifyItemRangeChanged(position, dataMyBooks.size - position)
        }
         */
        Log.i("정보태그","(onBindViewHolder)img->"+item.img)

        if (item.img.contains(context.getString(R.string.img_memo))) {
            Glide.with(holder.itemView.context)
                .load(item.img)
                .into(binding.imgMemo)
        } else {
            binding.imgMemo.setImageURI(Uri.parse(item.img))
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}