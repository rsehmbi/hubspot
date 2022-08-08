package com.example.hubspot.schedule.API

import com.example.hubspot.schedule.Utils.constants.Companion.BASE_URL
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance {

    // Helps in setting the lenient criteria for GSON otherwise if json contains some error. App will fail to retrieve
    var gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Initializes Simple API by Retrofit
    // This code is used from retrofit documentation
    val api: SimpleApi by lazy {
        retrofit.create(SimpleApi::class.java)
    }
}