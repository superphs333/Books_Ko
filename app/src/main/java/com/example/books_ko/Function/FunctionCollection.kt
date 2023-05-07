package com.example.books_ko.Function

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.books_ko.Class.AppHelper
import com.example.books_ko.R
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object FunctionCollection {

    val appHelper = AppHelper()


    suspend fun goServer(context: Context, accept_sort: String, map: MutableMap<String, String>): Boolean = suspendCoroutine { continuation ->
            // suspendCoroutine -> 코루틴을 일시정지
        /*
        웹페이지 주소
         */
        val destination = when (accept_sort) {
            "edit_my_book", "delete_my_book" -> "About_Book.php"
            "save_chatting_room", "delete_rom", "edit_chatting_room", "get_chatting_room_info", "out_room", "join_room", "out_join_room", "get_chatting", "alarm_for_chatting" -> "About_Chatting.php"
            "Change_Member_Info" -> "About_Member.php"
            "following" -> "About_Follow.php"
            else -> ""
        }
        val url: String = context.getString(R.string.server_url) + destination
        val request: StringRequest = object : StringRequest(
            Method.POST,
            url,
            Response.Listener<String> { response ->

                // 정상 응답
                Log.i("정보태그", "(go_server)response=>$response")

                // 결과값 파싱
                val jsonParser = JsonParser()
                val jsonElement: JsonElement = jsonParser.parse(response)

                // 결과값
                val result: String = jsonElement.getAsJsonObject().get("result").getAsString()
                Log.i("정보태그","[goServer]result=>"+result);

                val isSuccess = (result == "success")
                continuation.resume(isSuccess)

            }, // end onResponse
            Response.ErrorListener { error ->
                // 에러 발생
                val networkResponse = error.networkResponse
                if (networkResponse != null && networkResponse.data != null) {
                    val jsonError = String(networkResponse.data)
                    Log.d("정보태그", "onErrorResponse: $jsonError")
                }
                continuation.resume(false)
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["accept_sort"] = accept_sort
                for (key in map!!.keys) {
                    params[key] = map[key].toString()
                }
                return params
            }
        }

        request.setShouldCache(false)
        appHelper.requestQueue = Volley.newRequestQueue(context)
        appHelper.requestQueue!!.add(request)

    }
}