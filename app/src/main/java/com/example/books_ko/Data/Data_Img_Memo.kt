package com.example.books_ko.Data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Data_Img_Memo(
    var img: String = "",
) : Parcelable
