package com.example.photos;

import android.graphics.Bitmap;

public class Album {
    private String title;
    private Bitmap thumbnail;

    public Album(String title, Bitmap thumbnail) {
        this.title = title;
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }
}