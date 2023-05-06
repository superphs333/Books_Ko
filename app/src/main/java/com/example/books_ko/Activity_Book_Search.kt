package com.example.books_ko


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.books_ko.Adapter.AdapterSearchBook
import com.example.books_ko.Data.Data_Search_Book
import com.example.books_ko.Function.AboutMember
import com.example.books_ko.databinding.ActivityBookSearchBinding
import org.json.JSONObject

class Activity_Book_Search : AppCompatActivity() {

    private lateinit var binding: ActivityBookSearchBinding

    // 도서
    var arrayList: ArrayList<Data_Search_Book>? = null
        // 이렇게 초기화를 해주어야 arrayList에 객체가 담겨짐

    /*
    리사이클러뷰
     */
    var  mainAdapter: AdapterSearchBook? = null
    lateinit var linearLayoutManager:LinearLayoutManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        arrayList = ArrayList();


    }

    open fun search_book(view: View?){
        // arrayList초기화
        arrayList?.clear()

        val url =
            "https://dapi.kakao.com" + "/v3/search/book?query=" + binding.editSearch.text.toString()
        val request: StringRequest = object : StringRequest(
            Method.GET,
            url,
            Response.Listener<String> { response ->

                // 정상 응답
                Log.i("정보태그", "(search_book)response=>$response")

                val jsonObject = JSONObject(response)
                val TEMP = jsonObject.getJSONArray("documents")

                for (i in 0 until TEMP.length()) {
                    val tempDataSearchBook = Data_Search_Book()

                    val temp_object = TEMP.getJSONObject(i)
                    //Log.i("정보태그",temp_object.toString())

                    /*
                    가져 올 정보 : 작가, contents, isbn
                     */
                    // 작가
                    tempDataSearchBook.authors =
                        temp_object.getString("authors").replace("\\[".toRegex(), "")
                            .replace("\\]".toRegex(), "").replace("\"".toRegex(), "")
                    // contents
                    tempDataSearchBook.contents = temp_object.getString("contents")
                    // isbn
                    val string_array = temp_object.getString("isbn").split(" ".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    tempDataSearchBook.isbn = if (string_array[0].isBlank()) string_array[1] else string_array[0]  // 만약, 10자리 isbn이 없다면 13자리 isbn사용
                    // 그외 정보
                    tempDataSearchBook.publisher = temp_object.getString("publisher")
                    tempDataSearchBook.thumbnail = temp_object.getString("thumbnail")
                    tempDataSearchBook.title = temp_object.getString("title")
                    tempDataSearchBook.url = temp_object.getString("url")

                    // 리스트에 객체 넣기
                    arrayList?.add(tempDataSearchBook)
                    Log.i("정보태그", "tempDataSearchBook->$tempDataSearchBook")

                }
                Log.i("정보태그", "도서 담기 완료, arrayList->$arrayList")
                // 리사이클러뷰에 셋팅
                mainAdapter = AdapterSearchBook(arrayList!!,applicationContext,this@Activity_Book_Search)
                binding.rvBooks.adapter = mainAdapter
                linearLayoutManager = LinearLayoutManager(applicationContext)
                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                binding.rvBooks.layoutManager = linearLayoutManager



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