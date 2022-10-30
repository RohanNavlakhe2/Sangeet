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
import java.text.DecimalFormat
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

    fun convertMilisecondsToMinutesAndSeconds(milis:Long):String{
        val min = (milis/1000)/60
        val sec = (milis/1000)%60
        val numberFormat = DecimalFormat("00")
        var formattedMin = numberFormat.format(min)
        var formattedSec = numberFormat.format(sec)
        return "$formattedMin:$formattedSec"
    }
}