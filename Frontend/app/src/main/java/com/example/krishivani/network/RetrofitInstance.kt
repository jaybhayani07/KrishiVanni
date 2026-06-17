package com.example.krishivani.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // 10.0.2.2 is the special IP alias to your host loopback interface (localhost) on the Android Emulator
    private const val BASE_URL = "http://10.0.2.2:2503/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
