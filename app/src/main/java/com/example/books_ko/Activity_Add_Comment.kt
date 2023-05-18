package com.example.books_ko

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.books_ko.Data.Data_Comment_Memo
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.databinding.ActivityAddCommentBinding
import kotlinx.coroutines.launch

class Activity_Add_Comment : AppCompatActivity() {

    private lateinit var binding: ActivityAddCommentBinding

    var idx_memo = 0
    var writer_email = ""

    // mode -> add, edit, add_comment(대댓글추가), edit2(대댓글 수정)
    var mode = "add"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 값 전달받기
        idx_memo = intent.getIntExtra("idx_memo", 0)
        Log.d("정보태그", "(in Activity_Add_Comment)idx_memo=$idx_memo")
        writer_email = intent.getStringExtra("idx_memo").toString()
        Log.d("정보태그", "(in Activity_Add_Comment)writer_email=$writer_email")

        // 대댓글을 달지 않은 상태에서는 linear_to가 보이지 않아야 함
        binding.linearTo.visibility=View.GONE
    }

    fun onClick(view: View) {
        val id: Int = view.getId()
        when (id) {
            R.id.img_back -> finish()
            R.id.btn_comment -> when (mode) {
                "add" -> Management_Comment("add", 0, 0)
//                "edit" -> Management_Comment("edit", arrayList[temp_position].idx, temp_position)
//                "add_comment" -> Management_Comment("add_comment", arrayList[temp_position].group_idx, temp_position)
            }
            R.id.txt_cancel -> {
                mode = "add"
                binding.linearTo.visibility = View.GONE
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    /*
    댓글을 추가, 수정, 삭제하는 함수
     */
    private fun Management_Comment(sort:String, idx:Int, position:Int){
        // sort = add, add_comment, edit, delete

        val map: MutableMap<String, String> = HashMap()
        map["sort"] = sort
        map["idx_memo"] = idx_memo.toString()
        when(sort){
            "add" -> {
                map["email"] = email
                map["comment"] =  binding.editComment.text.toString()
            }


        }


        lifecycleScope.launch {
            val goServerForResult = FunctionCollection.goServerForResult(applicationContext,"Management_Comment",map)
            if(goServerForResult["status"]=="success"){
                val data: Map<String, String> = goServerForResult["data"] as Map<String, String>
                val idx = data["idx"] as? String
                when(sort){
                    "add" -> {
                        //val cm = Data_Comment_Memo()
                    }
                }
            }else{
                Toast.makeText(
                    applicationContext,
                    getString(R.string.toast_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}