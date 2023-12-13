package com.example.blinkitclone.models

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    @SerializedName("request")
    val requestJson: String
)