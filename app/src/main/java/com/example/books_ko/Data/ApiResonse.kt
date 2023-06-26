package com.example.books_ko.Data

data class ApiResponse<T>(
    // <T> :  서버 응답에서 실제 데이터를 나타내는 클래스
    // ex) 서버응답이 JSON형식으로 반환되는 경우 -> T : JSON을 kOTLIN객체로 변환한 데이터 클래스
    val status: String,
    val message: String,
    val data: T?
)

