package com.example.conkeyboard

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitInterface {
    @POST("/search")
    fun getSearchedCons(@Body json: JsonForSearch): Call<List<ConData>>

    @POST("/dccon")
    fun getOneCon(@Body json: JsonForOneCon): Call<ConData>

    @POST("/newcon")
    fun getNewCons(): Call<List<ConData>>
}