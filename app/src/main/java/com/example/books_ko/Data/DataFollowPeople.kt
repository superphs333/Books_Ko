package com.example.books_ko.Data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class DataFollowPeople(
    var email: String,
    var nickname: String,
    var profile_url: String,
    var visible: Boolean
) : Parcelable
