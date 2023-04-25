package com.example.books_ko.Function

import android.util.Log
import android.widget.EditText
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.books_ko.R
import com.example.books_ko.Class.AppHelper

object AboutMember{

    val appHelper = AppHelper();

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
                //val jsonParser = JsonParser()
                //val jsonElement: JsonElement = jsonParser.parse(response)

                // 결과값
                //val result: String = jsonElement.getAsJsonObject().get("result").getAsString()
                //Log.i("정보태그","result=>"+result);


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
                Log.d("정보태그","error=>"+error.getMessage());
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



}