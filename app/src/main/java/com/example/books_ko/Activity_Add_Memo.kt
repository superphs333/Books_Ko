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
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.ItemTouchHelperCallback
import com.example.books_ko.Adapter.Adapter_Img_Memo
import com.example.books_ko.Data.ApiData
import com.example.books_ko.Data.Data_Img_Memo
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Function.AboutPicture
import com.example.books_ko.Interface.JsonPlaceHolderApi
import com.example.books_ko.databinding.ActivityAddMemoBinding
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
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

class Activity_Add_Memo : AppCompatActivity() {


    private lateinit var binding: ActivityAddMemoBinding

    val ap = AboutPicture
    val am = AboutMember

    var image_Uri: String? = null
    var image_bitmap: Bitmap? = null
    private var rl_camera // 카메라
            : ActivityResultLauncher<Intent>? = null
    private var rl_gallery // 갤러리
            : ActivityResultLauncher<Intent>? = null
    private var rl_crop // 크롭
            : ActivityResultLauncher<Intent>? = null
    private var rl_underline // 편집된 이미지
            : ActivityResultLauncher<Intent>? = null

    var book_idx = 0 // 책 idx
    var memo_idx = 0 // 메모 idx
    var email = ""

    // 리사이클러뷰
    var arrayList: ArrayList<Data_Img_Memo> = ArrayList()
    private lateinit var adapterImgMemo : Adapter_Img_Memo
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var helper : ItemTouchHelper

    override  fun onCreate(savedInstanceState: Bundle?) {
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

        // 이메일 값
        lifecycleScope.launch {
            email = AboutMember.getEmailFromRoom(applicationContext)
            Log.i("정보태그","email->$email")
        }

        /*
        리사이클러뷰 셋팅
         */
        // 변수 초기화
        linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL,false)
        adapterImgMemo = Adapter_Img_Memo(arrayList!!, applicationContext, this)
        binding!!.rvMemoImgs.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = adapterImgMemo
            // list변경될 때
            // adapterMyBook.dataMyBooks = arrayList!!
            //                adapterMyBook.notifyDataSetChanged()
        }
        helper = ItemTouchHelper(ItemTouchHelperCallback(adapterImgMemo))
        helper.attachToRecyclerView(binding.rvMemoImgs)  // ItemTouchHelper를 제공된 RecyclerView에 붙인다

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
                        val outputUri = Uri.fromFile(File(applicationContext.cacheDir, "cropped_image_${i}.jpg"))
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


                val timeStamp  = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val randomInt = (0..1000).random()
                val imageFileName = "cropppedImg_{$timeStamp}_{$randomInt}"
                // 이미지 URI가 캐시 디렉토리를 참조하는 경우 외부 저장소로 복사 (갤러리)
                if (resultUri != null && resultUri.toString().startsWith("file:///data/user/0/")) {
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
                    //resultUri = Uri.fromFile(outputFile)
                    // 파일 내용 제공자 URI 가져오기
                    val authority = applicationContext.packageName + ".provider"
                    val fileProviderUri = FileProvider.getUriForFile(applicationContext, authority, outputFile)
                    resultUri = fileProviderUri
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
                    Log.e("정보태그", "UCrop error: ${cropError.message}")
                }
            }
        }
        // 편집된 이미지
        rl_underline = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                Log.d("정보태그", "(in onActivityResult) result=${data?.getStringExtra("result")}")
                Log.d("정보태그", "(in onActivityResult) result=${data?.getStringExtra("position")}")

                // You should convert it to absolute path
                val url = data?.getStringExtra("result")
                Log.d("정보태그", "url=$url")

                // Change the image
                adapterImgMemo.dataList[data?.getStringExtra("position")?.toIntOrNull() ?: 0].img =
                    url!!
                adapterImgMemo.notifyDataSetChanged()
            }
        }
        adapterImgMemo.rl_underline = rl_underline as ActivityResultLauncher<Intent>



    }

    // 메모 내용 서버로 전송
    fun send_to_SERVER(view: View) {
        /*
        데이터 전송
         */
        // Retrofit 인터페이스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl(applicationContext.getString(R.string.server_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val myApi = retrofit.create(JsonPlaceHolderApi::class.java)

        // 전송할 값
        val forSendEmail =email.toRequestBody("text/plain".toMediaTypeOrNull())
        // accept_sort값 분기
        var tempAcceptSort = "Save_Memo"
        if(memo_idx!=0){
            tempAcceptSort = "Edit_Memo"
        }
        val accept_sort = tempAcceptSort.toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendMemoIdx =memo_idx.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendBookIdx =book_idx.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendImgSize =arrayList.size.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendMemo = binding.editMemo.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val forSendPage = binding.editPage.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val open = when (binding.spinnerSelectOpen.selectedItem.toString()) {
            "전체" -> "all"
            "팔로잉" -> "follow"
            else -> "no"
        }
        val forSendOpen = open.toRequestBody("text/plain".toMediaTypeOrNull())
        var images = mutableListOf<MultipartBody.Part>() // 이미지들을 담을 변수
        Log.d("정보태그","imgDataList->"+arrayList)
        for((index, imgData) in arrayList.withIndex()){
            // 파일이름
            val imgFileName = "Img_Book_Memo-${(1..1000000).random()}.jpg"
            var imageFile: File? = null
            if (ap.isAbsolutePath(imgData.img)) {
                imageFile = File(imgData.img!!)
            } else {
                val imgFileName = "Img_Book_Memo-${(1..1000000).random()}.jpg"
                imageFile = ap.getFileFromContentUri(Uri.parse(imgData.img), applicationContext, imgFileName)
            }
            val imageRequestBody  = imageFile!!.asRequestBody("image/jpeg".toMediaTypeOrNull())

            // 전송이름
            val fileName = "uploaded_file$index"
            val imagePart = MultipartBody.Part.createFormData(fileName, imgFileName, imageRequestBody)
            images.add(imagePart)
        }

        myApi.sendDatatoMemo(forSendBookIdx,accept_sort,forSendImgSize,forSendMemo,forSendPage,forSendOpen,forSendEmail,forSendMemoIdx,images).enqueue(object :
            Callback<ApiResponse<ApiData>> {
            override fun onResponse(call: Call<ApiResponse<ApiData>>, response: Response<ApiResponse<ApiData>>) {
                var toString: String = response.raw().toString()
                Log.i("정보태그","정보->${toString}")
                // 요청 성공 처리
                val result = response.body()

                if (result?.status == "success") {
                    if(memo_idx==0){ // 추가
                        Toast.makeText(
                            applicationContext, getString(R.string.add_memo), Toast.LENGTH_LONG
                        ).show()
                    }else{ // 수정
                        Toast.makeText(
                            applicationContext, getString(R.string.edit_memo), Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.toast_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                finish()
            }

            override fun onFailure(call: Call<ApiResponse<ApiData>>, t: Throwable) {
                // 요청 실패 처리
                Log.i("정보태그",t.message.toString())
            }
        })
    }

    // 카메라에서 이미지 선택
    fun Pick_From_Camera(view: View) {
        image_Uri = ap.cameraOnePicture(rl_camera!!, applicationContext,getString(R.string.img_memo))
    }
    fun Pick_From_Gallery(view: View) {
        ap.pick_from_gallery_imgs(rl_gallery!!);
    }
}