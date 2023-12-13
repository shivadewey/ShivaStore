package com.example.blinkitclone.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.blinkitclone.utils.UriTypeConverter


@Database(entities = [CartProducts::class] , version = 1, exportSchema = false)
@TypeConverters(UriTypeConverter::class)
abstract class CartProductsDatabase : RoomDatabase() {

    abstract fun cartProductsDao() : CartProductsDao

    companion object{
        @Volatile
        var INSTANCE : CartProductsDatabase ? = null
        fun getDatabaseInstance(contex : Context) : CartProductsDatabase{
            val tempInstance = INSTANCE
            if (tempInstance != null) return tempInstance
            synchronized(this){
                val roomDbInstance =
                    Room.databaseBuilder(contex,CartProductsDatabase::class.java,"CartProducts").allowMainThreadQueries().build()
                INSTANCE = roomDbInstance
                return roomDbInstance
            }
        }
    }
}