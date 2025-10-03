package com.example.lovai.Model;

public class Moment {
    private String id;
    private String title;
    private String date;
    private int mediaCount;
    private String coverImageUrl;

    public Moment(String id, String title, String date, int mediaCount, String coverImageUrl) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.mediaCount = mediaCount;
        this.coverImageUrl = coverImageUrl;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }


    public int getMediaCount() {
        return mediaCount;
    }


    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setId(String id) {
        this.id = id;
    }


}
