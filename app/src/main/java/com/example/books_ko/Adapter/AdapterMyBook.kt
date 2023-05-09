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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.books_ko.ActivityDetailMyBook
import com.example.books_ko.Activity_Book_Add
import com.example.books_ko.Data.DataMyBook
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.R
import com.example.books_ko.databinding.ItemBookListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdapterMyBook (
    var dataMyBooks: ArrayList<DataMyBook> = ArrayList(),
    private val context: Context,
    private val activity : Activity
) : RecyclerView.Adapter<AdapterMyBook.CustomViewHolder>(){

    lateinit var binding : ItemBookListBinding
    val fc = FunctionCollection

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
        val item = dataMyBooks[position]

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

        /*
        데이터 삭제/수정
         */
        binding.txtFunction.setOnClickListener{

            Log.i("정보태그","선택된position->$position")


            // from_ -> search라면 수정은 보이지 않도록 함
            val InputStr: Array<String>
            if(item.from_=="search"){
                InputStr = arrayOf(context.getString(R.string.delete))
            }else{
                InputStr = arrayOf(context.getString(R.string.delete), context.getString(R.string.update))
            }

            val builder = AlertDialog.Builder(activity)
            val str = InputStr
            builder.setTitle("선택하세요")
                .setNegativeButton("취소", null)
                .setItems(
                    str
                )
                { dialog, which ->
                    Log.d("실행", "선택된것=" + str[which])
                    val selected = str[which]
                    when {
                        selected == "삭제" -> {
                            val builder = AlertDialog.Builder(context)
                            builder.setTitle("알림") // AlertDialog 제목
                            builder.setMessage(context.getString(R.string.check_delete)) // 내용
                            builder.setPositiveButton(
                                "예"
                            ) { dialog, which ->
                                Log.d("정보태그", "예 누름")
                                val map: MutableMap<String, String> = HashMap()
                                map.put("book_idx", item.idx.toString())
                                val job = CoroutineScope(Dispatchers.Main).launch {
                                    val isSuccess = fc.goServer(context, "delete_my_book", map)
                                    // 결과 처리
                                    if (isSuccess) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.delete_my_book),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val removeAtBook = dataMyBooks.removeAt(position) // 값을 제거하고 반환
                                        Log.i("정보태그","제거된 정보->{$removeAtBook}")
                                        notifyItemRemoved(position)
                                        // 아이템 삭제 후, 삭제한 위치 이후의 아이템 위치를 재설정
                                        if (position < dataMyBooks.size) {
                                            notifyItemRangeChanged(position, dataMyBooks.size - position)
                                        }
                                    }
                                }
                            }
                            builder.setNegativeButton(
                                "아니오"
                            ) {
                                    dialog, which -> Log.d("정보태그", "아니요 누름")
                            }
                            builder.setNeutralButton("취소", null)
                            builder.create().show() //보이기

                        }
                        selected == "수정" -> {
                            val intent = Intent(context, Activity_Book_Add::class.java).apply {
                                putExtra("idx", item.idx)
                                putExtra("title", item.title)
                                putExtra("authors", item.authors)
                                putExtra("publisher", item.publisher)
                                putExtra("isbn", item.isbn)
                                putExtra("contents", item.contents)
                                putExtra("review", item.review)
                                putExtra("status", item.status)
                                putExtra("rating", item.rating)
                                putExtra("thumbnail", item.thumbnail)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            val dialog = builder.create()
            dialog.show()
        }

        /*
        클릭시 -> ActivityDetailMyBook 이동
         */
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivityDetailMyBook::class.java).apply {
                putExtra("idx", item.idx)
                putExtra("title", item.title)
                putExtra("authors", item.authors)
                putExtra("publisher", item.publisher)
                putExtra("contents", item.contents)
                putExtra("review", item.review)
                putExtra("status", item.status)
                putExtra("rating", item.rating)
                putExtra("thumbnail", item.thumbnail)
            }
            context.startActivity(intent)
        }






    }

    override fun getItemCount(): Int {
        return dataMyBooks.size
    }


}