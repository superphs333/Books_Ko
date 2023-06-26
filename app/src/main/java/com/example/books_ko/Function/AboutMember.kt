package com.example.books_ko.Function

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.books_ko.Activity_Main2
import com.example.books_ko.Activity_Set_nickname
import com.example.books_ko.Class.AppHelper
import com.example.books_ko.Data.UserData
import com.example.books_ko.DataBase.UserDatabase
import com.example.books_ko.MainActivity
import com.example.books_ko.R
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    open fun chk_regex(sort: String?, inputText: EditText?): Boolean{
        val text = inputText!!.text.toString()
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

    // 이메일 가져오기
    suspend fun getEmailFromRoom(context: Context): String = withContext(Dispatchers.IO) {
        val database = Room.databaseBuilder(context, UserDatabase::class.java, "app_database").build()
        return@withContext database.userDao().getUser2().email
    }

    // 닉네임, 이메일 중복 체크
    open fun chkDouble(sort: String?, input: EditText?, callback: VolleyCallback?, from : String?=""){
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
                             if(from==""){
                                 // 이메일
                                 Toast.makeText(
                                     input?.context,
                                     input?.context?.getString(R.string.toast_email_double),
                                     Toast.LENGTH_SHORT
                                 ).show()
                             }
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

    open fun chkLogin(email: EditText?, pw: EditText?, auto_login: Boolean){
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

                /*
                개선 : FunctionCollection에 있는 함수로 처리하기
                 */

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
                    GlobalScope.launch(Dispatchers.IO) {
                        database.userDao().clearAllUsers()
                        val user = UserData(email!!.text.toString(), nickname, "normal",auto_login,profile_url)
                        database.userDao().insertUser(user)
                    }




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
                params["email"] = email!!.text.toString()
                params["pw"] = pw!!.text.toString()
                return params
            }
        }

        request.setShouldCache(false)
        appHelper.requestQueue = Volley.newRequestQueue(email?.context)
        appHelper.requestQueue!!.add(request)
    }

    // 가져오고 싶은 정보를 받고 해당 정보를 return해줌
     suspend fun getMemberInfo(context: Context, email: String, to_get: String): String {
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

    /*
    구글 - 신규회원인지, 기존회원인지 분류
     */
    fun validateNew(context : Context, sns_id: String, profile_url: String, login_email: String) {
        val url = context.getString(R.string.server_url)+"About_Member.php";
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

                if(result.equals("yes")){ // 존재회원(구글-로그아웃 했다가, 다시 로그인)
                    Log.i("정보태그","구글로그인-기존회원")
                    // Room에 회원 정보 저장
                    val user = UserData(login_email, "", "google",true,profile_url)
                    database = Room.databaseBuilder(context, UserDatabase::class.java, "app_database").build()
                    CoroutineScope(Dispatchers.IO).launch {
                        database.userDao().clearAllUsers()
                        user.nickname = getMemberInfo(context,login_email,"nickname")
                        database.userDao().insertUser(user)
                    }
                    // Main페이지로 이동
                    context?.let {
                        val intent = Intent(it, Activity_Main2::class.java)
                        it.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        if (it is Activity) {
                            it.finish()
                        }
                    }

                }else{ // 신규회원 -> 닉네임 설정 액티비티로 이동
                    Log.i("정보태그","구글로그인-신규회원")
                    // 신규회원
                    // 닉네임 설정 액티비티로 이동
                    context?.let {
                        val intent = Intent(it, Activity_Set_nickname::class.java)
                        intent.putExtra("profile_url", profile_url)
                        intent.putExtra("sns_id", sns_id)
                        intent.putExtra("login_email", login_email)
                        intent.putExtra("why_change", "signup") // 닉네임 변경 목적
                        it.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        if (it is Activity) {
                            it.finish()
                        }
                    }
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
                params["accept_sort"] = "validate_new"
                params["sns_id"] = sns_id
                return params
            }
        }

        request.setShouldCache(false)
        appHelper.requestQueue = Volley.newRequestQueue(context)
        appHelper.requestQueue!!.add(request)
    }

     fun google_sign_up(context: Context,email: String, sns_id: String, nickname: String, profile_url: String) {
        val url = context.getString(R.string.server_url)+"About_Member.php";
        val request: StringRequest = object : StringRequest(
            Method.POST,
            url,
            Response.Listener<String> { response ->

                // 정상 응답
                Log.i("정보태그", "(google_sign_up)response=>$response")

                // 결과값 파싱
                val jsonParser = JsonParser()
                val jsonElement: JsonElement = jsonParser.parse(response)

                // 결과값
                val result: String = jsonElement.getAsJsonObject().get("result").getAsString()
                Log.i("정보태그","result=>"+result);

                if (result == "success") {
                    Toast.makeText(
                        context, "회원가입이 완료되었습니다", Toast.LENGTH_LONG
                    ).show()

                    // 회원정보 저장
                    val user = UserData(email, nickname, "google",true,profile_url)
                    database = Room.databaseBuilder(context, UserDatabase::class.java, "app_database").build()
                    GlobalScope.launch(Dispatchers.IO) {
                        database.userDao().clearAllUsers()
                        database.userDao().insertUser(user)
                    }

                    // 페이지 이동
                    context?.let {
                        val intent = Intent(it, Activity_Main2::class.java)
                        it.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        if (it is Activity) {
                            it.finish()
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_error),
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
                params["accept_sort"] = "sign_up_google"
                params["sns_id"] = sns_id
                params["email"] = email
                params["nickname"] = nickname
                params["profile_url"] = profile_url

                return params
            }
        }

        request.setShouldCache(false)
        appHelper.requestQueue = Volley.newRequestQueue(context)
        appHelper.requestQueue!!.add(request)
    }

   fun changeMemberInfo(context: Context, activity:LifecycleOwner, sort: String, input: String, email: String) {
       val url = context.getString(R.string.server_url)+"About_Member.php";
       val request: StringRequest = object : StringRequest(
           Method.POST,
           url,
           Response.Listener<String> { response ->

               // 정상 응답
               Log.i("정보태그", "(Change_Member_Info)response=>$response")

               // 결과값 파싱
               val jsonParser = JsonParser()
               val jsonElement: JsonElement = jsonParser.parse(response)

               // 결과값
               val result: String = jsonElement.getAsJsonObject().get("result").getAsString()
               Log.i("정보태그","(result=>"+result);

               if(result == "success"){
                   database = Room.databaseBuilder(context, UserDatabase::class.java, "app_database").build()
                    // 정보 Room에 업데이트
                   if(sort=="nickname"){
                       CoroutineScope(Dispatchers.IO).launch {
                           database.userDao().updateUserNickName(email, input)
                       }
                   }

                   /*
                   페이지 이동 분기 -> Room에 저장 여부에 따라
                    - 저장되어 있지 않은 경우(비밀번호찾기->비밀번호변경)
                    */
                   val userLiveData = database.userDao().getUser()
                    // LiveData를 사용하여 데이터 변경 시 UI 업데이트
                   userLiveData.observe(activity, Observer { userData ->
                        if(userData != null){  // 나머지 => 해당 액티비티만 finish
                            if(sort != "sender_id"){
                                activity.lifecycleScope.launch {
                                    (activity as Activity).finish()
                                }
                            }
                        }else{ // 비밀번호 찾기->비밀번호 변경으로 온 경우 => 메인페이지 이동
                            context?.let {
                                val intent = Intent(it, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                it.startActivity(intent)
                                if (it is Activity) {
                                    it.finish()
                                }
                            }
                        }
                   })


               }else{
                   Toast.makeText(
                       context,
                       context.getString(R.string.toast_error),
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
               params["accept_sort"] = "Change_Member_Info"
               params["email"] = email
               params["sort"] = sort
               when (sort) {
                   "pw" -> params["pw"] = input
                   "nickname" -> params["nickname"] = input
                   "sender_id" -> params["sender_id"] = input
               }
               return params
           }
       }

       request.setShouldCache(false)
       appHelper.requestQueue = Volley.newRequestQueue(context)
       appHelper.requestQueue!!.add(request)
    }

    private suspend fun GetMemberAllInfo(context: Context, email:String):Map<String,String>{
        val url = context.getString(R.string.server_url) + "About_Member.php"
        var result = ""
        val memberInfo = mutableMapOf<String, String>()

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
                        Log.i("정보태그", "[Get_member_info]row=>" + map["nickname"])
                        memberInfo["nickname"] = map["nickname"].toString()
                        memberInfo["snsid"] = map["snsid"].toString()
                        memberInfo["profile_url"] = map["profile_url"].toString()
                        memberInfo["sender_id"] = map["sender_id"].toString()


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
                    return params
                }
            }

            request.setShouldCache(false)
            appHelper.requestQueue = Volley.newRequestQueue(context)
            appHelper.requestQueue!!.add(request)
        }

        return memberInfo
    }



}