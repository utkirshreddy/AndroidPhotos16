package com.example.photos.models;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model class representing an Album
 * Contains a list of photos and album metadata
 */
public class Album implements Serializable {

    private String id;
    private String name;
    private List<Photo> photos;
    private String thumbnailPath; // Path to the thumbnail image for this album

    // Constructor for new album
    public Album(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.photos = new ArrayList<>();
    }

    // Getters and setters
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

    // Function to add a photo to the album
    /**
     * Function to add a photo to the album
     * @param photo The photo to add
     * @return true if photo was added, false if it was a duplicate
     */
    public boolean addPhoto(Photo photo) {
        // Check if photo already exists in the album

        // log photo
        Log.d("Album", "Adding photo: " + photo.getPath());

        // log  photos in album
        for (Photo p : photos) {
            Log.d("Album", "Photo in album: " + p.getPath());
        }

        if (containsPhoto(photo)) {
            return false;
        }

        // Add the new photo
        photos.add(photo);

        // If this is the first photo, use it as the album thumbnail
        if (photos.size() == 1) {
            thumbnailPath = photo.getPath();
        }

        return true;
    }

    // Function to remove a photo from the album
    public boolean removePhoto(Photo photo) {
        boolean removed = photos.remove(photo);
        // Update thumbnail if necessary
        if (removed && thumbnailPath != null && thumbnailPath.equals(photo.getPath())) {
            updateThumbnail();
        }
        return removed;
    }

    // Function to remove a photo at a specific position
    public Photo removePhoto(int position) {
        if (position >= 0 && position < photos.size()) {
            Photo removed = photos.remove(position);
            // Update thumbnail if necessary
            if (thumbnailPath != null && thumbnailPath.equals(removed.getPath())) {
                updateThumbnail();
            }
            return removed;
        }
        return null;
    }

    // Function to update the album thumbnail
    public void updateThumbnail() {
        if (photos.isEmpty()) {
            thumbnailPath = null;
        } else {
            thumbnailPath = photos.get(0).getPath();
            //print all photos in the album
            for (Photo photo : photos) {
                Log.d("Album", "Photo path: " + photo.getPath());
            }

            // print path
            Log.d("Album", "Updated thumbnail path: " + thumbnailPath);
        }
    }

    public boolean containsPhoto(Photo photo) {
        for (Photo p : photos) {
            if (p.getPath().equals(photo.getPath())) {
                return true;
            }
        }
        return false;
    }

    //
    public boolean containsPhotoWithSameSource(String sourceUri) {
        if (sourceUri == null) return false;

        for (Photo photo : photos) {
            // If this photo was imported from the same source URI
            if (photo.getOriginalSourceUri() != null &&
                    photo.getOriginalSourceUri().equals(sourceUri)) {
                return true;
            }
        }
        return false;
    }

    // get first picture
    /**
     * Returns the first photo in the album to use as thumbnail
     * @return The first photo or null if album is empty
     */
    public Photo getFirstPhoto() {
        if (photos != null && !photos.isEmpty()) {
            return photos.get(0);
        }
        return null;
    }

    // Function to set the album thumbnail
    public void setFirstPhotoAsThumbnail() {
        if (photos != null && !photos.isEmpty()) {
            thumbnailPath = photos.get(0).getPath();
        } else {
            thumbnailPath = null;
        }
    }
}