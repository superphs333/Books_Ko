package com.example.books_ko.Adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.books_ko.Activity_Add_Chatting_Room
import com.example.books_ko.Activity_Chatting_Room
import com.example.books_ko.Data.Data_Chatting_Room
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.databinding.ItemChattingRoomBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.notify

class Adapter_Chatting_Room (
    var dataList: ArrayList<Data_Chatting_Room> = ArrayList(),
    private val context: Context,
    private val activity : Activity,
    private val email : String // 사용자의 이메일
) : RecyclerView.Adapter<Adapter_Chatting_Room.CustomViewHolder>(){

    lateinit var binding : ItemChattingRoomBinding
    val fc = FunctionCollection

    inner class CustomViewHolder(val binding: ItemChattingRoomBinding) : RecyclerView.ViewHolder(binding.root){
        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemChattingRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        val item = dataList[position]

        /*
        데이터 셋팅
         */
        binding.txtTitle.text = item.title // 방제
        binding.txtExplain.text = item.room_explain // 방설명
        binding.txtCount.text = item.join_count.toString() // 참여인원
        binding.txtTotal.text = item.total_count.toString() // 참여 가능 인원
        binding.txtFunction.visibility = if (email == item.leader) View.VISIBLE else View.GONE // txt_function -> leader의 경우에만 view

        // 클릭 -> 해당 채팅방 룸으로 들어가기
        binding.root.setOnClickListener {
            val intent = Intent(binding.root.context, Activity_Chatting_Room::class.java)
            intent.putExtra("room_idx", item.idx)
            intent.putExtra("leader", item.leader)
            binding.root.context.startActivity(intent)
        }


        // txt_function => 수정, 삭제
        binding.txtFunction.setOnClickListener {
            val str = arrayOf("수정", "삭제")
            AlertDialog.Builder(activity)
                .setTitle("선택하세요")
                .setNegativeButton("취소", null)
                .setItems(str) { dialog, which ->
                    Log.d("실행", "선택된것=" + str[which])
                    when (str[which]) {
                        "수정" -> {
                            val intent = Intent(context, Activity_Add_Chatting_Room::class.java)
                            intent.putExtra("idx", item.idx)
                            intent.putExtra("title", item.title)
                            intent.putExtra("room_explain", item.room_explain)
                            intent.putExtra("total_count", item.total_count)
                            activity.startActivity(intent)
                        }
                        "삭제" -> {
                            val map = mutableMapOf<String, String>()
                            map["idx"] = item.idx.toString()
                            CoroutineScope(Dispatchers.Main).launch  {
                                val goServer = fc.goServer(context,"delete_rom",map)
                                if(goServer){
                                    Toast.makeText(context, "삭제 되었습니다.", Toast.LENGTH_SHORT).show()
                                    dataList.removeAt(position)
                                    notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
                .create()
                .show()
        }





    }

    override fun getItemCount(): Int {
        return dataList.size
    }


}