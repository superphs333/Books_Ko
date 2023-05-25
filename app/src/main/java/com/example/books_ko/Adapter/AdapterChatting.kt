package com.example.books_ko.Adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.Data.DataChatting
import com.example.books_ko.R
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
            sort == "notice" -> VIEW_TYPE_NOTICE // 알림
            sort == "message" -> if (item.email == email) VIEW_TYPE_MY_CHAT else VIEW_TYPE_OTHERS_CHAT // 1 : 다른 사람 메세지, 3 : 내 메세지
            sort == "files" -> if (item.email == email) VIEW_TYPE_MY_FILE else VIEW_TYPE_OTHERS_FILE // 2 : 내 이미지 메세지, 4 : 다른 사람 이미지 메세지
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
                binding.txtChat.setText(item.content)
                binding.txtTime.setText(item.date)
            }
            is CustomViewHolderMyFile -> {
                val binding = holder.binding
                // 이미지
                val chatImg = context.getString(R.string.server_url_rearNotSlash)+item.content
                Glide.with(context).load(chatImg).into(binding.imgChat)
                // 시간
                binding.txtTime.setText(item.date)
                /*
                분류 -> 시간 visible/invisible
                0,3 -> 시간보임
                1,2 -> 시간 안보임
                 */
                val orderTag = item.orderTag
                if (orderTag == "0" || orderTag == "3") {
                    binding.txtTime.visibility = View.VISIBLE
                } else if (orderTag == "1" || orderTag == "2") {
                    binding.txtTime.visibility = View.INVISIBLE
                }

            }
            is CustomViewHolderMyChat -> {
                val binding = holder.binding
                binding.txtChat.setText(item.content)
                binding.txtTime.setText(item.date)
            }
            is CustomViewHolderOthersFile -> {
                val binding = holder.binding
                // 이미지
                val chatImg = context.getString(R.string.server_url_rearNotSlash)+item.content
                Glide.with(context).load(chatImg).into(binding.imgChat)
                // 시간
                binding.txtTime.setText(item.date)
                // 썸네일
                val glideProfileImg = if (item.profileUrl!!.contains(context.getString(R.string.img_profile))) {
                    context.getString(R.string.server_url_rearNotSlash)+ item.profileUrl
                } else item.profileUrl
                Glide.with(context).load(glideProfileImg).into(binding!!.chatThumbnail)
                // 닉네임
                binding.txtNickname.setText(item.nickname)

                /*
                ordertag => 이거에 따라 날짜, 프사, 닉네임이 보이거나/안보이거나
                0 = file 한개만 있는 경우 -> 날짜 o, 프사 o, 닉네임 o
                1 = file 여러개, 첫번재 -> 날짜 x, 프사 o, 닉네임 o
                2 = file 여러개, 중간 -> 날짜 x, 프사 x, 닉네임 x
                3 = file 여러개, 마지막 -> 날짜 o, 프사 x, 닉네임 x
                 */
                val orderTag = item.orderTag
                binding.txtTime.visibility = if (orderTag == "0" || orderTag == "3") View.VISIBLE else View.INVISIBLE
                binding.chatThumbnail.visibility = if (orderTag == "0" || orderTag == "1") View.VISIBLE else View.INVISIBLE
                binding.txtNickname.visibility = if (orderTag == "0") View.VISIBLE else View.INVISIBLE

            }
        }
    }

    override fun getItemCount(): Int {
        //Log.i("정보태그","[AdapterChatting]count->${dataList.size}")
        return dataList.size
    }
}




