package com.example.books_ko.Interface

import com.example.books_ko.ApiResponse
import com.example.books_ko.Data.ApiData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface JsonPlaceHolderApi {

    @Multipart //멀티파트 요청을 사용하여 데이터를 전송(멀티파트 요청 = 여러 부분으로 구성된 메세지, 텍스트 및 바이너리 데이터를 함께 전송 할 수 있음)
    @POST("About_Member.php") // 해당 경로로 데이터를 전송함(기본 URL뒤에 추가되어 최종 요청 URL이 생성됨)
    fun sendDatatoSignUp(
        @Part("accept_sort") accept_sort: RequestBody,
        @Part("email") email: RequestBody,
        @Part("pw") password: RequestBody,
        @Part("nickname") name: RequestBody,
        @Part profileImage: MultipartBody.Part
    ): retrofit2.Call<ApiResponse<ApiData>>

    @Multipart //멀티파트 요청을 사용하여 데이터를 전송(멀티파트 요청 = 여러 부분으로 구성된 메세지, 텍스트 및 바이너리 데이터를 함께 전송 할 수 있음)
    @POST("About_Book.php") // 해당 경로로 데이터를 전송함(기본 URL뒤에 추가되어 최종 요청 URL이 생성됨)
    fun sendDatatoBookAdd(
        @Part("idx") idx: RequestBody,
        @Part("accept_sort") accept_sort: RequestBody,
        @Part("title") title: RequestBody,
        @Part("authors") authors: RequestBody,
        @Part("publisher") publisher: RequestBody,
        @Part("isbn") isbn: RequestBody,
        @Part("total_page") total_page: RequestBody,
        @Part("contents") contents: RequestBody,
        @Part("email") email: RequestBody,
        @Part("rating") rating: RequestBody,
        @Part("status") status: RequestBody,
        @Part uploadedfile: MultipartBody.Part
    ): retrofit2.Call<ApiResponse<ApiData>>

    @FormUrlEncoded
    @POST("About_Book.php")
    fun getMyBook(
        @Field("accept_sort") accept_sort : String ,
        @Field("email") email : String ,
        @Field("status") status : Int ,
        @Field("search") search : String
    ): retrofit2.Call<ApiResponse<ApiData>>

}