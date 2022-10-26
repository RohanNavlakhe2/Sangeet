package com.yog.sangeet.sangeet_online.domain.usecases

import com.yog.sangeet.sangeet_online.data.VideoInfoDto
import com.yog.sangeet.sangeet_online.domain.repository.SangeetRepository
import com.yog.sangeet.sangeet_online.util.Resource
import kotlinx.coroutines.flow.Flow


class GetVideoInfoUseCase(private val sangeetRepository: SangeetRepository) {

    operator fun invoke(videoUrl:String): Flow<Resource<VideoInfoDto>> {
        return sangeetRepository.getVideoInfo(videoUrl)
    }


}