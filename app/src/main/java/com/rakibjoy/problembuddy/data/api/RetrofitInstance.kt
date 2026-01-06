package com.rakibjoy.problembuddy.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://codeforces.com/api/"

    val contestHistoryApi: ContestHistoryApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ContestHistoryApi::class.java)
    }
}
