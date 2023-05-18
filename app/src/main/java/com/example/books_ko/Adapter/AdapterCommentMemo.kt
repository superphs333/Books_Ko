package com.example.books_ko.Adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.Data.Data_Comment_Memo
import com.example.books_ko.Function.AboutMemo.mode
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.R
import com.example.books_ko.databinding.ItemCommentBinding

class AdapterCommentMemo (
    var dataList: ArrayList<Data_Comment_Memo> = ArrayList(),
    private val context: Context,
    private val activity : Activity,
    var email : String, // 사용자의 이메일
) : RecyclerView.Adapter<AdapterCommentMemo.CustomViewHolder>(){

    lateinit var binding : ItemCommentBinding
    val fc = FunctionCollection
    private var mFunctionListener: OnItemClickListener? = null
    private var mReplyListener: OnItemClickListener? = null


    fun setOnFunctionItemClickListener(listener: OnItemClickListener) {
        mFunctionListener = listener
    }

    fun setOnReplyItemClickListener(listener: OnItemClickListener) {
        mReplyListener = listener
    }



    inner class CustomViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root){

        init{
            binding.txtFunction.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    mFunctionListener?.onItemClick(it, pos)
                }
            }

            binding.txtReply.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    mReplyListener?.onItemClick(it, pos)
                }
            }
        }

        fun getAbsoluteAdapterPosition(): Int {
            return adapterPosition
        }


    }


    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
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
        // 프로필 이미지
        val glideImg = if (item.profile_url!!.contains(context.getString(R.string.img_profile))) {
            context.getString(R.string.server_url).removeSuffix("/") + item.profile_url
        } else item.profile_url
        Glide.with(context).load(glideImg).into(binding.imgProfile)
        binding.txtNickname.text = item.nickname // 닉네임
        binding.txtDateTime.text = item.date_time // 날짜,시간
        binding.txtFunction.visibility = if (item.email == email) View.VISIBLE else View.GONE // 기능 (본인인 경우에만 보이도록)
        if (item.visibility == 0) { // 삭제된 글
            // depth=0 and visibility=false인 경우
            binding.txtComment.setText("[삭제된 댓글입니다]")
            binding.txtFunction.visibility = View.GONE
            binding.txtReply.visibility = View.GONE
        } else { // 그 외
            binding.txtComment.text = item.comment
        }
        // depth = 1(대댓글), 0(댓글)
        if (item.depth == 1) { // 대댓글
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(80, 0, 0, 0)
            holder.itemView.layoutParams = params

            binding.txtTargetNickname.visibility = View.VISIBLE
            binding.txtTargetNickname.text = item.target
        } else { // 댓글
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 0)
            holder.itemView.layoutParams = params
            binding.txtTargetNickname.visibility = View.GONE
        }
//        holder.itemView.setOnClickListener {
//            Log.i(
//                "정보태그",
//                "depth=" + item.depth + ", group_idx=" + item.group_idx
//            )
//        }


//        binding.txtFollow.visibility = if (item.follow==1 || item.email == email) View.GONE else View.VISIBLE // 팔로우(자기자신이 아니거나, 팔로우 상태가 아닌 경우에만)
//        binding.txtPage.text = "${item.page}p" // 페이지
//        binding.txtHeartCount.text = item.countHeart.toString() // 하트개수




    }

    override fun getItemCount(): Int {
        return dataList.size
    }


}

