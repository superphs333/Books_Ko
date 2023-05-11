package com.example.books_ko.Adapter

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.Data.Data_Img_Memo
import com.example.books_ko.Function.ItemTouchHelperListener
import com.example.books_ko.R
import com.example.books_ko.databinding.ItemImgMemoBinding

class Adapter_Img_Memo(
    var dataList: ArrayList<Data_Img_Memo> = ArrayList(),
    private val context: Context,
    private val activity : Activity
) : RecyclerView.Adapter<Adapter_Img_Memo.CustomViewHolder>(), ItemTouchHelperListener {

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

        // 이미지 셋팅
        if (item.img.contains(context.getString(R.string.img_memo))) {
            Glide.with(holder.itemView.context)
                .load(item.img)
                .into(binding.imgMemo)
        } else {
            binding.imgMemo.setImageURI(Uri.parse(item.img))
        }

        // 이미지 삭제
        binding.imgDelete.setOnClickListener {
            Log.i("정보태그","position->$position")
            dataList.remove(item)
            notifyItemRemoved(position)
            Log.i("정보태그", "size=>"+dataList.size);
            // 아이템 삭제 후, 삭제한 위치 이후의 아이템 위치를 재설정
            if (position < dataList.size) {
                notifyItemRangeChanged(position, dataList.size - position)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    // 아이템이 드래그되면 호출되는 메서드
    override fun onItemMove(from_position: Int, to_position: Int): Boolean {
        Log.i("정보태그", "from_position=>$from_position")
        Log.i("정보태그", "to_position=>$to_position")

        // 이동할 객체 저장
        val memoImg = dataList.get(from_position)
        // 이동할 객체 삭제
        dataList.removeAt(from_position)
        // 이동하고 싶은 position에 추가
        dataList.add(to_position,memoImg)
        // Adapter에 데이터 이동 알림
        notifyItemMoved(from_position,to_position)
        notifyDataSetChanged()
//        val memoImg = dataList[from_position]
//        dataList.removeAt(from_position)
//        dataList.add(to_position, memoImg)
//        notifyItemMoved(from_position, to_position)
        return true
    }

    // 화면에서 없어지는 동작
    override fun onItemSwipe(position: Int) {
        dataList.removeAt(position)
        notifyDataSetChanged()
    }
}