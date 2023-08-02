package com.yog.sangeet;

import static com.yog.sangeet.MainActivity.songsListToDisplay;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yog.sangeet.util.RecyclerPlaylist;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;
import timber.log.Timber;


@AndroidEntryPoint
public class MusicList extends AppCompatActivity {
    static String mediaTitleOnLaunch = null;
    static String currentImage;
    static int nextSongIndex;
    public static MediaUtil mediaUtil;
    static MediaPlayer previousMediaPlayer;
    public static boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        Intent intent = getIntent();
        ((TextView)findViewById(R.id.folderNameTxtView)).setText(intent.getStringExtra("folder"));
        //setTitle(intent.getStringExtra("folder"));
        if (flag == true)
            definingHeightOfTheOfList();
        mediaUtil = new MediaUtil(this);
        mediaUtil.mediaPlayer = previousMediaPlayer;
        displayMusicList();
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        previousMediaPlayer = mediaUtil.mediaPlayer;
    }

    public void definingHeightOfTheOfList() {
        RecyclerView recyclerView = findViewById(R.id.songs_list);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int dpiOfScreen = metrics.densityDpi;
        switch (dpiOfScreen) {
            case DisplayMetrics.DENSITY_MEDIUM://(160)
                break;
            case DisplayMetrics.DENSITY_HIGH://(240)
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        600);
                recyclerView.setLayoutParams(layoutParams);
                break;
            case DisplayMetrics.DENSITY_XHIGH://320
                break;
            case DisplayMetrics.DENSITY_XXHIGH://480
                break;
            case DisplayMetrics.DENSITY_XXXHIGH://640
                break;
        }
    }

    private void displayMusicList() {
        RecyclerView songsRecyclerView = findViewById(R.id.songs_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        songsRecyclerView.setLayoutManager(linearLayoutManager);
        RecyclerPlaylist recyclerPlaylist = new RecyclerPlaylist(this, songsListToDisplay, "song_view");
        songsRecyclerView.setAdapter(recyclerPlaylist);
        if (mediaTitleOnLaunch != null) {
            displayRunningMusicCard(mediaTitleOnLaunch);
        }
    }

    public static void listenersDuty(ArrayList<AudioFile> tempSongList, int index) {
        nextSongIndex = 0;
        if (index + 1 < tempSongList.size())
            nextSongIndex = index + 1;
        mediaUtil.prepareMedia(tempSongList, index);
    }

    public void displayRunningMusicCard(final String mediaFileTitle) {
        TextView musicTitle = findViewById(R.id.running_song_title);
        musicTitle.setText(mediaFileTitle);
        ImageView runningSongImage = findViewById(R.id.running_song_image);
        runningSongImage.setImageBitmap(
                RecyclerPlaylist.setImage(MediaUtil.mediaFileLocation, 40, 50));
        CardView runningSongCard = findViewById(R.id.running_song_card);
        runningSongCard.setVisibility(View.VISIBLE);
        final ImageButton playOrPauseMusicButton = findViewById(R.id.running_song_button);
        if (currentImage.equals("playing_button")) {
            playOrPauseMusicButton.setImageDrawable(getResources().getDrawable(R.mipmap.music_playing_icon));
        } else if (currentImage.equals("paused_button")) {
            playOrPauseMusicButton.setImageDrawable(getResources().getDrawable(R.mipmap.music_paused_icon));
        }
        playOrPauseMusicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentImage.equals("playing_button")) {
                    mediaUtil.pauseMusic();
                    //playOrPauseMusicButton.setImageDrawable(getResources().getDrawable(R.drawable.paused_icon));
                    currentImage = "paused_button";
                } else if (currentImage.equals("paused_button")) {
                    mediaUtil.playMusic();
                    //playOrPauseMusicButton.setImageDrawable(getResources().getDrawable(R.drawable.playing_icon));
                    currentImage = "playing_button";
                }

            }
        });
    }

    //When we remove the activity from task and open the app again still the static variables remains there
    //which creates issues (like currently running song card appears again)
    // so making it null.

    //But this also has issue, When we come back from MusicList activity then also onDestroy() gets called.
    //And if we now open any folder then in that MusicList we won't see currently playing song card.
    /*@Override
    protected void onDestroy() {
        Timber.d("Music List - on Destroy");
        mediaTitleOnLaunch = null;
        currentImage = null;
        nextSongIndex = 0;
        mediaUtil = null;
        previousMediaPlayer = null;
        flag = false;
        super.onDestroy();

    }*/
}
