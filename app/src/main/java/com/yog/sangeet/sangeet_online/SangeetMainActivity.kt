package com.yog.sangeet.sangeet_online

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yog.sangeet.databinding.ActivitySangeetMainBinding
import com.yog.sangeet.sangeet_online.data.VideoInfoDto
import com.yog.sangeet.sangeet_online.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber


@AndroidEntryPoint
class SangeetMainActivity : AppCompatActivity() {

    private val sangeetViewModel by viewModels<SangeetViewModel>()

    private lateinit var actSangeetMainBinding: ActivitySangeetMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actSangeetMainBinding = ActivitySangeetMainBinding.inflate(layoutInflater)
        setContentView(actSangeetMainBinding.root)
        listenVideoInfo()

        Timber.d("Sangeet Activity")

        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val data = intent.getStringExtra(Intent.EXTRA_TEXT)
            actSangeetMainBinding.videoUrlTxt.text = data
            Timber.d("Video Url : $data")
            getVideoInfo(data)
        }
    }

    private fun getVideoInfo(videoUrl:String?){
        sangeetViewModel.getVideoInfo(videoUrl)
    }

    private fun listenVideoInfo(){
        lifecycleScope.launchWhenStarted {
            sangeetViewModel.videoInfoStateFlow.collectLatest {
                handleResult(it)
            }
        }

    }

    private fun handleResult(resource: Resource<VideoInfoDto>){
        when(resource){
            is Resource.Loading -> {
                actSangeetMainBinding.progressBar.visibility = View.VISIBLE
            }

            is Resource.Success -> {
                actSangeetMainBinding.progressBar.visibility = View.GONE
                Timber.d("Video Data : ${resource.data}")
            }

            is Resource.Error -> {
                actSangeetMainBinding.progressBar.visibility = View.GONE
                Toast.makeText(this,resource.message ?: "Something went wrong", Toast.LENGTH_SHORT).show()
            }
            is Resource.Init -> {}
        }
    }
}