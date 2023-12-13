package com.example.blinkitclone.models

import java.util.UUID

data class ProductType(
    val id : String = UUID.randomUUID().toString(),
    val categoryName : String ? = null,
    val products: ArrayList<Product>? = null
)