package com.yog.sangeet

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.yog.sangeet.databinding.ActivityExoPlayerBinding
import com.yog.sangeet.sangeet_online.Constants.MEDIA_ROOT_ID
import com.yog.sangeet.sangeet_online.SangeetViewModel
import com.yog.sangeet.sangeet_online.data.VideoInfoDto
import com.yog.sangeet.sangeet_online.exoplayer.MusicServiceConnection
import com.yog.sangeet.sangeet_online.util.MusicSource
import com.yog.sangeet.sangeet_online.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ExoPlayerActivity : AppCompatActivity()/*, Player.Listener*/ {

    /*@Inject*/
    lateinit var musicServiceConnection: MusicServiceConnection

    private val sangeetViewModel by viewModels<SangeetViewModel>()

    private var videoInfoDto: VideoInfoDto? = null

    private lateinit var binding: ActivityExoPlayerBinding

    private lateinit var exoPlayer: ExoPlayer
    private var playbackPosition: Long = 0

    private lateinit var downloadManager: DownloadManager

    private val dataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(this, "exoplayer-sample")
    }

    private var url = ""

    /*private lateinit var exoPlayerView:StyledPlayerView
    private lateinit var progressBar:ProgressBar*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listenVideoInfo()

        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        binding.download.setOnClickListener {

            if(videoInfoDto==null){
                return@setOnClickListener
            }

            if(videoInfoDto!!.downloadUrl == null || videoInfoDto!!.downloadUrl?.trim()?.isEmpty() == true){
                Toast.makeText(this, "Cannot Download This Song", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            DownloadUtil.download(videoInfoDto!!.title ?: "",videoInfoDto!!.downloadUrl!!, downloadManager)
        }

        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val data = intent.getStringExtra(Intent.EXTRA_TEXT)
            Timber.d("Video Url : $data")
            getVideoInfo(data)
        }
    }

    override fun onStart() {
        super.onStart()
        //initializePlayer()
    }

    /*private fun initializePlayer(url:String) {
        exoPlayer = ExoPlayer.Builder(this).build()
        preparePlayer(url)
        binding.exoPlayerView.player = exoPlayer
        exoPlayer.seekTo(playbackPosition)
        exoPlayer.addListener(this)
    }*/


    private fun preparePlayer(url: String) {
        val uri = Uri.parse(url)
        val mediaSource = buildMediaSource(uri, "")
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()

    }

    private fun buildMediaSource(uri: Uri, type: String): MediaSource {
        return if (type == "dash") {
            DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))
        }
    }

    private fun getVideoInfo(videoUrl: String?) {
        sangeetViewModel.getVideoInfo(videoUrl)
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
               // binding.exoPlayerView.visibility = View.VISIBLE
                binding.download.visibility = View.VISIBLE

                videoInfoDto = resource.data


                videoInfoDto?.downloadUrl?.let {
                    MusicSource.setMusic(listOf(videoInfoDto!!))
                    startSangeetService()
                    //initializePlayer(it)
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

    private var isPlaying = false

    private fun startSangeetService(){

        musicServiceConnection = MusicServiceConnection(this)

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

                binding.songControllerBtn.setOnClickListener {
                    if(isPlaying){
                        musicServiceConnection.transportControls.pause()
                        isPlaying = false
                    }else{
                        musicServiceConnection.transportControls.play()
                        isPlaying = true
                    }
                }

                Timber.tag(TAG).d("Video Info : $videoInfo")
            }
        })
    }

   /* override fun onPlayerError(error: PlaybackException) {
        Log.d("Streaming", "Error : ${error.message}")
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING)
            binding.progrssBar.visibility = View.VISIBLE
        else
            binding.progrssBar.visibility = View.INVISIBLE
    }*/

    override fun onStop() {
        //releasePlayer()
        super.onStop()
    }

    private fun releasePlayer() {
        playbackPosition = exoPlayer.currentPosition
        exoPlayer.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
    }

    companion object{
        const val TAG = "Sangeet"
    }


}