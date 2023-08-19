package com.yog.sangeet;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxbinding4.widget.RxTextView;
import com.yog.sangeet.util.RecyclerPlaylist;
import com.yog.sangeet.util.SangeetAfterTextChangedWatcher;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;


@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    public static ArrayList<AudioFile> audios;
    public static ArrayList<AudioFile> songsListToDisplay;
    public static ArrayList<ArrayList<AudioFile>> songsInsideFolder;
    public static final String TAG = "MainActivity";

    private EditText searchEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Main Activity - On Create");
        setContentView(R.layout.activity_main);
        searchEt = findViewById(R.id.searchEt);
        setTitle("Folders");
        requestPermission();
        setSearchListener();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        requestPermission();
    }

    private void createFolderAndSongsPlaylist() {
        ArrayList<String> folderList = new ArrayList<>();
        songsInsideFolder = new ArrayList<>();
        int indexOfFolderInFolderList = -1;
        for (AudioFile audioFile : audios) {
            String[] splittedMediaFileLocation = audioFile.getMediaFile().split("/");
            String folderName = splittedMediaFileLocation[splittedMediaFileLocation.length - 2];
            AudioFile audio = audioFile;

            if (!folderList.contains(folderName)) {
                folderList.add(folderName);
                indexOfFolderInFolderList = folderList.indexOf(folderName);
                songsInsideFolder.add(new ArrayList<AudioFile>());
                songsInsideFolder.get(indexOfFolderInFolderList).add(audio);
            } else {
                indexOfFolderInFolderList = folderList.indexOf(folderName);
                songsInsideFolder.get(indexOfFolderInFolderList).add(audio);
            }
        }
        displayFolderList(folderList);
    }

    private void displayFolderList(final ArrayList<String> folderList) {
        final RecyclerView recyclerView = findViewById(R.id.list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        RecyclerPlaylist recyclerPlaylist = new RecyclerPlaylist(this, folderList, "folder_view");
        recyclerView.setAdapter(recyclerPlaylist);
    }

    private void loadMedia() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audios = new ArrayList<AudioFile>();
            while (cursor.moveToNext()) {
                String mediaFile = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String mediaTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String mediaArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                int albumId = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                audios.add(new AudioFile(mediaFile, mediaTitle, mediaArtist, albumId));
            }
        } else {
            //means the device is not having any song
            Log.d(TAG, "No Music Found");
        }
        cursor.close();
    }


    private void setSearchListener() {
        Disposable disposable = RxTextView.textChangeEvents(searchEt)
                .debounce(500, TimeUnit.MILLISECONDS)
                .distinctUntilChanged(textViewTextChangeEvent -> {
                    return textViewTextChangeEvent.getText().toString().trim();
                })
                //.filter(textChangedEvent -> !textChangedEvent.getText().toString().trim().isEmpty())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(textChangedEvent -> {
                    Timber.d("Search Query : " + textChangedEvent.getText());
                });
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, READ_MEDIA_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED) {
                loadMedia();
                createFolderAndSongsPlaylist();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                READ_MEDIA_AUDIO
                        }, 1);
            }

            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                POST_NOTIFICATIONS
                        }, 1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                loadMedia();
                createFolderAndSongsPlaylist();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                READ_EXTERNAL_STORAGE
                        }, 1);
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length >= 1) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        loadMedia();
                        createFolderAndSongsPlaylist();
                    } else {
                        requestPermission();
                    }
                }
                break;
        }
    }
}
