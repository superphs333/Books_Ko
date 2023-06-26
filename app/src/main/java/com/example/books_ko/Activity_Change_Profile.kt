package com.example.books_ko

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.books_ko.Data.ApiData
import com.example.books_ko.Data.ApiResponse
import com.example.books_ko.DataBase.UserDatabase
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Function.AboutPicture
import com.example.books_ko.Interface.JsonPlaceHolderApi
import com.example.books_ko.databinding.ActivityChangeProfileBinding
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

class Activity_Change_Profile : AppCompatActivity() {

    private lateinit var binding : ActivityChangeProfileBinding

    private lateinit var database : UserDatabase

    var email = ""

    /*
    카메라, 갤러리 관련
     */
    // launher 선언
    private var rl_camera // 카메라
            : ActivityResultLauncher<Intent>? = null
    private var rl_gallery // 갤러리
            : ActivityResultLauncher<Intent>? = null
    private var rl_crop // 크롭
            : ActivityResultLauncher<Intent>? = null
    var image_Uri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // [개선] 권한설정

        /*
        기존 이미지 셋팅
         */
        database = Room.databaseBuilder(applicationContext, UserDatabase::class.java, "app_database").build()
        val userLiveData = database.userDao().getUser()
        CoroutineScope(Dispatchers.IO).launch{
            withContext(Dispatchers.Main){
                userLiveData.observe(this@Activity_Change_Profile, androidx.lifecycle.Observer { userLiveData ->
                    email = userLiveData.email
                    Log.i("정보태그","email->$email")
                    CoroutineScope(Dispatchers.IO).launch{
                        image_Uri = AboutMember.getMemberInfo(applicationContext,email,"profile_url")
                        if(image_Uri!=null || image_Uri!=""){
                            withContext(Dispatchers.Main){
                                // 프로필 이미지 셋팅
                                val glideImg = if (image_Uri!!.contains(getString(R.string.img_profile))) {
                                    getString(R.string.server_url).removeSuffix("/") + image_Uri
                                } else image_Uri
                                Log.i("정보태그","glideImg->$glideImg")
                                Glide.with(applicationContext).load(glideImg).into(binding.imgProfile)
                            }
                        }

                    }
                })
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
                binding.imgProfile.setImageURI(resultUri)


            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(result.data!!)
                if (cropError != null) {
                    Log.e("TAG", "UCrop error: ${cropError.message}")
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


        rl_gallery = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // 이미지 크롭하기
                val imageUri = result.data!!.data!!
                val outputUri = Uri.fromFile(File(applicationContext.cacheDir, "cropped_image.jpg"))
                var cropIntent2 = UCrop.of(imageUri, outputUri)
                    .withAspectRatio(1f, 1f) // 사각형 비율을 사용하려면 이 줄을 삭제하거나 주석 처리하세요.
                    .getIntent(applicationContext)
                rl_crop!!.launch(cropIntent2)
            }
        }
    }






    /*
    카메라에서 프로필이미지 셋팅
     */
    fun camera_ksfor_profile(view: View) {
        image_Uri = AboutPicture.cameraOnePicture(rl_camera!!, applicationContext,getString(R.string.img_profile))
    }



    /*
    갤러리에서 프로필 이미지 셋팅
     */
    fun gallery_for_profile(view: View) {
        AboutPicture.gallery_one_picture(rl_gallery!!);
    }

    /*
    프로필 이미지 삭제
     */
    fun delete_profile_picture(view: View) {
        // 프로필 이미지에 기본 이미지 셋팅
        binding.imgProfile.setImageResource(R.drawable.basic_profile_img)
        // uri에 셋팅되어 있는 값 초기화
        image_Uri = null
    }

    /*
    프로필 이미지 서버로 전송
     */
    // [개선] 이미지 없을 때 처리
    fun send_to_server(view: View) {
        val retrofit = Retrofit.Builder()
            .baseUrl(applicationContext.getString(R.string.server_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val myApi = retrofit.create(JsonPlaceHolderApi::class.java)
        val accept_sort = "Update_Profile_img".toRequestBody("text/plain".toMediaTypeOrNull())
        val emailToSend = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val filePath = File(image_Uri) // 이미지 파일의 경로를 가져옵니다.
        val profileImageFile = File(filePath.absolutePath) // 이미지 파일 객체를 생성합니다.
        val profileImage = profileImageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val profileImagePart = MultipartBody.Part.createFormData("uploadedfile", profileImageFile.name, profileImage)
        myApi.sendDataToChangeProfileUrl(accept_sort,emailToSend, profileImagePart).enqueue(object :
            Callback<ApiResponse<ApiData>> {
            override fun onResponse(call: Call<ApiResponse<ApiData>>, response: Response<ApiResponse<ApiData>>) {
                var toString: String = response.raw().toString()
                Log.i("정보태그","정보->${toString}")
                // 요청 성공 처리
                val result = response.body()

                if (result?.status == "success") {
                    Toast.makeText(applicationContext, R.string.profile_img_update_ok, Toast.LENGTH_SHORT).show()
                    finish() // 액티비티 종료
                } else {
                    // 서버 응답이 실패했을 때 처리할 코드 작성
                    Toast.makeText(
                        applicationContext,
                        "죄송합니다. 문제가 생겼습니다. 다시 시도해주세요",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }

            override fun onFailure(call: Call<ApiResponse<ApiData>>, t: Throwable) {
                // 요청 실패 처리
                Log.i("정보태그",t.message.toString())
            }
        })
    }
}