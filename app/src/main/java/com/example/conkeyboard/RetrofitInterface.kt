package com.example.conkeyboard

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitInterface {
    @POST("/search")
    fun getSearchedCons(@Body name: String): Call<List<ConData>>

    @POST("/dccon")
    fun getOneCon(@Body num: String): Call<ConData>

    @POST("/dailyhit")
    fun getDailyHitCons(): Call<List<ConData>>

    @POST("/weeklyhit")
    fun getWeeklyHitCons(): Call<List<ConData>>

    @POST("/new")
    fun getNewCons(): Call<List<ConData>>
}