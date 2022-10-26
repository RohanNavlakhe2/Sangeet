package com.yog.sangeet.sangeet_online.exoplayer.callbacks

import android.widget.Toast
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.yog.sangeet.sangeet_online.SangeetService
import timber.log.Timber


class MusicPlayerEventListener(
    private val sangeetService: SangeetService
) : Player.Listener {

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayWhenReadyChanged(playWhenReady,playbackState)
        if(playbackState == Player.STATE_READY && !playWhenReady) {
            sangeetService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Timber.d("On Player Error : ${error.message}")
        Toast.makeText(sangeetService, "An unknown error occured", Toast.LENGTH_LONG).show()

    }
}