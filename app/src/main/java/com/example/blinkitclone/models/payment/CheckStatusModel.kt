package com.example.blinkitclone.models.payment

import com.example.blinkitclone.models.payment.status.Data

data class CheckStatusModel(
    val code: String,
    val data: Data,
    val message: String,
    val success: Boolean
)