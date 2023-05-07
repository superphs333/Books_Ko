package com.example.books_ko.Data

data class DataMyBook(
    var unique_book_value: String = "",
    var title: String = "",
    var authors: String = "",
    var publisher: String = "",
    var thumbnail: String = "",
    var contents: String = "",
    var from_: String = "",
    var isbn: String = "",
    var status: Int = 0,
    var rating: Float = 0.0f,
    var review: String = "",
    var idx: Int = 0
)