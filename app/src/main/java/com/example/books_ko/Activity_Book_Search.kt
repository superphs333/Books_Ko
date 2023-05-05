package com.example.books_ko


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.ActivityBookSearchBinding

class Activity_Book_Search : AppCompatActivity() {

    private lateinit var binding: ActivityBookSearchBinding

    // 도서
    //var arrayList: ArrayList<Data_Search_Book> = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    open fun search_book(view: View?){
        // arrayList초기화
       // arrayList.clear()

        val url =
            "https://dapi.kakao.com" + "/v3/search/book?query=" + binding.editSearch.text.toString()
        val request: StringRequest = object : StringRequest(
            Method.GET,
            url,
            Response.Listener<String> { response ->

                // 정상 응답
                Log.i("정보태그", "(search_book)response=>$response")

//                // 결과값 파싱
//                val jsonParser = JsonParser()
//                val jsonElement: JsonElement = jsonParser.parse(response)
//
//                // 결과값
//                val result: String = jsonElement.getAsJsonObject().get("result").getAsString()
//                Log.i("정보태그","result=>"+result);



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
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = getString(R.string.Authorization)
                return headers
            }
        }
        request.setShouldCache(false)
        AboutMember.appHelper.requestQueue = Volley.newRequestQueue(applicationContext)
        AboutMember.appHelper.requestQueue!!.add(request)
    }
}