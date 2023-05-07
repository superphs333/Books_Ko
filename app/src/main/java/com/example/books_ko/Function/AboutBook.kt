package com.example.books_ko.Function

import Data_Search_Book
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.room.Room
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.books_ko.Activity_PopUp_in_Search_Book
import com.example.books_ko.ApiResponse
import com.example.books_ko.Class.AppHelper
import com.example.books_ko.Data.ApiData
import com.example.books_ko.Data.DataMyBook
import com.example.books_ko.DataBase.UserDatabase
import com.example.books_ko.Interface.JsonPlaceHolderApi
import com.example.books_ko.R
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object AboutBook {
    val appHelper = AppHelper();
    private lateinit var database: UserDatabase


    /*
    이미 등록되어 있는 책인지 확인한다 -> 등록되어 있지 않은 경우 내 도서에 저장
     */
    fun Check_in_mybook(
        context: Context,
        activity: LifecycleOwner,
        isbn: String,
        DSB: Data_Search_Book
    ) {
        database = Room.databaseBuilder(context, UserDatabase::class.java, "app_database").build()

        // 1. 코루틴 스코프 생성
        CoroutineScope(Dispatchers.Main).launch {
            // 2. 백그라운드 스레드에서 데이터베이스 쿼리 실행
            val userData = withContext(Dispatchers.IO) {
                database.userDao().getUser2()
            }

            val email = userData.email
            Log.i("정보태그", "email->$email")

            // 웹페이지 실행하기
            val url = context.getString(R.string.server_url) + "About_Book.php"
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
                    Log.i("정보태그", "result=>" + result);

                    if (result.equals("yes")) {  // 이미 존재하는 도서
                        // 이미 존재하는 도서
                        Toast.makeText(
                            context,
                            context.getString(R.string.before_add_book),
                            Toast.LENGTH_LONG
                        ).show()
                    } else { // 등록하지 않은 도서 -> 등록
                        val intent = Intent(context, Activity_PopUp_in_Search_Book::class.java)
                        intent.putExtra("book", DSB)
                        intent.putExtra("email", email)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
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
                // Post 방식으로 body에 요청 파라미터를 넣어 전달하고 싶을 경우
                // 만약 헤더를 한 줄 추가하고 싶다면 getHeaders() override
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String>? {
                    val params: MutableMap<String, String> = HashMap()
                    Log.i("정보태그", "")
                    params["accept_sort"] = "Check_in_mybook"
                    params["email"] = email
                    params["isbn"] = isbn
                    return params
                }
            }

            request.setShouldCache(false)
            AboutMember.appHelper.requestQueue = Volley.newRequestQueue(context)
            AboutMember.appHelper.requestQueue!!.add(request)
        }
    }

    suspend fun getMyBook(
        context: Context,
        email: String,
        Inputstatus: Int,
        search: String
    ): ArrayList<DataMyBook>? = withContext(Dispatchers.IO) {
        val retrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.server_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val myApi = retrofit.create(JsonPlaceHolderApi::class.java)
        val accept_sort = "My_Books"

        try {
            val response = myApi.getMyBook(accept_sort, email, Inputstatus, search).execute()
            if (response.isSuccessful) {
                val result = response.body()
                if (result?.status == "success"){
                    response.body()?.data?.bookList as ArrayList<DataMyBook>?
                } else {
                    Log.i("정보태그", "[getMyBook]서버에 연결은 되었으나 오류발생")
                    null
                }
            } else {
                Log.i("정보태그", "[getMyBook]result.staus isSuccessful X")
                null
            }
        } catch (e: IOException) {
            null
        }
    }
}

