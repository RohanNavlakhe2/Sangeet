package com.yog.sangeet.presentation


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.yog.sangeet.sangeet_online.SangeetViewModel
import com.yog.sangeet.sangeet_online.domain.usecases.GetVideoInfoUseCase
import com.yog.sangeet.sangeet_online.util.Resource
import com.yog.sangeet_online.MainCoroutineRule
import com.yog.sangeet.data.repository.SangeetFakeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class SangeetViewModeTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var sangeetViewModel: SangeetViewModel


    @Before
    fun setUp(){
        val useCase = GetVideoInfoUseCase(SangeetFakeRepository())
        sangeetViewModel = SangeetViewModel(useCase)
    }

    @Test
    fun `when valid video url`() = runBlocking{
        sangeetViewModel.getVideoInfo("https://www.video.com")
        val result = sangeetViewModel.videoInfoStateFlow.value
        Assert.assertEquals(result.javaClass, Resource.Success::class.java)
    }

    @Test
    fun `when empty video url`() = runBlocking{
        sangeetViewModel.getVideoInfo("")
        val result = sangeetViewModel.videoInfoStateFlow.value
        Assert.assertEquals(result.javaClass,Resource.Error::class.java)
    }

    @Test
    fun `when null video url`() = runBlocking{
        sangeetViewModel.getVideoInfo(null)
        val result = sangeetViewModel.videoInfoStateFlow.value
        Assert.assertEquals(result.javaClass,Resource.Error::class.java)
    }

   /* @Test
    fun `is correct formatting`() {
        val formattedString = sangeetViewModel.convertMilisecondsToMinutesAndSeconds(3500000)
        Assert.assertEquals("")
    }*/


}