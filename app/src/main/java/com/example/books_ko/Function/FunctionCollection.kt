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
import com.google.gson.JsonObject
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
            "Change_Member_Info","withdrawal" -> "About_Member.php"
            "following","ManagementFollow" -> "About_Follow.php"
            "Update_heart_check","Management_Comment","Delete_Book_Memo"-> "About_Memo.php"
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
                val result: String = jsonElement.getAsJsonObject().get("status").getAsString()
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

    // 서버에서 가져온 결과값이 필요한 경우
    suspend fun goServerForResult(context: Context, accept_sort: String, map: MutableMap<String, String>): Map<String, Any> = suspendCoroutine { continuation ->
        // suspendCoroutine -> 코루틴을 일시정지
        /*
        웹페이지 주소
         */
        val destination = when (accept_sort) {
            "edit_my_book", "delete_my_book" -> "About_Book.php"
            "save_chatting_room", "delete_rom", "edit_chatting_room", "get_chatting_room_info", "out_room", "join_room", "out_join_room", "get_chatting", "alarm_for_chatting" -> "About_Chatting.php"
            "Change_Member_Info","withdrawal" -> "About_Member.php"
            "following","ManagementFollow" -> "About_Follow.php"
            "Update_heart_check","Management_Comment","Delete_Book_Memo","Get_Memo_One"-> "About_Memo.php"
            else -> ""
        }

        val url: String = context.getString(R.string.server_url) + destination
        val request: StringRequest = object : StringRequest(
            Method.POST,
            url,
            Response.Listener<String> { response ->
                // 정상 응답
                Log.i("정보태그", "($accept_sort-go_server)response=>$response")
                /*
                ex)
                {
                    "status": "success",
                    "message": "요청이 성공적으로 처리되었습니다.",
                    "data": {
                        "accept_sort": "get_chatting_room_info",
                        "idx": "29",
                        "query": "SELECT cr.title, cr.room_explain, cr.total_count, cr.idx, cr.leader, COUNT(cr.idx) as join_count FROM Chatting_Room as cr LEFT JOIN Join_Chatting_Room as jcr ON cr.idx=jcr.room_idx WHERE cr.idx=29 and jcr.status=1 GROUP BY cr.idx",
                        "result": "success",
                        "room_info": {
                            "title": "333",
                            "room_explain": "3333",
                            "total_count": "333",
                            "idx": "29",
                            "leader": "leesotest@gmail.com",
                            "join_count": "1"
                        }
                    }
                }
                 */

                // 결과값 파싱
                val jsonParser = JsonParser()
                val jsonElement: JsonElement = jsonParser.parse(response) // JSON 문자열을 파싱하여 JsonElement로 변환

                // 결과값
                val resultObject: JsonObject = jsonElement.getAsJsonObject() // Element를 JsonObject로 변환
                val resultMap: MutableMap<String, Any> = HashMap() // 파싱 결과 저장용

                for ((key, value) in resultObject.entrySet()) {
                    if (key == "data") { // key: data인 경우
                        val dataJson = value as JsonObject // value를 JsonObject로 캐스팅
                        val dataMap: MutableMap<String, Any> = HashMap() // data" 하위 객체의 파싱 결과를 저장하기 위한 컨테이너

                        for ((dataKey, dataValue) in dataJson.entrySet()) {
                            val parsedValue = when {
                                dataValue.isJsonObject() -> { // JSON 객체인 경우
                                    val jsonObject = dataValue as JsonObject
                                    val jsonObjectMap: MutableMap<String, String> = HashMap()

                                    for ((objKey, objValue) in jsonObject.entrySet()) {
                                        jsonObjectMap[objKey] = objValue.asString
                                    }
                                    jsonObjectMap
                                }
                                else -> dataValue.asString // 그 외의 경우에는 문자열로 처리
                            }
                            dataMap[dataKey] = parsedValue
                        }
                        resultMap[key] = dataMap
                    } else { // data 외의 경우에는 key-value 저장
                        resultMap[key] = value.asString
                    }
                }


                /*
                사용법
                val status: String = resultMap["status"] as String
                val message: String = resultMap["message"] as String

                val data: Map<String, String> = resultMap["data"] as Map<String, String>
                val acceptSort: String = data["accept_sort"] as String
                val roomExplain: String = data["room_explain"] as String
                // 나머지 필요한 데이터를 찾아서 사용
                val roomIdx: Int = data["room_idx"]?.toIntOrNull() ?: 0 // Int로 변환하여 사용하거나 기본값 0 설정
                 */

                continuation.resume(resultMap)
            },
            Response.ErrorListener { error ->
                // 에러 발생
                val networkResponse = error.networkResponse
                if (networkResponse != null && networkResponse.data != null) {
                    val jsonError = String(networkResponse.data)
                    Log.d("정보태그", "onErrorResponse: $jsonError")
                }
                continuation.resume(emptyMap())
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["accept_sort"] = accept_sort
                for (key in map.keys) {
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