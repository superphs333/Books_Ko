package com.example.books_ko.Data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Data_Book_Memo(
    val idx: Int,
    val page: Int,
    var countHeart: Int,
    val countComment: Int,
    val email: String,
    val nickname: String,
    val profileUrl: String,
    val bookIdx: Int,
    val title: String,
    val dateTime: String,
    val imgUrls: String,
    val memo: String,
    val open: String,
    val thumbnail: String,
    var checkHeart: Int,
    var follow: Int
) : Parcelable
