package com.example.books_ko

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.books_ko.Class.GMailSender
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.Function.AboutPicture
import com.example.books_ko.databinding.ActivitySignupBinding
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import javax.mail.MessagingException
import javax.mail.SendFailedException


class Activity_Signup : AppCompatActivity() {

    companion object {
        // 필요한 권한과 요청 코드 정의
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private const val PERMISSION_REQUEST_CODE = 10
    }

    // 바인딩 변수
    private lateinit var binding: ActivitySignupBinding // Declare the binding variable
    val am = AboutMember;
    val ap = AboutPicture



    /*
    이메일용 변수
     */
    var gMailSender: GMailSender? = null // 이메일 보내는 객체
    var temp_email_string=""; // 이메일 인증 문자
    var email_no_double = false; // 이메일 중복 체크 여부
    var temp_email_not_duplication = ""; //  중복 확인 체크한(중복이 아닌) 이메일

    var nick_no_double = false;



    /*
    카메라, 갤러리 관련
     */
    // launher 선언
    private var rl_camera // 카메라
            : ActivityResultLauncher<Intent>? = null
    private val rl_gallery // 갤러리
            : ActivityResultLauncher<Intent>? = null
    var image_Uri: String? = null
    var image_bitmap: Bitmap? = null

    /*
    파일전송
     */
    // 서버에 전송할 파일
    var mFile_Input_Stream: FileInputStream? = null

    // 필요한 모든 권한이 부여되었는지 확인(bool값 반환)
        // REQUIRED_PERMISSIONS 의 모든 필요한 권한이 부여된 경우에 true반환
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
            // REQUIRED_PERMISSIONS : 권한 문자열의 컬렉션(목록이나 배열)
            // all : 중괄호 안에 주어진 조건을 만족하는 컬렉션의 모든 요소 검사
        // 현재 권한이 부여되었는지 확인(권한의 현재 상태 반환)
        ContextCompat.checkSelfPermission(
            baseContext, // 애플리케이션의 기본 컨텍스트
            it // 현재권한
        ) == PackageManager.PERMISSION_GRANTED
    }

    // ActivityCompat.requestPermissions에서 권한 요청 후 응답 처리 (권한 요청 처리)
    override fun onRequestPermissionsResult(
        requestCode: Int, // 권한 요청에 대한 요청 코드
        permissions: Array<String>, // 요청된 권한 목록
        grantResults: IntArray // 권한 부여 결과에 대한 배열(ackageManager.PERMISSION_GRANTED or PackageManager.PERMISSION_DENIED)
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) { // 요청 코드가 일치하는 경우에만 권한 처리 실행
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // You can request permissions again or show a message to the user
                Toast.makeText(this, "카메라를 사용하려면 카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()

                // 다시 사용자가 권한을 요청 하기
                showPermissionRationaleDialog()
            }
        } else { // 기본처리
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setMessage("카메라를 사용하려면 카메라 권한이 필요합니다.")
            .setPositiveButton("권한 요청") { _, _ ->
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater);
        setContentView(binding.root)




        /*
        비밀번호 정규식에 일치하는지 확인하기
         */
        binding.editPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                // 비밀번호 정규식 확인
                if (am.chk_regex("pw", binding.editPw)) {
                    binding.txtPwInfo.text = "사용가능한 비밀번호 입니다."
                } else {
                    binding.txtPwInfo.text = "비밀번호 형식을 확인해주세요."
                }

                // 비밀번호 = 비밀번호 확인 문구 일치 여부 (비밀번호 확인 문구 부분이 빈칸이 아닌 경우에)
                if (!binding.editPwDouble.text.toString().equals("")) {
                    if (binding.editPw.text.toString()
                            .equals(binding.editPwDouble.text.toString())
                    ) {
                        binding.txtPwDoubleInfo.text = "비밀번호가 일치합니다"
                    } else {
                        binding.txtPwDoubleInfo.text = "입력한 비밀번호를 확인해주세요"
                    }
                }
            }
        })


        /*
        비밀번호=비밀번호 확인 체크하기
         */
        binding.editPwDouble.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (binding.editPw.text.toString().equals(binding.editPwDouble.text.toString())) {
                    binding.txtPwDoubleInfo.text = "비밀번호가 일치합니다"
                } else {
                    binding.txtPwDoubleInfo.text = "입력한 비밀번호를 확인해주세요"
                }
            }
        })

        /*
        중복 확인 후에(nick_no_double = true)
        닉네임 입력칸에 입력값이 있을 경우 -> 중복 확인을 다시 해줘야 함
         */
        binding.editNick.addTextChangedListener(object : TextWatcher {
            var before_nick = binding.editNick.text.toString()
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (nick_no_double == true) { // 중복확인 체크가 끝난경우
                    // 만약 이전 닉네임과 같은 경우가 아니라면 nick_no_double=false
                    // + txt_nick_info 변경
                    if (!binding.editNick.equals(before_nick)) {
                        binding.txtNickInfo.text = "중복확인 문구"
                        nick_no_double = false
                    }
                }
            }
        })



    }

    fun send_email(view: View) {

        // 입력받은 이메일
        val input_email: String = binding.editEmail.text.toString()
        Log.i("정보태그", "input_email=>$input_email")

        /*
        이메일 형식인지 확인
         */
        if (!am.chk_regex("email", binding.editEmail)) {
            Toast.makeText(
                applicationContext,
                applicationContext.getString(R.string.toast_email),
                Toast.LENGTH_SHORT
            ).show()
            binding.editEmail.requestFocus()
            return
        }


        /*
        이메일 중복 여부 확인
         */
        am.chk_double("email", binding.editEmail, object : AboutMember.VolleyCallback {
            override fun onSuccess(result: Boolean) {
                Log.i("정보태그", "chk_double vollycallback=>$result")

                // 중복이 아닌 경우에만 이메일 전송
                if(!result){
                    // 중복 확인 여부 셋팅
                    email_no_double = true;
                    // 인증된 이메일 셋팅
                    temp_email_not_duplication = binding.editEmail.text.toString();


                    /*
                    이메일 전송
                     */
                    gMailSender = GMailSender();
                    // 임시문자 생성
                    temp_email_string = gMailSender!!.getEmailCode().toString()
                    // 이메일 전송
                    // 이메일 전송
                    val email_title: String = getString(R.string.app_title) + "에서 온 인증문자입니다"
                    val email_content = "다음의 인증문자를 입력하세요 :$temp_email_string"
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            gMailSender!!.sendMail(
                                email_title,
                                email_content,
                                binding.editEmail.text.toString()
                            )
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "이메일이 전송되었습니다!", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: SendFailedException) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "이메일 형식이 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: MessagingException) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(applicationContext, "인터넷 연결을 확인해주십시오+", Toast.LENGTH_SHORT).show()
                                Log.d("정보태그", "MessagingException=>" + e.message)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    applicationContext,
                                    "예상치 못한 문제가 발생하였습니다. 다시 한 번 시도해주세요",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("정보태그", "Exception=>" + e.message)
                            }
                        }
                    }


                }
            }
        })

    }


    fun Check_Nick_Double(view: View?) {
        // 닉네임 정규식 확인
        if (!am.chk_regex("nickname", binding.editNick)) {
            Toast.makeText(
                applicationContext,
                applicationContext.getString(R.string.toast_nickname),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // 닉네임 중복 확인
        am.chk_double("nickname", binding.editNick, object : AboutMember.VolleyCallback {
            override fun onSuccess(result: Boolean) {
                Log.i("정보태그", "(닉네임)chk_double vollycallback=>$result")
                if (result) { // 중복 o
                    nick_no_double = false
                    binding.txtNickInfo.text = "사용 불가능한 닉네임입니다"
                } else { // 중복x
                    nick_no_double = true
                    binding.txtNickInfo.text = "사용 가능한 닉네임입니다"
                }
            }
        })
    }

    fun camera_for_profile(view: View?){
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            // 앱이 사용자에게 필요한 권한을 요청하게 한다
            ActivityCompat.requestPermissions(
                // ActivityCompat.requestPermissions : 안드로이드 앱에서 실행 시 권한을 요청하 ㄹ때 사용
                this,
                REQUIRED_PERMISSIONS, // 요청 할 권한의 배열
                PERMISSION_REQUEST_CODE
                    // 권한 요청에 대한 고유한 요청 코드
                    // onRequestPermissionsResult 에서 권한 요청 결과를 처리 할 때 사용됨
            )
        }
    }

    // 카메라를 시작
    private fun startCamera() {
        Log.i("정보태그","startCamera()실행!")

        // launcher 선언
        rl_camera = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                Log.i("정보태그", "(registerForActivityResult)카메라")
                Log.i("정보태그", "image_Uri=>$image_Uri")
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
                UCrop.of(return_uri,)
            }
        }

        // 임시파일 가져오고, 카메라로 전달
        image_Uri = ap.cameraOnePicture(rl_camera!!,applicationContext)

    }




}