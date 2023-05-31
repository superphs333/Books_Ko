package com.example.books_ko.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.books_ko.DAO.UserDao
import com.example.books_ko.Data.UserData

// 데이터베이스 클래스 : 데이터베이스와 관련된 설정 및 데이터 액세스 객체(DAO)의 인스턴스를 포함함
    // 이렇게 얻은 데이터베이스는 인스턴스를 통해 userDao()메서드를 호출하여 데이터 액세스 객첼르 사용 할 수 있다
@Database(entities = [UserData::class], version = 1)
    // entities -> 해당 데이터베이스에서 사용할 엔티티 클래스들을 배열로 정의함
    // version -> 데이터베이스 버전 지정
abstract class  UserDatabase : RoomDatabase() {
    // 추상 메서드인 userDao를 정의하여 데이터 액세스 객체의 인스턴스를 반환함
    abstract fun userDao(): UserDao

    companion object{
        @Volatile // 인스턴스가 여러 스레드에서 동시에 수정되는 것을 방지함
        private var INSTANCE: UserDatabase? = null

        //데이터베이스 인스턴스를 생성하고 반환하는 정적 메서드
        fun getDatabase(context: Context) : UserDatabase{
            val tempInstance = INSTANCE
            if(tempInstance != null){
                return tempInstance
            }

            synchronized(this){ // 동시에 여러 스레드에서 데이터베이스 인스턴스를 생성하지 않도록
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "app_database" // 데이터베이스 이름 지정
                ).build()
                INSTANCE= instance
                return instance
            }
        }
    }
}
// 호출방법 : UserDatabase.getDatabase(context)