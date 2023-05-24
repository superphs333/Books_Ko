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
import com.example.books_ko.Data.DataChatting
import com.example.books_ko.Data.DataMyBook
import com.example.books_ko.Data.Data_Book_Memo
import com.example.books_ko.Data.Data_Comment_Memo
import com.example.books_ko.DataBase.UserDatabase
import com.example.books_ko.Interface.JsonPlaceHolderApi
import com.example.books_ko.R
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
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

