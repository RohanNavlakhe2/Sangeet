package com.yog.sangeet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.yog.sangeet.util.RecyclerPlaylist;

import java.io.IOException;
import java.util.ArrayList;



public class MediaUtil implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {

    public MediaPlayer mediaPlayer;
    private static AudioManager audioManager;
    private static AudioFocusRequest audioFocusRequest;
    private MusicList context;
    private static Intent intent;
    private static String mediaFileTitle;
    public static String mediaFileLocation;
    private static ArrayList<AudioFile> tempSongList;
    public static NotificationManager manager;

    MediaUtil(MusicList context) {
         this.context=context;
         definingAudioManager();
    }

    private void definingAudioManager(){
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = createAudioFocusRequestForAndroidO();
        }
    }

    void prepareMedia(ArrayList<AudioFile> tempSongList,int index) {
        MediaUtil.tempSongList=tempSongList;
        MediaUtil.mediaFileTitle=tempSongList.get(index).getMediaTitle();
        MediaUtil.mediaFileLocation=tempSongList.get(index).getMediaFile();

        int foucsState = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
          switch (foucsState) {
              case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                  initializingMediaPlayer();
                  break;
              case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                  Toast.makeText(context, "please try again", Toast.LENGTH_SHORT).show();
                  break;
              case AudioManager.AUDIOFOCUS_REQUEST_DELAYED:
                  break;
          }
          }

   private void initializingMediaPlayer(){
        mediaPlayer.reset();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);

        try {
            mediaPlayer.setDataSource(mediaFileLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.prepareAsync();
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Intent startOrStopServiceToplaySong = new Intent(context, ServiceToStartSong.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(startOrStopServiceToplaySong);
        }else{
            context.startService(startOrStopServiceToplaySong);
        }
    }


     void manageAudio() {
        if (mediaPlayer.isPlaying()) {
           pauseMusic();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            } else
                audioManager.abandonAudioFocus(this);
        } else {
            playMusic();
        }
    }
    public void playMusic(){
        mediaPlayer.start();
        createMediaStyleNotification("playing");
        displayRunningMusic("playing_button");
   }
    public void pauseMusic(){
        mediaPlayer.pause();
        createMediaStyleNotification("paused");
        displayRunningMusic("paused_button");
    }
    public void displayRunningMusic(String buttonState){
        MusicList.mediaTitleOnLaunch=mediaFileTitle;
        MusicList.currentImage=buttonState;
        context.displayRunningMusicCard(mediaFileTitle);
   }


    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                //Is your app a media player or podcast player that will hold the audio
                // focus for an indefinite period of time (as long as the user choose to
                // play audio from your app)? This is AUDIOFOCUS_GAIN.
                      //this condition will be true if player is paused because of
                    //AudioManager.AUDIOFOCUS_LOSS: or AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                     // this will be called if player is already playing but its volume is
                    //less because of AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                //The service lost audio focus, the user probably moved to playing media
                // on another app, so release the media player.
                try{
                MusicList.previousMediaPlayer.release();}
                catch (NullPointerException n){
                    mediaPlayer.release();
                }
                //first time when MusicList activity will be launched then MusicList.previousMediaPlayer
                // will be null so that time we will require to relase the current media player.
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                //Does your app temporarily need audio focus (but for an unknown duration,
                // without the option to duck) like a phone app would once a call is
                // connected? This is AUDIOFOCUS_GAIN_TRANSIENT.
                pauseMusic();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Or does your app temporarily need audio focus (with the option to duck),
                // since it needs to play an audio notification, or a turn by turn spoken
                // direction, or it needs to record audio from the user for a short period
                // of time? This is AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK.
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                //Does your app temporarily need audio focus (for an unknown duration,
                // during which no other sounds should be generated) since it needs to
                // record audio like a voice memo app? This is
                // AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE.
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
               pauseMusic();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private AudioFocusRequest createAudioFocusRequestForAndroidO() {
        AudioAttributes mAudioAttributes =
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build();
        AudioFocusRequest mAudioFocusRequest =
                new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(mAudioAttributes)
                        .setAcceptsDelayedFocusGain(true)
                        .setOnAudioFocusChangeListener(this)
                        .build();

        return mAudioFocusRequest;
    }

    private void createMediaStyleNotification(String state) {
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("10", "media style", NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "10").
                setSmallIcon(R.mipmap.headphone_icon).
                setContentTitle(mediaFileTitle).
                setLargeIcon(
                        RecyclerPlaylist.setImage(mediaFileLocation,70,80)
                )
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (state.equals("playing")) {
            builder.addAction(R.mipmap.music_playing_icon, "playing", getLaunchPi());
        } else if (state.equals("paused"))
            builder.addAction(R.mipmap.music_paused_icon, "paused", getLaunchPi());
        manager.notify(10, builder.build());
    }

    public Notification createMediaStyleNotification() {

        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("10", "media style", NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "10").
                setSmallIcon(R.mipmap.headphone_icon).
                setContentTitle(mediaFileTitle).
                setLargeIcon(
                        RecyclerPlaylist.setImage(mediaFileLocation,70,80)
                )
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setPriority(NotificationCompat.PRIORITY_HIGH);


        builder.addAction(R.mipmap.music_playing_icon, "playing", getLaunchPi());
        Notification notification = builder.build();
        manager.notify(10,notification);
        return notification;
    }

    private PendingIntent getLaunchPi() {
        intent = new Intent(context, ServiceToStartSong.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return pendingIntent;
    }

     @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        pauseMusic();
        MusicList.listenersDuty(tempSongList,MusicList.nextSongIndex);
    }
}
