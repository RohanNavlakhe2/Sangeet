package com.yog.sangeet;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.yog.sangeet.util.HeadphoneRemovalBroadcastReceiver;


public class ServiceToStartSong extends Service {
    HeadphoneRemovalBroadcastReceiver headphoneRemoval;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("srvc","create");
        super.onCreate();
        headphoneRemoval=new HeadphoneRemovalBroadcastReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(headphoneRemoval,intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("srvc","start");
        /*If your service is started then also make this service run in the foreground,
         supplying the ongoing notification to be shown to the user while in this state...
         By default started services are background, meaning that their process won't be given
         foreground CPU scheduling (unless something else in that process is foreground)*/

        //also

        /*If your app targets API level 26 or higher, the system imposes restrictions on using
        or creating background services unless the app itself is in the foreground. If an app
        needs to create a foreground service, the app should call startForegroundService().
        That method creates a background service, but the method signals to the system that
        the service will promote itself to the foreground. Once the service has been created,
        the service must call its startForeground() method within five seconds.*/

        //try to display your music start notification here to avoid two notifications.
        //1 . music playing
        //2.  this one
         /*Notification notification = new NotificationCompat.Builder(this, "10")
                .setContentTitle("title")
                .setContentText("text")
                .setSmallIcon(R.drawable.music)
                .build();*/
        //startForeground(2001,MusicList.mediaUtil.createMediaStyleNotification());
        startForeground(10,MusicList.mediaUtil.createMediaStyleNotification());
        MusicList.mediaUtil.manageAudio();
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        Log.i("srvc","destroy");
        super.onDestroy();
        MusicList.mediaUtil.pauseMusic();
        MusicList.mediaUtil.mediaPlayer.release();
        MediaUtil.manager.cancel(10);
        unregisterReceiver(headphoneRemoval);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
}
