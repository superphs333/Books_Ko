package com.example.books_ko.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.books_ko.Data.UserData

// 데이터 액세스 객체(DAO)
    // Room라이브러리에서 제공하는 인터페이스
    // 데이터베이스에 접근하여 데이터를 조작하는 메서드를 정의함
/*
어노테이션
@Query : SQL쿼리를 실행하기 위한 어노테이션
@Insert : 단일 객체 또는 여러 객체를 동시에 삽입할 수 있다
@Update
@Delete
 */
@Dao
interface  UserDao {
    @Query("SELECT * FROM user")
    fun getUser(): LiveData<UserData>

    @Query("SELECT * FROM user")
    fun getUser2(): UserData

    @Query("SELECT email FROM user")
    fun getUserEmail(): String

    @Query("SELECT * FROM user WHERE email = :email")
    fun getUserByEmail(email: String): LiveData<UserData?>

    @Insert
    fun insertUser(user: UserData)

    @Query("DELETE FROM user")
    fun clearAllUsers()

    @Query("UPDATE user SET nickname = :newNickname WHERE email = :email")
    fun updateUserNickName(email: String, newNickname: String)

}