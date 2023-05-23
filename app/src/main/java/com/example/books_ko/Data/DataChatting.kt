package com.example.books_ko.Data

data class DataChatting(
    var idx: Int,
    var roomIdx: Int,
    var email: String,
    var nickname: String,
    var profileUrl: String,
    var sort: String,
    var content: String,
    var date: String,
    var orderTag: String
) {
    constructor(sort: String, content: String) : this(0, 0, "", "", "", sort, content, "", "")
}