package com.yog.sangeet.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yog.sangeet.MusicList;


public class HeadphoneRemovalBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MusicList.mediaUtil.pauseMusic();
    }
}
