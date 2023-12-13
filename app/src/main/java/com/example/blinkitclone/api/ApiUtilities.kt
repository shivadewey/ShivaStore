package com.example.blinkitclone.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtilities {
    suspend fun getApiInterface(): ApiInterFace {
        return Retrofit.Builder()
            .baseUrl("https://api-preprod.phonepe.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterFace::class.java)
    }

    val notificationApi : ApiInterFace by lazy {
        Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterFace::class.java)
    }
}