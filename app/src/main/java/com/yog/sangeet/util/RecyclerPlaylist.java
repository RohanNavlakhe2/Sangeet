package com.yog.sangeet.util;

import static com.yog.sangeet.MainActivity.songsInsideFolder;
import static com.yog.sangeet.MusicList.mediaUtil;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.yog.sangeet.AudioFile;
import com.yog.sangeet.MainActivity;
import com.yog.sangeet.MusicList;
import com.yog.sangeet.R;
import com.yog.sangeet.databinding.FolderListXmlBinding;
import com.yog.sangeet.databinding.PlaylistXmlBinding;


import java.util.ArrayList;

public class RecyclerPlaylist extends RecyclerView.Adapter<RecyclerPlaylist.Holder> {
    MusicList musicList;
    MainActivity mainActivity;
    String whichView;
    PlaylistXmlBinding binding;
    FolderListXmlBinding folderBinding;
    ArrayList<AudioFile> songsListToDisplay;
    ArrayList<String> folderList;

    public RecyclerPlaylist(MainActivity mainActivity, ArrayList<String> folderList,String whichView) {
        this.mainActivity=mainActivity;
        this.folderList=folderList;
        this.whichView=whichView;
        //below method call is necessay to avoid item repetition.
        setHasStableIds(true);
    }
    public RecyclerPlaylist(MusicList musicList, ArrayList<AudioFile> songsListToDisplay,String whichView) {
        this.musicList=musicList;
        this.songsListToDisplay=songsListToDisplay;
        this.whichView=whichView;
        //below method call is necessay to avoid item repetition.
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=null;
        View view=null;
        if(whichView.equals("folder_view")) {
            inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            folderBinding=DataBindingUtil.inflate(inflater, R.layout.folder_list_xml,null,false);
            view=folderBinding.getRoot();
        }
        else if(whichView.equals("song_view")) {
            inflater = (LayoutInflater) musicList.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            binding=DataBindingUtil.inflate(inflater,R.layout.playlist_xml,null,false);
            view=binding.getRoot();
        }
        RecyclerView.LayoutParams layoutParams=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);
        return new Holder(view);
       /* return new RecyclerView.ViewHolder(view) {
            @Override
            public String toString() {
                return super.toString();
            }
        };*/
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder,final int position) {
        if(whichView.equals("folder_view")){
          folderBinding.folerName.setText(folderList.get(position));
          folderBinding.folderCard.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  MainActivity.songsListToDisplay=songsInsideFolder.get(position);
                  Intent launchMusicList=new Intent(mainActivity,MusicList.class);
                  launchMusicList.putExtra("folder",folderList.get(position));
                  mainActivity.startActivity(launchMusicList);
              }
          });
        }else if(whichView.equals("song_view")){
            binding.title.setText(songsListToDisplay.get(position).getMediaTitle());
            binding.artist.setText(songsListToDisplay.get(position).getMediaArtist());

            Glide.with(musicList)
                    .load(setImage(songsListToDisplay.get(position).getMediaFile(),65,75))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.i("img_load","failed");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.i("img_load","ready");
                            return false;
                        }
                    })
                    .into(binding.songImage);

           /* binding.songImage.setImageBitmap
                     (setImage(songsListToDisplay.get(position).getMediaFile(),60,75));*/

            binding.audioCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mediaUtil.mediaPlayer!=null)
                        mediaUtil.mediaPlayer.reset();
                    if(MusicList.flag==false){
                        musicList.definingHeightOfTheOfList();
                        MusicList.flag=true;
                    }
                    //here I am reseting the mediaPlayer because in case of playing music more than
                    // one time in the same activity instance previos media should be stopped.If
                    // we dont do so the mediaPlayer which is playing the first song will be
                    // a garbage collect and in this situation we wont have any control over
                    // that media means we cant stop that media to play the new media so
                    // music overlap will happen.
                    mediaUtil.mediaPlayer=new MediaPlayer();
                    MusicList.listenersDuty(songsListToDisplay,position);
                }
            });
        }
    }

     @Override
    public int getItemCount() {
        if(whichView.equals("folder_view"))
           return folderList.size();
        else
         return songsListToDisplay.size();
    }

// the below two methods are necessary to avoid repetition of items.
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static Bitmap setImage(String filePath,int width,int height){

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        byte[] art = retriever.getEmbeddedPicture();

        if( art != null ){
            //Bitmap.createScaledBitmap() is for scaling the image according to our imageview
            //if we dont take this method then it will give OutOfMemoryError because every time
            // when we scroll the list it loads the entire image, image may be 2560*1920 anything
            // that's why oome occurs.
            // with the help of this method here before loading the entire image we are
            // down scaling it to 60*75.
             return  Bitmap.createScaledBitmap(
                    BitmapFactory.decodeByteArray(art, 0, art.length),width,height,true);
        }
        else{
            return BitmapFactory.decodeResource(Resources.getSystem(),R.drawable.music);//not working
            //image.setImageResource(R.drawable.music);
        }
    }

    static class Holder extends RecyclerView.ViewHolder{

        public Holder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
