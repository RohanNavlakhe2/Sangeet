package com.yog.sangeet_online.data.repository

import com.yog.sangeet_online.data.VideoInfoDto
import com.yog.sangeet_online.domain.repository.SangeetRepository
import com.yog.sangeet_online.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SangeetFakeRepository: SangeetRepository{

    override fun getVideoInfo(videoUrl: String): Flow<Resource<VideoInfoDto>> = flow {
         val videoInfo = VideoInfoDto(
             "Title",
             "",
             ""
         )

        emit(Resource.Success(videoInfo))
    }

}