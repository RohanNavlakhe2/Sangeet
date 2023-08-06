package com.yog.sangeet.sangeet_online

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.yog.sangeet.ExoPlayerActivity
import com.yog.sangeet.MainActivity
import com.yog.sangeet.sangeet_online.exoplayer.MusicNotificationManager
import com.yog.sangeet.sangeet_online.exoplayer.callbacks.MusicPlayerEventListener
import com.yog.sangeet.sangeet_online.exoplayer.callbacks.MusicPlayerNotificationListener
import com.yog.sangeet.sangeet_online.util.MusicSource
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


private const val SERVICE_TAG = "SangeetService"

@AndroidEntryPoint
class SangeetService:MediaBrowserServiceCompat()  {

    @Inject
    lateinit var exoPlayer:ExoPlayer

    /*@Inject
    lateinit var defaultDataSourceFactory:DefaultDataSourceFactory*/

    private val defaultDataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(this, "exoplayer-sample")
    }

    private lateinit var musicNotificationManager: MusicNotificationManager

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    private var isPlayerInitialized = false

    private lateinit var musicPlayerEventListener: MusicPlayerEventListener



    companion object {
        var curSongDuration = 0L
            private set
    }
    override fun onCreate() {
        super.onCreate()
        Timber.d("Sangeet Service : onCreate")
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, FLAG_IMMUTABLE)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {
            curSongDuration = exoPlayer.duration
        }

        preparePlayer(MusicSource.songs,MusicSource.songs[0],true)

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        //mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)

    }
    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        //val curSongIndex = if(curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        val curSongIndex = 0
       // exoPlayer.setMediaSource(MusicSource.asMediaSource(defaultDataSourceFactory))
        exoPlayer.setMediaSource(MusicSource.asMediaSource2(defaultDataSourceFactory))
        exoPlayer.prepare()
        exoPlayer.seekTo(curSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? = BrowserRoot(Constants.MEDIA_ROOT_ID,null)

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId){
            Constants.MEDIA_ROOT_ID -> {
                result.sendResult(MusicSource.asMediaItems())
                if(!isPlayerInitialized && MusicSource.songs.isNotEmpty()){
                    preparePlayer(MusicSource.songs,MusicSource.songs[0],false)
                    isPlayerInitialized = true
                }
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (rootIntent!!.component!!.className == ExoPlayerActivity::class.java.name) {
            exoPlayer.removeListener(musicPlayerEventListener)
            exoPlayer.release()
            //stopSelf(Constants.NOTIFICATION_ID)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Sangeet Service destroy")
        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return MusicSource.songs[windowIndex].description
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("Sangeet Service : onStartCommand")
        return super.onStartCommand(intent, flags, startId)

    }

}