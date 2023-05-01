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

        )

