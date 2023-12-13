package com.example.blinkitclone.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

import java.util.UUID

@Parcelize
data class Product(
    val id : String = UUID.randomUUID().toString(),
    val productRandomId : String ? = null,
    val productTitle : String ? = null,
    val productQuantity : String ? = null,
    val productUnit : String ? = null,
    val productPrice : String ? = null,
    val productStock : String ? = null,
    val productCategory : String ? = null,
    val productType : String ? = null,
    var itemCount : String ? = null,
    val storeType : String ? = null,
    val storeOwnerUid : String ? = null,
    var productImageUris : ArrayList<String?> ? = null,
) : Parcelable