package com.yog.sangeet;

public class AudioFile {
    String mediaFile;
    String mediaTitle;
    String mediaArtist;
    int albumId;

    public AudioFile(String mediaFile, String mediaTitle, String mediaArtist,int albumId) {
        this.mediaFile=mediaFile;
        this.mediaTitle=mediaTitle;
        this.mediaArtist=mediaArtist;
        this.albumId=albumId;
    }
    public String getMediaTitle() {
        return mediaTitle;
    }
    public String getMediaFile() {
        return mediaFile;
    }
    public String getMediaArtist() {
        return mediaArtist;
    }
}
