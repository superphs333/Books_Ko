package com.example.books_ko

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.books_ko.Data.ApiData
import com.example.books_ko.Data.UserData
import com.example.books_ko.DataBase.UserDatabase
import com.example.books_ko.Function.AboutPicture
import com.example.books_ko.Interface.JsonPlaceHolderApi
import com.example.books_ko.databinding.ActivityBookAddBinding
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class Activity_Book_Add : AppCompatActivity() {

    private lateinit var binding: ActivityBookAddBinding
    private lateinit var rl_gallery: ActivityResultLauncher<Intent> // 갤러리
    private lateinit var rl_crop:ActivityResultLauncher<Intent> // 크롭
    val ap = AboutPicture
    var image_Uri: String? = null
    var image_bitmap: Bitmap? = null
    private lateinit var database : UserDatabase
    var email = ""
    var idx = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = Room.databaseBuilder(applicationContext, UserDatabase::class.java, "app_database").build()


        GlobalScope.launch {
            val userData: UserData = database.userDao().getUser2()
            withContext(Dispatchers.Main) {
                Log.i("정보태그","userData->${userData}")
                email = userData.email
                Log.i("정보태그","email->${email}")
            }
        }


        // spinner
        val data = resources.getStringArray(R.array.read_status)
        val adapter = ArrayAdapter<String>(
            applicationContext,
            android.R.layout.simple_dropdown_item_1line,
            data
        )
        binding.categoryReadStatus.adapter = adapter

        // 이미지 길게 누르면 -> 이미지 삭제(기본이미지로 변경 + 이미지 변수=null)
        binding.imgBook.setOnLongClickListener {
            val builder = AlertDialog.Builder(this@Activity_Book_Add)
            builder.setTitle("알림") // AlertDialog 제목
            builder.setMessage(getString(R.string.check_delete)) // 내용
            builder.setPositiveButton(
                "예"
            ) { dialog, which ->
                Log.d("정보태그", "예 누름")
                // 이미지 기본 이미지로 변경
                binding.imgBook.setImageResource(R.drawable.basic_book_cover)
                image_bitmap = null
                image_Uri = ""
            }
            builder.setNegativeButton(
                "아니오"
            ) {
                    dialog, which -> Log.d("정보태그", "아니요 누름")
            }
            builder.setNeutralButton("취소", null)
            builder.create().show() //보이기

            true
        }

        rl_gallery = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                // 이미지 크롭하기
                val imageUri = result.data!!.data!!
                val outputUri = Uri.fromFile(File(applicationContext.cacheDir, "cropped_image.jpg"))
                var cropIntent2 = UCrop.of(imageUri, outputUri)
                    .withAspectRatio(1f, 1f) // 사각형 비율을 사용하려면 이 줄을 삭제하거나 주석 처리
                    .getIntent(applicationContext)
                rl_crop!!.launch(cropIntent2)
            }
        }

        rl_crop = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.i("정보태그", "result->" + result)
            if (result.resultCode == Activity.RESULT_OK) {

                var resultUri = result.data?.let { UCrop.getOutput(it) };
                Log.i("정보태그","(크롭후)resultUri->"+resultUri)

                // 이미지 URI가 캐시 디렉토리를 참조하는 경우 외부 저장소로 복사
                if (resultUri != null && resultUri.toString().startsWith("file:///data/user/0/")) {
                    val timeStamp  = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    val imageFileName = "Image_Book_Cover_$timeStamp"+"_"
                    val inputStream = contentResolver.openInputStream(resultUri) // 이미지 파일 읽는다
                    val outputFile = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${imageFileName}.jpg") // 외부 저장소에 새 이미지 파일 생성
                    val outputStream = FileOutputStream(outputFile)

                    // 이미지 파일 복사
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input?.copyTo(output)
                        }
                    }

                    // 외부 저장소에 있는 새 파일의 URI로 변경
                    resultUri = Uri.fromFile(outputFile)
                    image_Uri = resultUri?.toString()?.removePrefix("file://")
                }
                // 이미지 셋팅
                binding.imgBook.setImageURI(resultUri)
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(result.data!!)
                if (cropError != null) {
                    Log.e("TAG", "UCrop error: ${cropError.message}")
                }
            }
        }
    }

    /*
    갤러리에서 이미지 가져오기
     */
     fun get_Img(view : View){
        ap.galleryOnePictrue(rl_gallery)
     }

    /*
    도서 정보 서버로 보내기
     */
    fun Book_Add(view: View) {
        var title = binding.editTitle.getText().toString()
        var authors = binding.editAuthors.getText().toString()
        var publisher = binding.editPublisher.getText().toString()
        var isbn = binding.editIsbn.getText().toString() // isbn
        var total_page ="0"
        var contents = binding.editContent.getText().toString() // 요약정보
        var rating = binding.rating.getRating().toString()
        //  status -> 읽고싶은:3, 읽는중:1, 읽음:2
        var status = "3"
        when(binding.categoryReadStatus.selectedItem.toString()) {
            getString(R.string.read_bucket) -> status = "3" // 읽고 싶은 경우
            getString(R.string.read_reading) -> status = "1" // 읽는 중인 경우
            else -> status = "2" // 읽은 경우
        }

        /*
        검사
         */
        when {
            // 제목
            title=="" -> {
                Toast.makeText(
                    applicationContext,
                    "책 제목을 입력해주세요",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            // 글쓴이
            authors=="" -> {
                Toast.makeText(
                    applicationContext,
                    "작가를 입력해주세요",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            // isbn
            isbn=="" -> {
                Toast.makeText(
                    applicationContext,
                    "isbn를 입력해주세요",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            // 이후에도 몇 개 더 검사해야 함
        }
        Log.i("정보태그", "모든 정규식 통과")



        /*
        데이터 전송
         */
        // Retrofit 인터페이스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl(applicationContext.getString(R.string.server_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val myApi = retrofit.create(JsonPlaceHolderApi::class.java)
        val accept_sort = "Save_in_my_book".toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendEmail =email.toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendTitle = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendAuthors = authors.toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendPublisher = publisher.toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendIsbn = isbn.toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendTotalPage = total_page.toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendContents = contents.toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendRating = rating.toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendStatus = status.toRequestBody("text/plain".toMediaTypeOrNull())
        val filePath = File(image_Uri) // 이미지 파일의 경로를 가져옵니다.
        val profileImageFile = File(filePath.absolutePath) // 이미지 파일 객체를 생성합니다.
        val profileImage = profileImageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val profileImagePart = MultipartBody.Part.createFormData("uploadedfile", profileImageFile.name, profileImage)
        myApi.sendDatatoBookAdd(accept_sort,forSendTitle, forSendAuthors, forSendPublisher,forSendIsbn, forSendTotalPage, forSendContents,forSendEmail,forSendRating,forSendStatus, profileImagePart).enqueue(object :
            Callback<ApiResponse<ApiData>> {
            override fun onResponse(call: Call<ApiResponse<ApiData>>, response: Response<ApiResponse<ApiData>>) {
                var toString: String = response.raw().toString()
                Log.i("정보태그","정보->${toString}")
                // 요청 성공 처리
                val result = response.body()

                if (result?.status == "success") {
                    if(idx==0){ // 추가
                        Toast.makeText(
                            applicationContext, getString(R.string.save_in_my_book), Toast.LENGTH_LONG
                        ).show()
                    }else{ // 수정
                        Toast.makeText(
                            applicationContext, getString(R.string.edit_book), Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.toast_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // 책 리스트 페이지로 이동
                finish()
            }

            override fun onFailure(call: Call<ApiResponse<ApiData>>, t: Throwable) {
                // 요청 실패 처리
                Log.i("정보태그",t.message.toString())
            }
        })
    }


}
