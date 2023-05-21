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

object AboutMemo {
    val appHelper = AppHelper();
    private lateinit var database: UserDatabase
    var mode = ""


    suspend fun  getMemo(
        context: Context,
        email: String,
        book_idx: Int,
        view: Int,
        likeChk : Boolean
    ): ArrayList<Data_Book_Memo>? = withContext(Dispatchers.IO) {
        val retrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.server_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val myApi = retrofit.create(JsonPlaceHolderApi::class.java)
        val accept_sort = "Get_Data_Book_Memos"

        // 보내는 값 확인
        Log.d("정보태그", "context: $context, email: $email, book_idx: $book_idx, view: $view")


        try {
            val response = myApi.Get_Data_Book_Memos(accept_sort, email,book_idx,view,likeChk).execute()
            Log.i("정보태그",response.body()!!.data!!.memoList.toString())
            if (response.isSuccessful) {
                val result = response.body()
                if (result?.status == "success"){
                    response.body()?.data?.memoList as ArrayList<Data_Book_Memo>?
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

    suspend fun  getMemoComments(
        context: Context,
        email: String?,
        idx_memo: Int?,
        view: Int
    ): ArrayList<Data_Comment_Memo>? = withContext(Dispatchers.IO) {
        val retrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.server_url))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val myApi = retrofit.create(JsonPlaceHolderApi::class.java)
        val accept_sort = "Get_Comments"

        // 보내는 값 확인
        Log.d("정보태그", "email: $email, idx_memo: $idx_memo, view: $view")


        try {
            val response = myApi.Get_Data_Book_Memo_Comments(accept_sort,idx_memo,email,view).execute()
            Log.i("정보태그",response.body()!!.data!!.memoCommentList.toString())
            if (response.isSuccessful) {
                val result = response.body()
                if (result?.status == "success"){
                    response.body()?.data?.memoCommentList as ArrayList<Data_Comment_Memo>?
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

    suspend fun getOneMemo(
        context: Context,
        memo_idx: Int,
    ): Data_Book_Memo? {
        return CoroutineScope(Dispatchers.IO).async {
            val map: MutableMap<String, String> = HashMap()
            map["idx"] = memo_idx.toString()
            val goServerForResult = FunctionCollection.goServerForResult(context, "Get_Memo_One", map)
            if (goServerForResult["status"] == "success") {
                val data: Map<String, Any> = goServerForResult["data"] as Map<String, Any>
                val memoInfo : Map<String, String> = data["memoData"] as Map<String, String>
                var returnMemoData = Data_Book_Memo()
                returnMemoData.page = memoInfo["page"].toString().toInt()
                returnMemoData.memo = memoInfo["memo"] as String
                returnMemoData.open = memoInfo["open"] as String
                returnMemoData.imgUrls = memoInfo["img_urls"] as String
                returnMemoData
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.toast_error), Toast.LENGTH_LONG).show()
                }
                null
            }
        }.await()
    }

}

