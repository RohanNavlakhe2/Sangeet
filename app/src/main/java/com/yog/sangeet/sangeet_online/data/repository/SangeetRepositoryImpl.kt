package com.yog.sangeet.sangeet_online.data.repository

import com.yog.sangeet.sangeet_online.data.VideoInfoDto
import com.yog.sangeet.sangeet_online.data.network.SangeetApi
import com.yog.sangeet.sangeet_online.domain.repository.SangeetRepository
import com.yog.sangeet.sangeet_online.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber

class SangeetRepositoryImpl(private val sangeetApi: SangeetApi): SangeetRepository {

 override fun getVideoInfo(videoUrl: String):Flow<Resource<VideoInfoDto>> = flow {

       emit(Resource.Loading())

      try {
       val videoInfo = sangeetApi.getVideoInfo(videoUrl)
       emit(Resource.Success(videoInfo))
      }catch (e:Exception){
       emit(Resource.Error(e.message ?: "Something Went Wrong"))
       Timber.e(e)
      }

 }


}