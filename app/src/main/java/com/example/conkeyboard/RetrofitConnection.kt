package com.example.conkeyboard

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitConnection(context: Context) {
    private val url: String = context.resources.getString(R.string.url)
    private val client: OkHttpClient = OkHttpClient.Builder().connectTimeout(1, TimeUnit.MINUTES).readTimeout(1, TimeUnit.MINUTES).build()
    private val retrofit: Retrofit = Retrofit.Builder().baseUrl(url).client(client).addConverterFactory(
        GsonConverterFactory.create()).build()
    val server: RetrofitInterface = retrofit.create(
        RetrofitInterface::class.java)
}