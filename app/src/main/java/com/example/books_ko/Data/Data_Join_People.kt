package com.example.books_ko.Data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Data_Join_People (
    var follow: Boolean,
    var nickname: String,
    var profileUrl: String,
    var email: String
): Parcelable