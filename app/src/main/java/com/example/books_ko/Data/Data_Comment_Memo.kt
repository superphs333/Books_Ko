package com.example.books_ko.Data


data class Data_Comment_Memo(
    var idx_memo: Int,
    var idx: Int,
    var login_value: String,
    var nickname: String,
    var profile_url: String,
    var comment: String,
    var date_time: String,
    var group_idx: Int,
    var depth: Int,
    var target: String?,
    var visibility: Int
) {
    constructor(
        idx_memo: Int,
        idx: Int,
        login_value: String,
        nickname: String,
        profile_url: String,
        comment: String,
        date_time: String,
        group_idx: Int,
        depth: Int,
        visibility: Int
    ) : this(idx_memo, idx, login_value, nickname, profile_url, comment, date_time, group_idx, depth, null, visibility)
}
