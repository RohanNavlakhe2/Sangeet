package com.yog.sangeet.sangeet_online.domain.repository

import com.yog.sangeet.sangeet_online.data.VideoInfoDto
import com.yog.sangeet.sangeet_online.util.Resource
import kotlinx.coroutines.flow.Flow

interface SangeetRepository {
    fun getVideoInfo(videoUrl:String): Flow<Resource<VideoInfoDto>>
}