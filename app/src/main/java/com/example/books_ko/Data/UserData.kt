package com.example.books_ko.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// 엔티티 클래스
@Entity(tableName = "user")
    // 해당 클래스가 데이터베이스 테이블의 엔티티임을 나타내고, "user"라는 테이블 이름을 지정
data class UserData(
    @PrimaryKey val email: String, //  필드를 주요 키(Primary Key)로 지정
    @ColumnInfo(name = "nickname") var nickname: String, // nickname필드를 데이터베이스 테이블의 nickname열(column)에 매핑
        // 이 필드난 가변변수로 선언되어 데이터베이스에서 값을 수정할 수 있다
    @ColumnInfo(name = "platform_type") var platform_type: String,
    @ColumnInfo(name = "auto_login") var auto_login: Boolean,
    @ColumnInfo(name = "profile_url") var profile_url: String,
)