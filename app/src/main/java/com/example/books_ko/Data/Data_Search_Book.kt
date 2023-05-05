package com.example.books_ko.Data

import java.io.Serializable

data class Data_Search_Book(
    var unique_book_value: String = "",
    var title: String = "",
    var authors: String = "",
    var publisher: String = "",
    var thumbnail: String = "",
    var contents: String = "",
    var from_: String = "",
    var isbn: String = "",
    var url: String = ""
) : Serializable