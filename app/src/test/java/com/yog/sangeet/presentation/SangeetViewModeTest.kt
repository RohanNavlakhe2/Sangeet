package com.yog.sangeet_online.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.yog.sangeet_online.MainCoroutineRule
import com.yog.sangeet_online.SangeetViewModel
import com.yog.sangeet_online.data.VideoInfoDto
import com.yog.sangeet_online.data.repository.SangeetFakeRepository
import com.yog.sangeet_online.domain.usecases.GetVideoInfoUseCase
import com.yog.sangeet_online.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
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
        Assert.assertEquals(result.javaClass,Resource.Success::class.java)
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


}