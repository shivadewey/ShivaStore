package com.example.blinkitclone.models.payment.status

data class Data(
    val amount: String,
    val merchantId: String,
    val merchantTransactionId: String,
    val paymentInstrument: PaymentInstrument,
    val responseCode: String,
    val state: String,
    val transactionId: String
)