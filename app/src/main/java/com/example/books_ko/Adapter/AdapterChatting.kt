package com.example.books_ko.Adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.books_ko.Data.DataChatting
import com.example.books_ko.databinding.ItemBookMemoBinding
import com.example.books_ko.databinding.ItemChattingMyFileBinding
import com.example.books_ko.databinding.ItemChattingNoticeBinding
import com.example.books_ko.databinding.ItemChattingOthersChatBinding
import com.example.books_ko.databinding.ItemChattingOthersFileBinding
import com.example.books_ko.databinding.ItemChattingRoomBinding
import com.example.books_ko.databinding.ItemChttingMyChatBinding

class AdapterChatting (
    var dataList: ArrayList<DataChatting> = ArrayList(),
    private val context: Context,
    private val activity : Activity,
    private val email : String // 사용자의 이메일
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object {
        const val VIEW_TYPE_NOTICE = 0 // 알림
        const val VIEW_TYPE_OTHERS_CHAT = 1 // 다른 사람 메세지
        const val VIEW_TYPE_MY_FILE = 2 // 내 이미지 메세지
        const val VIEW_TYPE_MY_CHAT = 3 // 내 메세지
        const val VIEW_TYPE_OTHERS_FILE = 4 // 다른 사람 이미지 메세지
    }


    lateinit var bindingNotice : ItemChattingNoticeBinding
    lateinit var bindingMyChat : ItemChttingMyChatBinding
    lateinit var bindingMyFile : ItemChattingMyFileBinding
    lateinit var bindingOthersChat : ItemChattingOthersChatBinding
    lateinit var bindingOthersFile : ItemChattingOthersFileBinding

    // 알림
    inner class CustomViewHolderNotice(val binding: ItemChattingNoticeBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }
    // 내 메세지
    inner class CustomViewHolderMyChat(val binding: ItemChttingMyChatBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }
    // 내 파일
    inner class CustomViewHolderMyFile(val binding: ItemChattingMyFileBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }
    // 다른 사람 메세지
    inner class CustomViewHolderOthersChat(val binding: ItemChattingOthersChatBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }
    // 다른 사람 파일 메세지
    inner class CustomViewHolderOthersFile(val binding: ItemChattingOthersFileBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }

    override fun getItemViewType(position: Int): Int {
        val sort: String = dataList.get(position).sort
        val item = dataList[position]
        return when {
            item.sort == "notice" -> VIEW_TYPE_NOTICE // 알림
            item.sort == "message" -> if (item.email == email) VIEW_TYPE_MY_CHAT else VIEW_TYPE_OTHERS_CHAT // 1 : 다른 사람 메세지, 3 : 내 메세지
            item.sort == "files" -> if (item.email == email) VIEW_TYPE_OTHERS_FILE else VIEW_TYPE_MY_FILE // 2 : 내 이미지 메세지, 4 : 다른 사람 이미지 메세지
            else -> throw IllegalArgumentException("Invalid sort: ${item.sort}")
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_NOTICE -> CustomViewHolderNotice(ItemChattingNoticeBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            VIEW_TYPE_OTHERS_CHAT -> CustomViewHolderOthersChat(ItemChattingOthersChatBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            VIEW_TYPE_MY_FILE -> CustomViewHolderMyFile(ItemChattingMyFileBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            VIEW_TYPE_MY_CHAT -> CustomViewHolderMyChat(ItemChttingMyChatBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            VIEW_TYPE_OTHERS_FILE -> CustomViewHolderOthersFile(ItemChattingOthersFileBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dataList[position]
        when (holder) {
            is CustomViewHolderNotice -> {
                val binding = holder.binding
                binding.txt.setText(item.content)
            }
            is CustomViewHolderOthersChat -> {
                val binding = holder.binding

            }
            is CustomViewHolderMyFile -> {
                val binding = holder.binding

            }
            is CustomViewHolderMyChat -> {
                val binding = holder.binding

            }
            is CustomViewHolderOthersFile -> {
                val binding = holder.binding

            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}




