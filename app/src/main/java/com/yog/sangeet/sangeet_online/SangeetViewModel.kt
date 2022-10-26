package com.yog.sangeet.sangeet_online

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yog.sangeet.sangeet_online.data.VideoInfoDto
import com.yog.sangeet.sangeet_online.domain.usecases.GetVideoInfoUseCase
import com.yog.sangeet.sangeet_online.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SangeetViewModel @Inject constructor(
    private val getVideoInfoUseCase: GetVideoInfoUseCase
) : ViewModel() {

    private val _videoInfoStateFlow = MutableStateFlow<Resource<VideoInfoDto>>(Resource.Init())
    val videoInfoStateFlow:StateFlow<Resource<VideoInfoDto>> = _videoInfoStateFlow

    fun getVideoInfo(videoUrl: String?) = viewModelScope.launch {

        if(videoUrl.isNullOrEmpty()){
            _videoInfoStateFlow.value = Resource.Error("Invalid Video Url")
            return@launch
        }

        try {
            getVideoInfoUseCase(videoUrl).onEach {
                _videoInfoStateFlow.value = it
            }.launchIn(this)
        }catch (e:Exception){
            _videoInfoStateFlow.value = Resource.Error(e.message ?: "Something went wrong")
        }

    }
}