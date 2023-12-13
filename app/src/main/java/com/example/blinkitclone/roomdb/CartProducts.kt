package com.example.blinkitclone.roomdb


import android.net.Uri
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "CartProducts")
data class CartProducts(

    @PrimaryKey
    val productId : String  = "random", // cant apply nullability check here.

    val productTitle : String ? = null,
    val productQuantity : String ? = null,
    val productPrice : String ? = null,
    var productCount : Int ? = null,
    var productStock : Int ? = null,
    var productImage : String ? = null,
    var productCategory : String ? = null,
    var storeOwnerUid : String ? = null,

)