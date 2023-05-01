package com.example.books_ko.Function

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.room.Room
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.books_ko.Activity_Main2
import com.example.books_ko.Class.AppHelper
import com.example.books_ko.Data.UserData
import com.example.books_ko.DataBase.UserDatabase
import com.example.books_ko.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object AboutMember{

    val appHelper = AppHelper();
    private lateinit var database : UserDatabase


    // 생성자
    init{

    }


    // Interface
    open interface VolleyCallback {
        fun onSuccess(result: Boolean)
    }

    /*
    함수
     */
    // 정규식
    open fun chk_regex(sort: String?, input_email: EditText?): Boolean{
        val text = input_email!!.text.toString()
        var regex ="";
        Log.i("정보태그", "분류=>$sort, text=>$text")
        // sort에 따라 정규식 분류
        when (sort) {
            "email" -> regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$"
            "pw" ->                     // 대소문자 구분 숫자 특수문자 조합 9~12자리
                regex = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&])[A-Za-z[0-9]$@$!%*#?&]{9,12}$"

            "nickname" ->                     // 영문, 숫자로만 이루어진 4~12자리 문자
                regex = "^[a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힣|0-9]{1}[a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힣|0-9_]{3,11}$"
        }
        var check = Pattern.matches(regex,text)
        Log.i("정보태그", "정규식결과(chk_regex)=>$check")
        return check;
    }

    // 닉네임, 이메일 중복 체크
    open fun chk_double(sort: String?, input: EditText?, callback: VolleyCallback?){
        Log.i("정보태그", "function chk_double")
        val chk_double_result = booleanArrayOf(false)

        // 웹페이지 실행하기
        val url = input?.context?.getString(R.string.server_url)+"About_Member.php";
        val request: StringRequest = object : StringRequest(
            Method.POST,
            url,
            Response.Listener<String> { response ->

                // 정상 응답
                Log.i("정보태그", "(chk_double)response=>$response")

                // 결과값 파싱
                val jsonParser = JsonParser()
                val jsonElement: JsonElement = jsonParser.parse(response)

                // 결과값
                val result: String = jsonElement.getAsJsonObject().get("result").getAsString()
                Log.i("정보태그","result=>"+result);

                if(result.equals("yes")){ // 중복인 경우
                    // 닉네임 vs 이메일 분기
                    when(sort){
                        "nickname" ->  {
                            Toast.makeText(
                                input?.context,
                                input?.context?.getString(R.string.toast_nickname_double),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                         "email" -> {
                             // 이메일
                             Toast.makeText(
                                 input?.context,
                                 input?.context?.getString(R.string.toast_email_double),
                                 Toast.LENGTH_SHORT
                             ).show()
                         }
                    }
                    // 포커스 두기
                    input?.requestFocus();
                    chk_double_result[0] = true
                }else{ // 중복이 아닌 경우
                    chk_double_result[0] = false
                }

                // 콜백에 결과값 전송
                callback!!.onSuccess(chk_double_result[0])
            }, // end onResponse
            Response.ErrorListener { error ->
                // 에러 발생
                val networkResponse = error.networkResponse
                if (networkResponse != null && networkResponse.data != null) {
                    val jsonError = String(networkResponse.data)
                    Log.d("정보태그", "onErrorResponse: $jsonError")
                }
            }
        ) {
            // Post 방식으로 body에 요청 파라미터를 넣어 전달하고 싶을 경우
            // 만약 헤더를 한 줄 추가하고 싶다면 getHeaders() override
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                Log.i("정보태그", "")
                params["accept_sort"] = "chk_double"
                params["sort"] = sort!! // nickname or email
                params["input"] = input!!.text.toString() // 입력값
                return params
            }
        }

        request.setShouldCache(false)
        appHelper.requestQueue = Volley.newRequestQueue(input?.context)
        appHelper.requestQueue!!.add(request)
    }

    open fun chk_login(activity: LifecycleOwner, email: EditText?, pw: EditText?, auto_login: Boolean){
        // 웹페이지 실행하기
        val url = email?.context?.getString(R.string.server_url)+"About_Member.php";
        val request: StringRequest = object : StringRequest(
            Method.POST,
            url,
            Response.Listener<String> { response ->

                // 정상 응답
                Log.i("정보태그", "(chk_double)response=>$response")

                // 결과값 파싱
                val jsonParser = JsonParser()
                val jsonElement: JsonElement = jsonParser.parse(response)

                // 결과값
                val result: String = jsonElement.getAsJsonObject().get("result").getAsString()
                Log.i("정보태그","result=>"+result);

                if(result.equals("yes")){ // 로그인 정보 존재
                    Toast.makeText(
                        email?.context,
                        email?.context?.getString(R.string.login_ok),
                        Toast.LENGTH_SHORT
                    ).show()

                    // Room인스턴스 생성
                    database = Room.databaseBuilder(email!!.context, UserDatabase::class.java, "app_database").build()
                    // 회원정보 가져오기, 정보 저장
                    var nickname=""
                    var profile_url="";
                    CoroutineScope(Dispatchers.IO).launch {
                        var tmp_nickname = getMemberInfo(email!!.context,email!!.text.toString(),"nickname")
                        var tmp_profile_url =  getMemberInfo(email!!.context,email!!.text.toString(),"profile_url")
                        nickname = tmp_nickname
                        profile_url = tmp_profile_url
                    }
                    // 정보저장
                    database.userDao().getUserByEmail(email!!.text.toString()).observe(activity, Observer { existingUser ->
                        if (existingUser == null) {
                            Log.i("정보태그","existingUser  null")
                            val user = UserData(email!!.text.toString(), nickname, "normal",auto_login,profile_url)
                            database.userDao().insertUser(user)
                        }else{
                            Log.i("정보태그","existingUser  존재")

                        }
                    })

                    // 메인 페이지로 이동
                    email?.context?.let {
                        val intent = Intent(it, Activity_Main2::class.java)
                        it.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        if (it is Activity) {
                            it.finish()
                        }
                    }
                }else{
                    Toast.makeText(
                        email?.context,
                        email?.context?.getString(R.string.no_login_info),
                        Toast.LENGTH_SHORT
                    ).show()
                }


            }, // end onResponse
            Response.ErrorListener { error ->
                // 에러 발생
                val networkResponse = error.networkResponse
                if (networkResponse != null && networkResponse.data != null) {
                    val jsonError = String(networkResponse.data)
                    Log.d("정보태그", "onErrorResponse: $jsonError")
                }
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                Log.i("정보태그", "")
                params["accept_sort"] = "login"
                params["email"] = email!!.text.toString() // nickname or email
                params["pw"] = pw!!.text.toString() // nickname or email
                return params
            }
        }

        request.setShouldCache(false)
        appHelper.requestQueue = Volley.newRequestQueue(email?.context)
        appHelper.requestQueue!!.add(request)
    }

    // 가져오고 싶은 정보를 받고 해당 정보를 return해줌
    private suspend fun getMemberInfo(context: Context, email: String, to_get: String): String {
        val url = context.getString(R.string.server_url) + "About_Member.php"
        var result = ""

        val response = suspendCoroutine<String> { continuation ->
            val request = object : StringRequest(
                Method.POST,
                url,
                Response.Listener<String> { response ->
                    Log.i("정보태그", "(chk_double)response=>$response")

                    val jsonParser = JsonParser()
                    val jsonElement: JsonElement = jsonParser.parse(response)

                    result = jsonElement.getAsJsonObject().get("result").getAsString()
                    Log.i("정보태그", "통신결과=>$result")

                    if (result == "success") {
                        val row = jsonElement.asJsonObject["row"]
                        val gson = Gson()
                        val map = gson.fromJson<Map<*, *>>(
                            row.toString(),
                            MutableMap::class.java
                        )
                        Log.i("정보태그", "[Get_member_info]row=>" + map[to_get])
                        result = map[to_get].toString()

                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.toast_error),
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                    continuation.resume(response)
                },
                Response.ErrorListener { error ->
                    val networkResponse = error.networkResponse
                    if (networkResponse != null && networkResponse.data != null) {
                        val jsonError = String(networkResponse.data)
                        Log.d("정보태그", "onErrorResponse: $jsonError")
                    }
                    continuation.resumeWithException(error)
                }) {
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String> {
                    val params: MutableMap<String, String> = HashMap()
                    Log.i("정보태그", "")
                    params["accept_sort"] = "Get_member_info"
                    params["email"] = email
                    params["to_get"] = to_get
                    return params
                }
            }

            request.setShouldCache(false)
            appHelper.requestQueue = Volley.newRequestQueue(context)
            appHelper.requestQueue!!.add(request)
        }

        return result
    }



}