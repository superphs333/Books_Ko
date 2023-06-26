package com.example.books_ko.Function

import android.content.Context
import android.util.Log
import com.example.books_ko.Class.AppHelper
import com.example.books_ko.Data.DataChatting
import com.example.books_ko.DataBase.UserDatabase
import com.example.books_ko.Interface.JsonPlaceHolderApi
import com.example.books_ko.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object AboutChatting {
    val appHelper = AppHelper();
    private lateinit var database: UserDatabase
    var mode = ""


    suspend fun  getChattingDatas(
        context: Context,
        email: String,
        room_idx: Int,
        view: Int, // 0 : 채팅방,
    ): ArrayList<DataChatting>? = withContext(Dispatchers.IO) {
        val retrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.server_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val myApi = retrofit.create(JsonPlaceHolderApi::class.java)
        val accept_sort = "get_chatting"

        // 보내는 값 확인
        Log.d("정보태그", "email: $email, room_idx: $room_idx, view: $view")


        try {
            val response = myApi.Get_Chatting(accept_sort, email,room_idx,view).execute()
            Log.i("정보태그",response.body()!!.data!!.chattingList.toString())
            if (response.isSuccessful) {
                val result = response.body()
                if (result?.status == "success"){
                    response.body()?.data?.chattingList as ArrayList<DataChatting>?
                } else {
                    Log.i("정보태그", "[getMemo]서버에 연결은 되었으나 오류발생")
                    null
                }
            } else {
                Log.i("정보태그", "[getMemo]result.staus isSuccessful X")
                null
            }
        } catch (e: IOException) {
            null
        }
    }

}

