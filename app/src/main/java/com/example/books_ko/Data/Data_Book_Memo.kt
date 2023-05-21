package com.example.books_ko.Data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Data_Book_Memo(
    var idx: Int,
    var page: Int,
    var countHeart: Int,
    var countComment: Int,
    var email: String,
    var nickname: String,
    var profileUrl: String,
    var bookIdx: Int,
    var title: String,
    var dateTime: String,
    var imgUrls: String,
    var memo: String,
    var open: String,
    var thumbnail: String,
    var checkHeart: Int,
    var follow: Int
) : Parcelable {
    constructor() : this(
        idx = 0,
        page = 0,
        countHeart = 0,
        countComment = 0,
        email = "",
        nickname = "",
        profileUrl = "",
        bookIdx = 0,
        title = "",
        dateTime = "",
        imgUrls = "",
        memo = "",
        open = "",
        thumbnail = "",
        checkHeart = 0,
        follow = 0
    )
}
