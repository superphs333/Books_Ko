package com.example.books_ko.Interface

import com.example.books_ko.ApiResponse
import com.example.books_ko.Data.ApiData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
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

    @Multipart
    @POST("About_Member.php")
    fun sendDataToChangeProfileUrl(
        @Part("accept_sort") accept_sort: RequestBody,
        @Part("email") email: RequestBody,
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

    // 메모저장
    @Multipart //멀티파트 요청을 사용하여 데이터를 전송(멀티파트 요청 = 여러 부분으로 구성된 메세지, 텍스트 및 바이너리 데이터를 함께 전송 할 수 있음)
    @POST("About_Memo.php")
    fun sendDatatoMemo(
        @Part("book_idx") book_idx: RequestBody,
        @Part("accept_sort") accept_sort: RequestBody,
        @Part("size") size: RequestBody,
        @Part("memo") memo: RequestBody,
        @Part("page") page: RequestBody,
        @Part("open") open: RequestBody,
        @Part("email") email: RequestBody,
        @Part("memo_idx") memo_idx: RequestBody,
        @Part images: List<MultipartBody.Part>,
        @Part("imgOrderJoined") imgOrderJoined: RequestBody
    ): retrofit2.Call<ApiResponse<ApiData>>

    /*
    메모리스트
        - accept_sort => case 나누기 위해
        - requester => 요청자(email)
        - book_idx => 책고유값
        - view => 상태(전체, 팔로우, 내메모)
     */
    @FormUrlEncoded
    @POST("About_Memo.php")
    open fun Get_Data_Book_Memos(
        @Field("accept_sort") accept_sort: String,
        @Field("email") email: String?,
        @Field("book_idx") book_idx: Int?,
        @Field("view") view: Int?
    ): retrofit2.Call<ApiResponse<ApiData>>

    /*
    메모 댓글 리스트
     */
    @FormUrlEncoded
    @POST("About_Memo.php")
    open fun Get_Data_Book_Memo_Comments(
        @Field("accept_sort") accept_sort: String,
        @Field("idx_memo") idx_memo: Int?,
        @Field("email") email: String?,
        @Field("view") view: Int? // 0(메모의 댓글), 1(내 댓글)
    ): retrofit2.Call<ApiResponse<ApiData>>


    /*
    채팅방 리스트
     */
    @FormUrlEncoded
    @POST("About_Chatting.php")
    open fun Get_Chatting_Room_Data(
        @Field("accept_sort") accept_sort: String?,
        @Field("email")   email : String ,
        @Field("inputStatus") inputStatus : Int
    ): retrofit2.Call<ApiResponse<ApiData>>

    /*
    채팅방 참여자 리스트
     */
    @FormUrlEncoded
    @POST("About_Chatting.php")
    open fun Get_join_chatting_room_people(
        @Field("accept_sort") accept_sort: String,
        @Field("room_idx")   room_idx : Int ,
        @Field("email") email : String
    ): retrofit2.Call<ApiResponse<ApiData>>

}