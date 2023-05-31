package com.example.books_ko

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import android.Manifest
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.books_ko.DataBase.UserDatabase
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val am = AboutMember
    val actvity = this
    private lateinit var database : UserDatabase

    /*
    구글로그인
     */
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mAuth: FirebaseAuth? = null
    private var rl_google_login: ActivityResultLauncher<Intent>? = null

    /*
    권한관련
     */
    private val CAMERA_PERMISSION_REQUEST_CODE = 1
    private val STORAGE_PERMISSION_REQUEST_CODE = 2
    private val CAMERA_AND_STORAGE_PERMISSION_REQUEST_CODE = 3


    // 권한 요청
    private fun requestPermissions() {
        val cameraPermission = Manifest.permission.CAMERA
        val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

        val cameraPermissionGranted = ContextCompat.checkSelfPermission(this, cameraPermission) == PackageManager.PERMISSION_GRANTED
        val storagePermissionGranted = ContextCompat.checkSelfPermission(this, storagePermission) == PackageManager.PERMISSION_GRANTED

        val permissionsToRequest = ArrayList<String>()

        if (!cameraPermissionGranted) {
            permissionsToRequest.add(cameraPermission)
        }
        if (!storagePermissionGranted) {
            permissionsToRequest.add(storagePermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), CAMERA_AND_STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_AND_STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 카메라와 외부 저장소 권한이 모두 허용된 경우 처리할 작업 수행
                } else {
                    // 하나 이상의 권한이 거부된 경우 처리할 작업 수행
                    for (i in permissions.indices) {
                        val permission = permissions[i]
                        val grantResult = grantResults[i]
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                                // "다시 묻지 않기" 처리된 경우 앱 설정으로 이동
                                showPermissionDeniedDialog()
                            } else {
                                // 다시 묻기 처리되지 않은 경우 앱 종료
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("권한 거부됨")
        builder.setMessage("권한이 거부되었습니다. 앱 설정으로 이동하여 권한을 허용해주세요.")
        builder.setPositiveButton("설정으로 이동") { _, _ ->
            navigateToAppSettings()
        }
        builder.setNegativeButton("취소", null)
        builder.create().show()
    }

    private fun navigateToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }







    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        Log.i("정보태그","MainActivity onCreate")

        val apiLevel = android.os.Build.VERSION.SDK_INT
        Log.d("정보태그", "API Level: $apiLevel")

        requestPermissions()

        // 자동로그인 / user데이터베이스에 있는 모든 정보 삭제
        database = Room.databaseBuilder(applicationContext, UserDatabase::class.java, "app_database").build()
            // app_database라는 이름을 가지는 Room데이터베이스를 생성하고 인스턴스를 databse변수에 저장
            // build() -> 설정된 옵션에 따라 데이터베이스 인스턴스를 생성함
        val userLiveData = database.userDao().getUser()
            // 사용자를 비동기적으로 가져온다
        // userLiveData를 관찰하는데, user라는 변수를 통해 관찰된 데이터베이스를 받는다
            // Observer의 람다 식({ user -> ... }) 내에서 데이터가 변경될 때 수행할 동작을 정의
        userLiveData.observe(this, Observer { user ->
            if (user != null) {
                // 데이터가 있다면 정보 출력
                Log.d("정보태그", "Email: ${user.email}, Nickname: ${user.nickname}, Auto Login: ${user.auto_login}")
                // 자동 로그인 여부 확인
                if (user.auto_login) { // 자동로그인
                    Log.i("정보태그", "자동로그인")
                    val intent = Intent(applicationContext, Activity_Main2::class.java)
                    startActivity(intent)
                    finish()
                } else { // 자동로그인 x
                    Log.i("정보태그", "자동로그인x")
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            database.userDao().clearAllUsers()
                        }
                    }
                    setContentView(binding.root)
                }
            } else {
                Log.i("정보태그", "데이터 없음")
                setContentView(binding.root)
            }
        })
//                if (userLiveData.value?.auto_login == true) {// 자동로그인 상태 -> 페이지 이동
//                    Log.i("정보태그", "자동로그인")
//                    val intent = Intent(applicationContext, Activity_Main2::class.java)
//                    startActivity(intent)
//                    finish()
//                } else if (userLiveData.value == null){
//                    Log.i("정보태그", "자동로그인x_user정보 없음")
//                    database.userDao().clearAllUsers()
//                }else {
//                    Log.i("정보태그", "자동로그인x")
//                    database.userDao().clearAllUsers()
//                }

        FirebaseApp.initializeApp(applicationContext) // firebase앱초기화
        /*
        구글 로그인 및 GoogleSinInClient객체 구성
        -> 앱에 필요한 사용자 데이터를 요청하도록 Google로그인을 구성
         */
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // GoogleSignInOptions => 구글 로그인에 대한 옵션을 설정하는데 사용됨(사용자 데이터를 요청하고 앱에 필요한 권한을 설정)
            .requestIdToken(getString(R.string.default_web_client_id)) // 구글 인증을 위한 토큰 요청
            .requestEmail() // 이메일 정보도 요청하도록 설정함
            .build()
        // GoogleSignInClient 객체 구성 : 구글 로그인 클라이언트를 나타내는 객체(앱-구글 로그인 서비스사이의 통신을 관리)
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        // FirebaseAuth객체를 초기화
            // FirebaseAuth = Firebase인증 서비스를 사용하기 위한 객체(사용자 인증과 관련된 작업을 수행함)
        mAuth = FirebaseAuth.getInstance();


        // 로그인 버튼
        binding.signInButton.setOnClickListener {
            Log.i("정보태그", "(button)login_google")
            val intent = mGoogleSignInClient!!.signInIntent
                // mGoogleSignInClient 객체의 signInIntent를 사용하여 로그인 인텐트(intent)를 가져온다
                    // signInIntent = 구글 로그인 작업을 위한 인텐트 객체
            rl_google_login!!.launch(intent)
        }
        // 로그인 후 로직 : 사용자가 로그인 한 후 활동 -> 사용자에 대한 GoogleSingInAccount 개체를 가져올 수 있다
        rl_google_login = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    // GoogleSignInAccount객체는 사용자 이름과 같이 로그인 한 사용자에 대한 정보가 포함된다
                try {
                    // GoogleSignInAccount 객체를 얻는다 -> 로그인 한 사용자에 대한 정보가 포함되어 있음
                    val account = task.getResult(ApiException::class.java)
                    /*
                    사용자가 정상적으로 로그인하면, GoogleSignInAccount객체에서 id토큰을
                    가져와서 Firebase사용자 인증 정보로 교환하고 해당 정볼르 사용해 firebase
                    에 인증한다
                     */
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    e.printStackTrace()
                    Log.i("정보태그", "Google sign in failed", e)
                    Toast.makeText(
                        applicationContext,
                        "Google sign in Failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }




    }

    override fun onStart() {
        super.onStart()




    }





    /*
    로그인 버튼 -> 아이디, 비밀번호 확인후
        - 맞으면 -> 다음 페이지로 이동
        - 틀리면 -> 다시 한 번 확인해주세요 알림
     */
    fun login(view: View) {
        // 이메일 유효성 확인
        binding.editEmail.takeIf { !am.chk_regex("email", it) }?.let {
            Toast.makeText(applicationContext, "이메일을 확인해주세요", Toast.LENGTH_SHORT).show()
            it.requestFocus()
            return
        }

        // 비밀번호 유효성 확인
        binding.editPw.takeIf { !am.chk_regex("pw", it) }?.let {
            Toast.makeText(applicationContext, "비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show()
            it.requestFocus()
            return
        }

        /*
        로그인 정보 있는지 확인
         */
        am.chk_login(
            actvity,
            binding.editEmail,
            binding.editPw,
            binding.chkAutologin.isChecked
        )

    }
    // 회원 가입 페이지 이동
    fun go_to_Signup_Page(view: View) {
        //val Intent = Intent(this,)
        val intent = Intent(this,Activity_Signup::class.java);
        startActivity(intent);
    }
    fun find_pw(view: View) {
        val intent = Intent(this,Activity_Find_Pw::class.java);
        startActivity(intent);
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount){
        Log.d("정보태그", "firebaseAuthWithGoogle:" + acct.id)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // signInWithCredential에 대한 호출이 성공하면,
                    // getCurrentUser메서드로 사용자의 계정 데이터를 가져 올 수 있다.
                    Log.i("정보태그", "signInWithCredential:success")

                    /*
                        사용자가 처음으로 로그인하면, 신규 사용자 계정이 사용되고
                        사용자가 인증 정보(사용자가 로그인 할 때 사용한 사용자 이름과 비밀번호,
                        전화번호 또는 인증제공업체 정보)에 연결된다
                        -> 이 신규계정은 firebase프로젝트에 저장되어,
                        사용자의 로그인 방법과 무관하고 프로젝트 내의 모든 앱에서 사용자 본인 확인에
                        사용 할 수 있다.
                         */
                    val user = mAuth!!.currentUser
                    // FirebaseUser객체에서 사용자의 기본 프로필을 가져 올 수 있다.

                    /*
                    사용자 프로필 가져오기
                     */
                    if (user != null) {
                        val profile_url = user.photoUrl.toString()
                        val login_value = user.uid
                        val login_email = user.email
                        Log.d("정보태그", "profile_url=$profile_url")
                        Log.d("정보태그", "login_value=$login_value")
                        Log.d("정보태그", "login_email=$login_email")

                        // Check if user's email is verified
                        val emailVerified = user.isEmailVerified
                        Log.d("정보태그", "emailVerified=$emailVerified")
                        if(emailVerified){
                            // 신규회원인지 이미 회원가입한 회원인지를 분기한다 -> 그 후 처리는 아래 함수에서
                            am.validate_new(actvity,applicationContext,user.uid, profile_url,
                                login_email!!
                            )
                        }else{
                            Toast.makeText(applicationContext, "Authentication Failed", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("정보태그", "signInWithCredential:failure", task.exception)
                    Toast.makeText(applicationContext, "Authentication Failed", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
}
