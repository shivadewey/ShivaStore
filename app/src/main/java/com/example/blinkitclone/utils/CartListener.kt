package com.example.blinkitclone.utils

import android.net.Uri

interface CartListener {
    fun onProductAddedToCart(shouldVisible : Boolean)

    fun onSettingImageUri(imageUri : Uri)

    fun gettingTotalItemInTheCart(fromCategoryProduct : Boolean)

    fun showingCartItemCount(itemCount : String)

    fun savingTotalItemInSp(totalCount : Int)

    fun hideCartLayout()

}