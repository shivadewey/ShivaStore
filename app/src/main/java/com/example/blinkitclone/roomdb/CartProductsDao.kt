package com.example.blinkitclone.roomdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter

import androidx.room.Update
import com.example.blinkitclone.utils.UriTypeConverter


@Dao

interface CartProductsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartProduct(cartProducts: CartProducts)

    @Update
    fun updateCartProduct(cartProducts: CartProducts)

    @Query("DELETE FROM CartProducts WHERE productId=:productId")
    suspend fun deleteCartProduct(productId : String?)

    @Query("SELECT * FROM CartProducts")
    fun getAllProductsNotes() : LiveData<List<CartProducts>>

    @Query("DELETE FROM CartProducts")
     suspend fun deleteAll()
}