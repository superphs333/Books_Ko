package com.example.books_ko

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.books_ko.Adapter.AdapterCommentMemo

import com.example.books_ko.Data.Data_Comment_Memo
import com.example.books_ko.DataBase.UserDatabase
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Function.AboutMemo
import com.example.books_ko.Function.FunctionCollection
import com.example.books_ko.databinding.ActivityAddCommentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class Activity_Add_Comment : AppCompatActivity() {

    private lateinit var binding: ActivityAddCommentBinding

    var idx_memo = 0
    var memo_writer_email = "" // 메모 작성자
    var sender_nickname = "" // 댓글 작성자 닉네임
    var login_email = "" // 댓글 작성자 이메일
    var login_profileUrl = "" // 댓글 작성자 프로필 이미지


    // mode -> add, edit, add_comment(대댓글추가), edit2(대댓글 수정)
    var mode = "add"
    var temp_position = 0

    /*
    리사이클러뷰
     */
    var arrayList: ArrayList<Data_Comment_Memo> = ArrayList()
    private lateinit var mainAdapter : AdapterCommentMemo
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var database : UserDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Room.databaseBuilder(applicationContext, UserDatabase::class.java, "app_database").build()
        val userLiveData = database.userDao().getUser()

        // 값 전달받기
        idx_memo = intent.getIntExtra("idx_memo", 0) // memoidx
        Log.d("정보태그", "(in Activity_Add_Comment)idx_memo=$idx_memo")
        memo_writer_email = intent.getStringExtra("memo_writer_email").toString() // 메모작성자
        Log.d("정보태그", "(in Activity_Add_Comment)writer_email=$memo_writer_email")

        // 댓글 작성자 닉네임 (알림 보내기 용), 리사이클러뷰 셋팅
        userLiveData.observe(this@Activity_Add_Comment, Observer { userData ->
            login_email = userData.email
            Log.i("정보태그", "login_email->${login_email}")
            CoroutineScope(Dispatchers.IO).launch {
                sender_nickname = AboutMember.getMemberInfo(applicationContext, login_email, "nickname")
                Log.i("정보태그", "sender_nickname->$sender_nickname")
                login_profileUrl = AboutMember.getMemberInfo(applicationContext, login_email, "profile_url")
                Log.i("정보태그", "profile_url->$login_profileUrl")

                /*
                리사이클러뷰 변수
                 */
                linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL,false)
                mainAdapter = AdapterCommentMemo(arrayList!!, applicationContext, this@Activity_Add_Comment,login_email)
                // 대댓글 닫기 버튼
                mainAdapter.setOnReplyItemClickListener(object : AdapterCommentMemo.OnItemClickListener {
                    override fun onItemClick(v: View, position: Int) {
                        Log.d("정보태그", "[setOnReplyItemClickListener](txt_reply)position=$position")

                        // edit_comment에 포커스, mode= add2
                        mode = "add_comment"
                        binding.editComment.requestFocus()
                        // 키보드 올리기
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(binding.editComment, InputMethodManager.SHOW_IMPLICIT)
                        temp_position = position
                        binding.linearTo.visibility = View.VISIBLE // ~님께 댓글 다는중 부분
                        // txt_target_nickname 셋팅
                        Log.i("정보태그","arrayList->${mainAdapter.dataList.toString()}")
                        binding.txtTargetNickname.text = mainAdapter.dataList[position].nickname
                    }
                })
                // 기능 버튼 -> 수정, 삭제
                mainAdapter.setOnFunctionItemClickListener(object : AdapterCommentMemo.OnItemClickListener{
                    override fun onItemClick(v: View, position: Int) {
                        Log.d("정보태그", "[setOnFunctionItemClickListener](txt_reply)position=$position")
                        val options = arrayOf("수정", "삭제")
                        AlertDialog.Builder(this@Activity_Add_Comment)
                            .setTitle("선택하세요")
                            .setNegativeButton("취소", null)
                            .setItems(options) { dialog, which ->
                                when (options[which]) {
                                    "수정" -> {
                                        binding.editComment.setText(mainAdapter.dataList[position].comment)
                                        binding.editComment.requestFocus()
                                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.showSoftInput(binding.editComment, InputMethodManager.SHOW_IMPLICIT)
                                        mode = "edit"
                                        binding.btnComment.text = "수정"
                                        temp_position = position
                                    }
                                    "삭제" -> Management_Comment("delete", mainAdapter.dataList[position].idx, position)
                                }
                            }
                            .show()


                    }
                })



                withContext(Dispatchers.Main) {
                    binding!!.rvComments.apply {
                        setHasFixedSize(true)
                        layoutManager = linearLayoutManager
                        adapter = mainAdapter
                    }
                    mainAdapter.dataList =
                        AboutMemo.getMemoComments(applicationContext,login_email,idx_memo,0)!!
                    mainAdapter.notifyDataSetChanged()
                }

            }
        })

        // 대댓글을 달지 않은 상태에서는 linear_to가 보이지 않아야 함
        binding.linearTo.visibility=View.GONE


    }



    fun onClick(view: View) {
        val id: Int = view.getId()
        when (id) {
            R.id.img_back -> finish()
            R.id.btn_comment -> when (mode) {
                "add" -> Management_Comment("add", 0, 0)
                "edit" -> Management_Comment("edit", mainAdapter.dataList[temp_position].idx, temp_position)
                "add_comment" -> Management_Comment("add_comment", mainAdapter.dataList[temp_position].group_idx, temp_position)
            }
            R.id.txt_cancel -> {
                mode = "add"
                binding.linearTo.visibility = View.GONE
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
            R.id.txt_cancel -> { // 대댓글 취소
                mode = "add"
                binding.linearTo.visibility = View.GONE // 대댓글을 달지 않는 상태에서는 linear_to가 보이지 않아야 한다
                // 키보드 내리기
                val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            }
        }
    }

    /*
    댓글을 추가, 수정, 삭제하는 함수
     */
    private fun Management_Comment(sort:String, group_idx:Int, position:Int){
        // sort = add, add_comment, edit, delete
        // idx = group_idx

        // 날짜, 시간
        val now = System.currentTimeMillis()
        val mDate = Date(now)
        val simpleDate = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val date_time = simpleDate.format(mDate)
        Log.d("실행", "date_time=$date_time")

        val map: MutableMap<String, String> = HashMap()
        map["sort"] = sort
        map["idx_memo"] = idx_memo.toString()
        when(sort){
            "add" -> {
                map["email"] = email
                map["comment"] =  binding.editComment.text.toString()
                map["sender_nickname"] =  sender_nickname
                map["memo_writer_email"] =  memo_writer_email
                map["date_time"] =  date_time
            }
            "add_comment" -> { // 대댓글
                map["email"] = email
                map["group_idx"] = group_idx.toString()
                map["comment"] =  binding.editComment.text.toString()
                map["date_time"] =  date_time
                map["target"] =  mainAdapter.dataList[position].email
            }
            "edit" ->{ // 수정
                map["idx"] = group_idx.toString()
                map["comment"] =  binding.editComment.text.toString()
            }
            "delete" -> { // 삭제
                map["idx"] = group_idx.toString()
            }


        }

        Log.i("정보태그","map->$map")
        lifecycleScope.launch {
            val goServerForResult = FunctionCollection.goServerForResult(applicationContext,"Management_Comment",map)
            if(goServerForResult["status"]=="success"){
                val data: Map<String, String> = goServerForResult["data"] as Map<String, String>
                var idx = 0
                if(sort=="add" || sort=="add_comment"){
                    idx = data["idx"].toString().toInt()
                }
                when(sort){
                    "add" -> {
                        val cm = Data_Comment_Memo(
                            idx_memo,
                            idx,
                            login_email,
                            sender_nickname,
                            login_profileUrl,
                            binding.editComment.text.toString(),
                            date_time,
                            idx,
                            0,
                            1
                        )
                        mainAdapter.dataList.add(cm)
                        mainAdapter.notifyDataSetChanged()
                    }
                    "add_comment" -> {
                        // 값추가
                        // [개선] 추가된 값만 부모댓글에서 추가하기
                        CoroutineScope(Dispatchers.IO).launch {
                            mainAdapter.dataList =
                                AboutMemo.getMemoComments(applicationContext,login_email,idx_memo,0)!!
                            withContext(Dispatchers.Main){
                                mainAdapter.notifyDataSetChanged()
                            }
                        }

                        mode = "add"
                        binding.linearTo.visibility = View.GONE
                    }

                    "edit" -> {
                        mainAdapter.dataList.get(position).comment = binding.editComment.text.toString()
                        mainAdapter.notifyDataSetChanged()
                        binding.btnComment.text = "전송"
                        mode = "add"
                    }
                    "delete" -> {
                        mainAdapter.dataList.get(position).visibility=0
                        mainAdapter.notifyDataSetChanged()
                    }
                }

                // 댓글 입력부 빈값
                binding.editComment.setText("")
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