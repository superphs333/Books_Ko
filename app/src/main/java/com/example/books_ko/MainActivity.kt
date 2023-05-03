package com.example.books_ko

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // user데이터베이스에 있는 모든 정보 삭제
        database = Room.databaseBuilder(applicationContext, UserDatabase::class.java, "app_database").build()
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if(database.userDao().getUser2().auto_login){// 자동로그인 상태 -> 페이지 이동
                    Log.i("정보태그","자동로그인")
                    val intent = Intent(applicationContext, Activity_Main2::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Log.i("정보태그","자동로그인x")
                    database.userDao().clearAllUsers()
                }
            }
        }


        FirebaseApp.initializeApp(applicationContext) // firebase앱초기화
        /*
        구글 로그인 및 GoogleSinInClient객체 구성
        -> 앱에 필요한 사용자 데이터를 요청하도록 Google로그인을 구성
         */
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail() // 이메일도 사용
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth
        // 로그인 버튼
        binding.signInButton.setOnClickListener {
            Log.i("정보태그", "(button)login_google")
            val intent = mGoogleSignInClient!!.signInIntent
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

        /*
        자동로그인 여부
         */




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
