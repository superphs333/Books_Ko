package com.example.books_ko

import Data_Search_Book
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.books_ko.Class.AppHelper
import com.example.books_ko.databinding.ActivityPopUpInSearchBookBinding
import com.google.gson.JsonParser

class Activity_PopUp_in_Search_Book : AppCompatActivity() {

    private lateinit var binding: ActivityPopUpInSearchBookBinding
    lateinit var book : Data_Search_Book
    val appHelper = AppHelper();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPopUpInSearchBookBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 상태바 제거(전체화면 모드)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_DEFAULT)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        // spinner 셋팅
        val data = resources.getStringArray(R.array.read_status)
        val adapter = ArrayAdapter<String>(applicationContext,android.R.layout.simple_dropdown_item_1line,data)
        binding.categoryReadStatus.adapter = adapter

        // intent에서 온 값 받기
        book = intent.getParcelableExtra<Data_Search_Book>("book")!!
        Log.i("정보태그","book->$book")

        // 제목셋팅
        binding.txtTitle.text = book.title

    }

    /*
     해당 책을 마이북에 저장함
     */
    fun save_in_my_book_list(view: View?){
        /*
        읽은상태
         */
        var status = 0
        var selected = binding.categoryReadStatus.selectedItem.toString()
        status = when (why_change) {
            getString(R.string.read_bucket) -> 3 // 읽고싶은
            getString(R.string.read_reading) -> 1 // 읽는중
            else -> 2 // 읽음
        }
        Log.i("정보태그", "select status=>$status")

        /*
        도서저장
         */
        val url = applicationContext.getString(R.string.server_url) + "About_Book.php"
        val request: StringRequest = object : StringRequest(
            Method.POST,
            url,
            Response.Listener<String> { response ->

                // 정상 응답
                Log.i("정보태그", "(search_book)response=>$response")

                // 결과값 파싱
                val jsonParser = JsonParser()
                val jsonElement = jsonParser.parse(response)

                // 결과값
                val result = jsonElement.asJsonObject["result"].asString
                Log.i("정보태그", "result=>$result")

                if (result == "success") {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.save_in_my_book),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.toast_error),
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
        )
        {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                Log.i("정보태그", "")
                params["accept_sort"] = "Book_Add_in_Search"
                params["email"] = intent.getStringExtra("email") ?: ""
                params["from"] = "search"
                params["title"] = book.title
                params["authors"] = book.authors
                params["publisher"] = book.publisher
                params["isbn"] = book.isbn
                params["contents"] = book.contents
                params["thumbnail"] = book.thumbnail
                params["rating"] = binding.rating.rating.toString()
                params["status"] = status.toString()
                return params
            }
        }
        request.setShouldCache(false)
        appHelper.requestQueue = Volley.newRequestQueue(applicationContext)
        appHelper.requestQueue!!.add(request)


    }
}