package com.example.photos.models;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Album implements Serializable {

    private String id;
    private String name;
    private List<Photo> photos;
    private String thumbnailPath;

    public Album(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.photos = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }


    public boolean addPhoto(Photo photo) {
        if (containsPhoto(photo)) {
            return false;
        }
        photos.add(photo);
        if (photos.size() == 1) {
            thumbnailPath = photo.getPath();
        }
        return true;
    }

    public Photo removePhoto(int position) {
        if (position < 0) {
            return null;
        }

        if (position >= photos.size()) {
            return null;
        }

        Photo removedPhoto = photos.get(position);
        photos.remove(position);

        if (thumbnailPath != null) {
            if (thumbnailPath.equals(removedPhoto.getPath())) {
                updateThumbnail();
            }
        }

        return removedPhoto;
    }

    public void updateThumbnail() {
        if (photos.isEmpty()) {
            thumbnailPath = null;
        } else {
            thumbnailPath = photos.get(0).getPath();
        }
    }

    public boolean containsPhoto(Photo photo) {
        String photoPath = photo.getPath();

        for (Photo p : photos) {
            String comparingPhoto = p.getPath();

            if (comparingPhoto.equals(photoPath)) {
                return true;
            }
        }
        return false;
    }


    public boolean containsPhotoWithSameSource(String sourceUri) {
        if (sourceUri == null) return false;

        for (Photo photo : photos) {
            String comparingUri = photo.getOriginalSourceUri();

            if (comparingUri != null &&
                    comparingUri.equals(sourceUri)) {
                return true;
            }
        }
        return false;
    }
}