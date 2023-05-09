package com.example.books_ko

import android.app.Activity
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.books_ko.Adapter.Adapter_Img_Memo
import com.example.books_ko.Data.Data_Img_Memo
import com.example.books_ko.Function.AboutPicture
import com.example.books_ko.databinding.ActivityAddMemoBinding
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Activity_Add_Memo : AppCompatActivity() {

    // 이건 oncreate 밖에
    private lateinit var binding: ActivityAddMemoBinding

    val ap = AboutPicture
    var image_Uri: String? = null
    var image_bitmap: Bitmap? = null
    private var rl_camera // 카메라
            : ActivityResultLauncher<Intent>? = null
    private var rl_gallery // 갤러리
            : ActivityResultLauncher<Intent>? = null
    private var rl_crop // 크롭
            : ActivityResultLauncher<Intent>? = null

    var book_idx = 0 // 책 idx
    var memo_idx = 0 // 메모 idx

    // 리사이클러뷰
    var arrayList: ArrayList<Data_Img_Memo> = ArrayList()
    private lateinit var adapterImgMemo : Adapter_Img_Memo
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        book_idx = intent.getIntExtra("book_idx", 0)
        Log.i("정보태그", "book_idx=>$book_idx")
        binding.txtTitle.text = intent.getStringExtra("title") // 제목셋팅
        // spinner셋팅
        val data = resources.getStringArray(R.array.select_memo_view)
        val adapterSpinner = ArrayAdapter(applicationContext, android.R.layout.simple_dropdown_item_1line, data)
        binding.spinnerSelectOpen.adapter = adapterSpinner

        /*
        리사이클러뷰 셋팅
         */
        // 변수 초기화
        linearLayoutManager = LinearLayoutManager(applicationContext)
        adapterImgMemo = Adapter_Img_Memo(arrayList!!, applicationContext, this)
        binding!!.rvMemoImgs.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = adapterImgMemo
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL)) // 가로나열
            // list변경될 때
            // adapterMyBook.dataMyBooks = arrayList!!
            //                adapterMyBook.notifyDataSetChanged()
        }

        /*
        사진 관련 registerForActivityResult
         */
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

                    // 이미지 크롭하기
                    val imageUri = result.data!!.data!!
                    val outputUri = Uri.fromFile(File(applicationContext.cacheDir, "cropped_image.jpg"))
                    var cropIntent2 = UCrop.of(imageUri, outputUri)
                        .withAspectRatio(1f, 1f) // 사각형 비율을 사용하려면 이 줄을 삭제하거나 주석 처리
                        .getIntent(applicationContext)
                    rl_crop!!.launch(cropIntent2)

                }else{// 이미지 여러장 선택
                    val clipData = result.data!!.clipData
                    Log.i("정보태그", "이미지 여러장 선택, 갯수->{${clipData!!.itemCount}}")
                    for (i in 0 until clipData.itemCount) {
                        val imageUri = clipData.getItemAt(i).uri
                        val outputUri = Uri.fromFile(File(applicationContext.cacheDir, "cropped_image.jpg"))
                        var cropIntent2 = UCrop.of(imageUri, outputUri)
                            .withAspectRatio(1f, 1f) // 사각형 비율을 사용하려면 이 줄을 삭제하거나 주석 처리
                            .getIntent(applicationContext)
                        rl_crop!!.launch(cropIntent2)
                    }
                }
            }
        }
        rl_camera = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.i("정보태그", "(registerForActivityResult)카메라")
                Log.i("정보태그", "(카메라)image_Uri=>$image_Uri")
                val original_uri = Uri.parse(image_Uri)
                var return_uri: Uri? = null
                return_uri = if (original_uri.scheme == null) {
                    Log.i("정보태그", "original_uri.getScheme()==null")
                    Uri.fromFile(File(image_Uri))
                } else {
                    Log.i("정보태그", "original_uri.getScheme()!=null")
                    original_uri
                }
                Log.i("정보태그", "return_uri=$return_uri")
                // 앞에 file://이 붙어서 나옴

                // 이미지 크롭하기
                var cropIntent = UCrop.of(return_uri, return_uri)
                    .withAspectRatio(1f, 1f) // 사각형 비율을 사용하려면 이 줄을 삭제하거나 주석 처리하세요.
                    .getIntent(applicationContext)
                rl_crop!!.launch(cropIntent)
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
                    val imageFileName = getString(R.string.img_memo)+"_{$timeStamp}"
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
                    Log.i("정보태그","image_Uri->$image_Uri")

                }
                // 이미지를 list에 추가한다
                val dim = Data_Img_Memo(image_Uri!!)
                arrayList!!.add(dim)
                adapterImgMemo.dataList = arrayList!!
                adapterImgMemo.notifyDataSetChanged()


            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(result.data!!)
                if (cropError != null) {
                    Log.e("TAG", "UCrop error: ${cropError.message}")
                }
            }
        }


    }

    fun send_to_SERVER(view: View) {}

    // 카메라에서 이미지 선택
    fun Pick_From_Camera(view: View) {
        image_Uri = ap.cameraOnePicture(rl_camera!!, applicationContext,getString(R.string.img_memo))
    }
    fun Pick_From_Gallery(view: View) {
        ap.gallery_one_picture(rl_gallery!!);
    }
}