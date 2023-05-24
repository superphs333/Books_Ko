package com.example.books_ko

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.books_ko.Adapter.AdapterChatting
import com.example.books_ko.Data.DataChatting
import com.example.books_ko.Function.AboutChatting
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.ActivityChattingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class Activity_Chatting : AppCompatActivity() {

    private lateinit var binding : ActivityChattingBinding

    var room_idx = 0
    var email = ""
    var nickname = ""
    var profile_url = ""

    /*
    리사이클러뷰 관련
     */
    var arrayList: ArrayList<DataChatting> = ArrayList()
    private lateinit var mainAdapter : AdapterChatting
    private lateinit var linearLayoutManager: LinearLayoutManager


    /*
    소켓관련
     */
    private lateinit var memberSocket: Socket // 서버와 연결되어 있는 소켓 객체
    private var isRunning = false // 어플 종료시 스레드 중지 위해

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        room_idx = intent.getIntExtra("room_idx", 0)
        Log.d("정보태그", "[Activity_Chatting]room_idx=$room_idx")
        binding.txtTitle.setText(getIntent().getStringExtra("title")); // 제목셋팅
        // 소프트키보드가 뷰를 밀러 올리는 것을 방지(화면의 뷰들이 키보드에 의해 가려지지 않도록 화면 자체를 위로 밀어내거나 이동)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)


        GlobalScope.launch {
            email = AboutMember.getEmailFromRoom(applicationContext)
            Log.d("정보태그", "[Activity_Chatting]email=$email")

            // 리사이클러뷰
            linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL,false)
                // LinearLayoutManger 방향 중요! (안 나올 수가 있다)
            arrayList = AboutChatting.getChattingDatas(applicationContext,email,room_idx,0)!!
            mainAdapter = AdapterChatting(arrayList!!, applicationContext, this@Activity_Chatting, email)
            runOnUiThread {
                binding!!.rvChatting.apply {
                    setHasFixedSize(true)
                    layoutManager = linearLayoutManager
                    adapter = mainAdapter
                }
                mainAdapter.dataList = arrayList
                mainAdapter.notifyDataSetChanged()
            }


            profile_url = AboutMember.getMemberInfo(applicationContext, email,"profile_url")
            nickname = AboutMember.getMemberInfo(applicationContext, email,"nickname")
            Log.d("정보태그", "[Activity_Chatting]profile_url=$profile_url")
            Log.d("정보태그", "[Activity_Chatting]nickname=$nickname")
            //connectToServer() // 서버와 연결(접속 스레드 가동)

            // 서버와 연결(접속 스레드 가동)
            val connectionThread = ConnectionThread()
            connectionThread.start()
            //connectionThread.join()

            // 메세지를 받아들이는 스레드 시작
//                isRunning = true
//                val thread = MessageThread(socket)
//                thread.start()

        }

    }

    // 화면이 꺼지면 -> 채팅방 나가기(socket.close)
    override fun onDestroy() {
        super.onDestroy()

        try {
            memberSocket.close()
            isRunning = false
        } catch (e: java.lang.Exception) {
        }
    }

    /*
    서버에 데이터를 전송 작업
     */
    inner class sendToServerThread() : Thread() {
        val txtMsg = binding.editChat.text.toString()
        private lateinit var dos: DataOutputStream // 데이터를 보낼 stream

        init {
            try {
                val os: OutputStream = memberSocket.getOutputStream()
                dos = DataOutputStream(os)
            } catch (e: IOException) {
                Log.d("정보태그", "OutputStream 오류-${e.message}")
                e.printStackTrace()
            }
        }

        override fun run() {
            try {
                /*
                서버에 데이터를 보낸다.
                전달받은 메세지를 dos.writeUTF(msg)를 통해 서버로 데이터를 보내주기만 하면 됩니다.
                */
                dos.writeUTF("message")
                dos.flush()
                dos.writeUTF(txtMsg)
                dos.flush()

                // edit 부분을 비워준다
                runOnUiThread {
                    binding.editChat.setText("")
                }

            } catch (e: Exception) {
                Log.d("정보태그", "OutputStream 오류-${e.message}")
                e.printStackTrace()
            }
        }
    }


    inner class ConnectionThread : Thread() {
        override fun run() {
            try {
                val socket = Socket("13.124.220.90", 1234)
                memberSocket = socket // 클라이언트 소켓 셋팅
                Log.i("정보태그", "Socket 성공")

                val outputStream: OutputStream = socket.getOutputStream()
                val dataOutputStream = DataOutputStream(outputStream)

                // 방 idx 송신하기
                dataOutputStream.writeUTF(room_idx.toString())
                dataOutputStream.flush()

                // 전송할 데이터 json
                val toGoServerJson = JSONObject()
                toGoServerJson.put("email", email)
                toGoServerJson.put("nickname", nickname)
                toGoServerJson.put("profile_url", profile_url)
                val toGoServerJsonString = toGoServerJson.toString()
                Log.i("정보태그", "전송할 회원 정보->${toGoServerJsonString}")
                dataOutputStream.writeUTF(toGoServerJsonString)
                dataOutputStream.flush()

                // 메세지를 받아들이는 스레드 시작
                isRunning = true
                val thread = MessageThread(socket)
                thread.start()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("정보태그", "Socket 에러 - ${e.message}")
            }
        }
    }


    /*
    메세지를 받아들이는 작업을 처리
    :전달받은 메세지를 계속(while) 불러들인다
     */
    inner class MessageThread(private val socket: Socket) : Thread() {


        private lateinit var dis: DataInputStream

        init {
            try {
                val inputStream: InputStream = socket.getInputStream()
                dis = DataInputStream(inputStream)
                Log.d("정보태그","InputStream성공")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("정보태그","InputStream에러-" + e.message)
            }
        }

        override fun run() {
            try {
                Log.d("정보태그","MessageThread run!!")

                while (isRunning) {
                    val msg = dis.readUTF()
                    Log.i("정보태그", "서버로부터 받은 메세지->$msg")

                    val jsonObject = JSONObject(msg)
                    Log.i("정보태그", "[sort]${jsonObject.getString("sort")}")
                    val sort = jsonObject.getString("sort")
                    val idx = jsonObject.getInt("idx")
                    val content = jsonObject.getString("content")
                    val orderTag = jsonObject.getString("order_tag")

                    var email = ""
                    var nickname = ""
                    var profileUrl = ""
                    var time = ""
                    if (sort == "message" || sort == "files") {
                        email = jsonObject.getString("email")
                        nickname = jsonObject.getString("nickname")
                        profileUrl = jsonObject.getString("profile_url")
                        time = jsonObject.getString("date")
                    }

                    // 리사이클러뷰에 넣을 객체 생성
                    val dc = when (sort) {
                        "notice" -> DataChatting(sort, content)
                        "message", "files" -> DataChatting(idx, room_idx, email, nickname, profileUrl, sort, content, time, orderTag)
                        else -> null
                    }

                    // 리사이클러뷰에 적용
                    dc?.let { // dc가 null이 아닌 경우
                        mainAdapter.dataList.add(it)
                        Log.i("정보태그", "채팅갯수=>${mainAdapter.dataList.size}")

                        runOnUiThread {
                            Log.d("정보태그", "어댑터 반영")
                            binding.rvChatting.scrollToPosition(mainAdapter.itemCount - 1)
                            mainAdapter.notifyDataSetChanged()
                        }

//                        val map = mapOf(
//                            "room_idx" to roomIdx.toString(),
//                            "title" to binding.txtTitle.text.toString(),
//                            "sort" to sort,
//                            "nickname" to nickname,
//                            "content" to content,
//                            "writer" to email,
//                            "email" to fshared.get_email()
//                        )

                        //fs.go_server("alarm_for_chatting", map) { result -> }
                    }
                }
            } catch (e: Exception) {
                Log.i("정보태그", "[isRunning중에러]e=>${e.message}")
            }
        }
    }







    /*
    메세지 작업을 처리하는 스레드
    : 전달받은 메세지를 계속(while) 불러들인다
     */
//    class MessageThread :

    fun onClick(view: View){
        when (view.id) {
            R.id.btn_send -> { // 텍스트 메세지 전송
                val thread = sendToServerThread()
                thread.start()
            }
            R.id.btn_plus -> {

            }
        }
    }



}