package com.yog.sangeet.sangeet_online.util

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.yog.sangeet.sangeet_online.data.VideoInfoDto
import timber.log.Timber
import kotlin.random.Random

object MusicSource {

    var songs = emptyList<MediaMetadataCompat>()

    fun setMusic(videoInfoList:List<VideoInfoDto>){
       songs = videoInfoList.map { videoInfo ->
          MediaMetadataCompat.Builder()
              .putString(METADATA_KEY_MEDIA_ID,"${Random.nextInt()} ${videoInfo.title}")
              .putString(METADATA_KEY_TITLE,videoInfo.title)
              .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,videoInfo.title)
              .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,videoInfo.thumbnail)
              .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,videoInfo.downloadUrl)
              .build()
       }
    }

    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            /*.setSubtitle(song.description.subtitle)*/
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }.toMutableList()

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        Timber.d("As Media Source")
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri()))
            Timber.d("Media Source Uri : ${song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)} ")
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaSource2(dataSourceFactory: DataSource.Factory): ProgressiveMediaSource {
        val song = songs[0]
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri()))
    }
}