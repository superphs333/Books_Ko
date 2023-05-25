package com.example.books_ko

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.books_ko.Adapter.AdapterChatting
import com.example.books_ko.Data.DataChatting
import com.example.books_ko.Function.AboutChatting
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Function.AboutPicture
import com.example.books_ko.databinding.ActivityChattingBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*

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

    private var rl_gallery // 갤러리
            : ActivityResultLauncher<Intent>? = null
    private var rl_crop // 크롭
            : ActivityResultLauncher<Intent>? = null


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

        rl_gallery = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if(result.data==null){
                // data == null일 때는 -> 앨범에서 뒤로 가기 눌렀을 때
                // data가 없기 때문에 생기는 오류를 잡아주기 위함
                Log.i("정보태그","선택없이 뒤로가기")
            }else{
                if(result.data!!.clipData==null){ // 이미지 한 장 선택
                    Log.i("정보태그", "이미지 한 장 선택")

                    val imageUri = result.data!!.data!!
                    val absolutePath = imageUri?.let { uri ->
                        getPathFromUri(uri)
                    }
                    Log.i("정보태그","outputUri->$absolutePath")

                    // 이미지를 서버에 전송하기
                    val fileSender: FileSender = FileSender(absolutePath!!)
                    fileSender.start()


                }else{// 이미지 여러장 선택
                    val clipData = result.data!!.clipData
                    Log.i("정보태그", "이미지 여러장 선택, 갯수->{${clipData!!.itemCount}}")
                    // 이미지 경로 저장 list
                    var img_list: MutableList<String> = mutableListOf()
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri
                        Log.i("정보태그","imageUri->$imageUri")

                        // 이미지 절대 경로
                        val absolutePath = imageUri?.let { uri ->
                            getPathFromUri(uri)
                        }
                        Log.i("정보태그","absolutePath->$absolutePath")


                        img_list.add(absolutePath!!)
                    }
                    // 이미지를 서버에 전송하기
                    val filesSender: FilesSender = FilesSender(img_list)
                    filesSender.start()
                }
            }
        }



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

    private fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val path = columnIndex?.let { cursor?.getString(it) }
        cursor?.close()
        return path
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

                        /*
                        알림전송
                         */
                        // [개선] 서버에서 메세지도 보내고, 알림도 보내는 방향 생각해보기

//                        val map = mapOf(
//                            "room_idx" to room_idx.toString(),
//                            "title" to binding.txtTitle.text.toString(),
//                            "sort" to sort,
//                            "nickname" to nickname,
//                            "content" to content,
//                            "writer" to email,
//                            "email" to email
//                        )

                        //fs.go_server("alarm_for_chatting", map) { result -> }
                    }
                }
            } catch (e: Exception) {
                Log.i("정보태그", "[isRunning중에러]e=>${e.message}")
            }
        }
    }


    inner class FileSender(private val filePath: String) : Thread() {
        // sort -> 파일명 -> 파일 사이즈 -> 파일

        private var fileName: String = ""

        private lateinit var dos: DataOutputStream
        private lateinit var fis: FileInputStream
        private lateinit var bis: BufferedInputStream

        init {
            // 파일명
            val generator = Random()
            val n = 1000000
            fileName = "Chat_image_" + generator.nextInt(n) + ".jpg"

            // 데이터 전송용 스트림 생성
            try {
                dos = DataOutputStream(memberSocket.getOutputStream())
                Log.i("정보태그", "[FileSender] DataOutputStream 성공")
            } catch (e: IOException) {
                Log.i("정보태그", "[FileSender] DataOutputStream 에러 - ${e.message}")
                e.printStackTrace()
            }
        }

        override fun run() {
            try {
                dos.writeUTF("file")
                dos.flush()

                // 전송할 파일을 읽어서 Socket Server에 전송
                val result = fileRead()
                Log.i("정보태그", "[FileSender] result: $result")
            } catch (e: IOException) {
                Log.i("정보태그", "[FileSender] dos.writeUTF 에러 - ${e.message}")
                e.printStackTrace()
            }
        }

        // 파일을 전송하는 함수
        private fun fileRead(): String {
            var result = ""
            try {
                // 파일명 전송
                dos.writeUTF(fileName)

                /*
                파일을 읽어서 서버에 전송
                 */
                val file = File(filePath)

                // 파일 사이즈 보내기(얼만큼 보낼 것인지 알려주기)
                dos.writeUTF(file.length().toString())
                dos.flush()

                fis = FileInputStream(file) // 파일에서 데이터를 읽기
                bis = BufferedInputStream(fis)
                 // FileInputStream보다 더 효율적으로 입출력 위해

                val data = ByteArray(4096)
                var len: Int
                while (bis.read(data).also { len = it } != -1) {
                    dos.write(data, 0, len)
                    dos.flush()
                }

                // 서버에 전송(서버로 보내기 위해서 flush를 사용)
                dos.flush()
                result = "SUCCESS"
            } catch (e: IOException) {
                Log.i("정보태그", "dos.writeUTF 에러 - ${e.message}")
                e.printStackTrace()
                result = "ERROR"
            } finally {
                bis.close()
            }

            return result
        }
    }


    inner class FilesSender(private val imgList: List<String>) : Thread(){
        private lateinit var dos: DataOutputStream
        private lateinit var fis: FileInputStream
        private lateinit var bis: BufferedInputStream

        init {
            try {
                // 데이터 전송용 스트림 생성
                dos = DataOutputStream(memberSocket.getOutputStream())
            } catch (e: IOException) {
                Log.d("정보태그", "DataOutputStream에러-${e.message}")
                e.printStackTrace()
            }
        }

        override fun run(){
            for((i,filePath) in imgList.withIndex()){
                try {
                    sleep(300)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                Log.d("정보태그", "=====i=$i 시작=====")

                try{
                    // 파일 전송을 할 것이라는 것을 서버에 알린다
                    dos.writeUTF("files")

                    /*
                    order_tag를 보냄
                    1 -> 맨 첫번째
                    2 -> 중간
                    3 -> 맨 마지막
                     */
                    val size = imgList.size
                    dos.writeUTF(
                        when{
                            size == 1 -> "0"
                            i == 0 -> "1"
                            i == size-1 -> "3"
                            else -> "2"
                        }
                    )
                    dos.flush()

                    // 전송할 파일을 읽어서 Server에 전송
                    val result = fileRead(dos, filePath)

                }catch (e: Exception){
                    e.printStackTrace()
                    Log.d("정보태그", "dos.writeUTF에러-${e.message}")
                }finally {
                    // 리소스 초기화
                    try {
                        fis.close()
                    } catch (e: Exception) {
                        Log.i("정보태그", "bis.close()에러->${e.message}")
                        e.printStackTrace()
                    }
                }
            } // end for
        }

        fun fileRead(dos: DataOutputStream, filePath: String): String{
            var result = ""
            try{
                val now = Date()
                val formatter = SimpleDateFormat("yyyy_mm_dd_hh_mm_ss")
                val formatedNow = formatter.format(now)
                val generator = Random()
                var n = 1000000
                n = generator.nextInt(n)
                val fileNm = "Img_Chat_$formatedNow$n.jpg"
                dos.writeUTF(fileNm)
                Log.i("정보태그", "파일 이름($fileNm)을 전송하였습니다.")


                /*
                파일을 읽어서 서버에 전송
                 */
                val file = File(filePath)
                // 파일 사이즈 전송
                dos.writeUTF(file.length().toString())
                dos.flush()
                // 파일 전송
                fis = FileInputStream(file) // 파일을 데이터에서 읽기
                bis = BufferedInputStream(fis) // FileInputStream보다 더 효율적으로 입출력 위해
                val size = 4096
                val data = ByteArray(size)
                var len: Int
                while (bis.read(data).also { len = it } != -1) {
                    dos.write(data, 0, len)
                    dos.flush()
                }

                // 서버에 전송(서버로 보내기 위해서 flush를 사용)
                result = "SUCCESS";


            }catch(e: Exception){
                Log.d("정보태그", "dos.writeUTF에러-${e.message}")
                e.printStackTrace()
                result = "ERROR"
            }finally {
                Log.i("정보태그", "bis,fis close")
                try {
                    bis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    fis.close()
                } catch (e: IOException) {
                    Log.d("정보태그", "bis.close()에러-${e.message}")
                    e.printStackTrace()
                }
            }
            return result
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
            R.id.btn_plus -> { // 지금은 일단 이미지 전송만, 추후 더 많은 걸 전송 할 수 있도록 추가
                AboutPicture.pick_from_gallery_imgs(rl_gallery!!)
            }
        }
    }



}