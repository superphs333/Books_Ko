package com.example.books_ko.Data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Data_Chatting_Room(
    var title: String,
    var room_explain: String,
    var total_count: Int,
    var join_count: Int,
    var idx: Int,
    var leader: String
) : Parcelable
