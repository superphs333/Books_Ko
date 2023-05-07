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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.books_ko.Function.AboutPicture
import com.example.books_ko.databinding.ActivityBookAddBinding
import com.yalantis.ucrop.UCrop
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    val imageFileName = "Profile_$timeStamp"+"_"
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


}
