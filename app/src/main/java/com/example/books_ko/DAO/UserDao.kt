package com.example.books_ko.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.books_ko.Data.UserData

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