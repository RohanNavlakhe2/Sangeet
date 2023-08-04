package com.yog.sangeet

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.yog.sangeet.databinding.ActivityExoPlayerBinding
import com.yog.sangeet.sangeet_online.Constants.MEDIA_ROOT_ID
import com.yog.sangeet.sangeet_online.SangeetService
import com.yog.sangeet.sangeet_online.SangeetViewModel
import com.yog.sangeet.sangeet_online.data.VideoInfoDto
import com.yog.sangeet.sangeet_online.exoplayer.MusicServiceConnection
import com.yog.sangeet.sangeet_online.exoplayer.extension.currentPlaybackPosition
import com.yog.sangeet.sangeet_online.exoplayer.extension.isPlayEnabled
import com.yog.sangeet.sangeet_online.exoplayer.extension.isPlaying
import com.yog.sangeet.sangeet_online.exoplayer.extension.isPrepared
import com.yog.sangeet.sangeet_online.util.MusicSource
import com.yog.sangeet.sangeet_online.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ExoPlayerActivity : AppCompatActivity(){

    lateinit var musicServiceConnection: MusicServiceConnection
    private lateinit var musicPlaybackState: LiveData<PlaybackStateCompat?>

    private val sangeetViewModel by viewModels<SangeetViewModel>()

    private var videoInfoDto: VideoInfoDto? = null

    private lateinit var binding: ActivityExoPlayerBinding

    private lateinit var downloadManager: DownloadManager

    private var shouldUpdateSeekbar = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listenVideoInfo()
        setSeekbarChangeListener()

        Timber.d("ExoPlayerActivity - onCreate")


        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        binding.downloadSongImg.setOnClickListener {

            if(videoInfoDto==null){
                return@setOnClickListener
            }

            if(videoInfoDto!!.downloadUrl == null || videoInfoDto!!.downloadUrl?.trim()?.isEmpty() == true){
                Toast.makeText(this, "Cannot Download This Song", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            DownloadUtil.download(videoInfoDto!!.title ?: "",videoInfoDto!!.downloadUrl!!, downloadManager)
        }

        getVideoInfo(intent)

    }

    private fun getVideoInfo(intent: Intent) {
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val data = intent.getStringExtra(Intent.EXTRA_TEXT)
            Timber.d("Video Url : $data")
            sangeetViewModel.getVideoInfo(data)
        }
    }

    private fun listenVideoInfo() {
        lifecycleScope.launchWhenStarted {
            sangeetViewModel.videoInfoStateFlow.collectLatest {
                handleResult(it)
            }
        }

    }

    private fun handleResult(resource: Resource<VideoInfoDto>) {
        when (resource) {
            is Resource.Loading -> {
                binding.videoInfoProgressBar.visibility = View.VISIBLE
            }

            is Resource.Success -> {
                binding.videoInfoProgressBar.visibility = View.GONE
                binding.downloadSongImg.visibility = View.VISIBLE

                videoInfoDto = resource.data


                videoInfoDto?.downloadUrl?.let {
                    MusicSource.setMusic(listOf(videoInfoDto!!))
                    startSangeetService()
                }

                Timber.d("Video Data : ${resource.data}")
            }

            is Resource.Error -> {
                binding.videoInfoProgressBar.visibility = View.GONE
                Toast.makeText(this, resource.message ?: "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }
            is Resource.Init -> {}
        }
    }

    private fun startSangeetService(){

        musicServiceConnection = MusicServiceConnection(this)
        musicPlaybackState = musicServiceConnection.playbackState
        listenPlayBackState()
        updatePlayBackPosition()

        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object :
            MediaBrowserCompat.SubscriptionCallback() {

            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                Timber.tag(TAG).d("On Children Loaded")
                val mediaItem = children[0]
                val videoInfo = VideoInfoDto(
                    mediaItem.description.title.toString(),
                    mediaItem.description.iconUri.toString(),
                    mediaItem.description.mediaUri.toString()
                )

                binding.songNameTxt.text = videoInfo.title ?: "Music"
                Glide.with(this@ExoPlayerActivity)
                    .load(videoInfo.thumbnail)
                    .into(binding.songThumbnailImageView)

                binding.songControllerBtn.setOnClickListener {
                    val isPrepared = musicPlaybackState.value?.isPrepared ?: false
                    if(isPrepared){
                        if(musicPlaybackState.value?.isPlaying == true){
                            musicServiceConnection.transportControls.pause()
                            toggleSongStateIcon(R.drawable.ic_play)
                        }else if(musicPlaybackState.value?.isPlayEnabled == true){
                            musicServiceConnection.transportControls.play()
                            toggleSongStateIcon(R.drawable.ic_pause)
                        }
                    }
                }

                Timber.tag(TAG).d("Video Info : $videoInfo")
            }
        })
    }

    private fun listenPlayBackState(){
        musicPlaybackState.observe(this){
            if(it?.isPlaying == true){
                toggleSongStateIcon(R.drawable.ic_pause)
            }else {
                toggleSongStateIcon(R.drawable.ic_play)
            }

            if(shouldUpdateSeekbar)
              binding.songProgressSeekBar.progress = it?.position?.toInt() ?: 0
            setCurrentSongTime(it?.position ?: 0)
        }
    }

    private fun updatePlayBackPosition(){
        lifecycleScope.launch {
            while(true){
                val currentPlaybackPos =  musicPlaybackState.value?.currentPlaybackPosition?.toInt() ?: 0
                if(binding.songProgressSeekBar.progress != currentPlaybackPos){

                    if(shouldUpdateSeekbar)
                       binding.songProgressSeekBar.progress = currentPlaybackPos
                    binding.songProgressSeekBar.max = SangeetService.curSongDuration.toInt()
                    setTotalSongDuration(SangeetService.curSongDuration)
                    setCurrentSongTime(musicPlaybackState.value?.currentPlaybackPosition ?: 0)
                }
                delay(100)
            }
        }
    }

    private fun setCurrentSongTime(duration:Long){
        binding.songCurrentTimeTxt.text = sangeetViewModel.convertMilisecondsToMinutesAndSeconds(duration)
    }

    private fun setTotalSongDuration(duration:Long){
         binding.songDurationTxt.text = sangeetViewModel.convertMilisecondsToMinutesAndSeconds(duration)
    }

    private fun toggleSongStateIcon(icon:Int){
        binding.songControllerBtn.setImageResource(icon)
    }

    private fun setSeekbarChangeListener(){
        binding.songProgressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                 if (fromUser){
                     setCurrentSongTime(progress.toLong())
                 }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = true
                seekBar?.let {
                    musicServiceConnection.transportControls.seekTo(it.progress.toLong())
                }
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        //musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
        //stopService(Intent(applicationContext,SangeetService::class.java))
        /*exoPlayer.release()
        SangeetService.stopSangeetService()*/
        Timber.d("ExoPlayerActivity - on New Intent")
        intent?.let {
            getVideoInfo(it)
        }

    }

    companion object{
        const val TAG = "Sangeet"
    }


}