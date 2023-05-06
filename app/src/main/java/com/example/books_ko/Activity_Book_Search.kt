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
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: AdapterSearchBook
    private var bookList: ArrayList<Data_Search_Book> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        linearLayoutManager = LinearLayoutManager(this)
        adapter = AdapterSearchBook(bookList, applicationContext, this)

        binding.rvBooks.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = this@Activity_Book_Search.adapter
        }
    }

    fun search_book(view: View?) {
        bookList.clear()
        adapter.notifyDataSetChanged()

        val url = "https://dapi.kakao.com" + "/v3/search/book?query=" + binding.editSearch.text.toString()
        val request: StringRequest = object : StringRequest(
            Method.GET,
            url,
            Response.Listener<String> { response ->

                // 정상 응답
                Log.i("정보태그", "(search_book)response=>$response")

                val jsonObject = JSONObject(response)
                val documents = jsonObject.getJSONArray("documents")

                for (i in 0 until documents.length()) {
                    val tempObject = documents.getJSONObject(i)

                    /*
                    가져 올 정보 : 작가, contents, isbn
                     */
                    val authors =
                        tempObject.getString("authors").replace("\\[".toRegex(), "")
                            .replace("\\]".toRegex(), "").replace("\"".toRegex(), "")
                    val contents = tempObject.getString("contents")
                    val isbn = if (tempObject.getString("isbn").length == 10) tempObject.getString("isbn") else tempObject.getString("isbn")
                    val publisher = tempObject.getString("publisher")
                    val thumbnail = tempObject.getString("thumbnail")
                    val title = tempObject.getString("title")
                    val url = tempObject.getString("url")
                    Log.i("정보태그","url->"+url)


                    bookList.add(Data_Search_Book(isbn,title, authors, publisher, thumbnail, contents, "Kakao",isbn, url))
                }
                Log.i("정보태그", "도서 담기 완료, arrayList->$bookList")
                adapter.notifyDataSetChanged()

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