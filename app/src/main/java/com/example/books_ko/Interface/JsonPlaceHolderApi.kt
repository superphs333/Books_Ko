package com.example.books_ko.Interface

import com.example.books_ko.ApiResponse
import com.example.books_ko.Data.ApiData
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
}