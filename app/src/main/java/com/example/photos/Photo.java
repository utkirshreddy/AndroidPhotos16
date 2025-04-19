package com.example.photos;

import android.graphics.Bitmap;

public class Photo {
    private String title;
    private Bitmap image;

    public Photo(String title, Bitmap image) {
        this.title = title;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public Bitmap getImage() {
        return image;
    }
}