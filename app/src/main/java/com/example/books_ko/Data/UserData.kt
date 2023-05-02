package com.example.books_ko.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserData(
    @PrimaryKey val email: String,
    @ColumnInfo(name = "nickname") var nickname: String,
    @ColumnInfo(name = "platform_type") var platform_type: String,
    @ColumnInfo(name = "auto_login") var auto_login: Boolean,
    @ColumnInfo(name = "profile_url") var profile_url: String,
)