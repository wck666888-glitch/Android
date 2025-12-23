package com.cvte.irremote.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Android Emulator localhost
    private const val BASE_url = "http://10.0.2.2:3000/"
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val apiService: ConfigApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ConfigApiService::class.java)
    }
}
