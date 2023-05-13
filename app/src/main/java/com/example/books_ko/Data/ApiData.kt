package com.example.books_ko.Data

data class ApiData (
    val result: String,
    val sql_success: String,
    val uploadedfile_error: String? = null,
    val destination: String? = null,
    val tmp_name: String? = null,
    val move: String? = null,
    val pw: String? = null,
    val sql_insert: String? = null,
    val sql: String? = null,
    val bookList:ArrayList<DataMyBook>,
    val chattingRoomList:ArrayList<Data_Chatting_Room>,
    val dataJoinPeopleList:ArrayList<Data_Join_People>,
        )

