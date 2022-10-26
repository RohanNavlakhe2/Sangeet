package com.yog.sangeet.sangeet_online.data.network

import com.yog.sangeet.sangeet_online.data.VideoInfoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface SangeetApi {

    @GET("/videoInfo")
    suspend fun getVideoInfo(@Query("videoUrl") videoUrl:String): VideoInfoDto
}