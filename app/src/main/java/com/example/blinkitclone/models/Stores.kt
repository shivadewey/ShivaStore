package com.example.blinkitclone.models

data class Stores(
    val storeOwnerUid : String ? = null,
    val storeId : String ? = null,
    val storeOwnerName : String ? = null,
    val storeName : String ? = null,
    val storeState : String ? = null,
    val storeCity : String ? = null,
    val storeAddress : String ? = null,
    val storeTypes : String ? = null,
    val storeOwnerNumber : String ? = null,
    var storeOwnerToken : String ? = null,
)

