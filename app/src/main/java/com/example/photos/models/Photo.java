package com.example.photos.models;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.photos.StorageManager;
import com.example.photos.PhotosApplication;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Photo implements Serializable {
    private String id;
    private String path; // URI path to the photo
    private String title; // Title/caption (file name by default)
    private Map<String, List<String>> tags; // Maps tag types to lists of tag values

    // Tag types
    public static final String TAG_PERSON = "person";
    public static final String TAG_LOCATION = "location";

    private String originalSourceUri; // Store the original source URI

    private String albumId; // ID of the album this photo belongs to

    // Add getter/setter


    // Constructor for new photo
    public Photo(String path, String title, String albumId) {
        this.id = UUID.randomUUID().toString();
        this.path = path;
        this.title = title; // Default title
        this.tags = new HashMap<>();
        this.tags.put(TAG_PERSON, new ArrayList<>());
        this.tags.put(TAG_LOCATION, new ArrayList<>());
        this.originalSourceUri = path; // Store the original source URI
        this.albumId = albumId; // Set the album ID
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbumId() {
        return albumId;
    }
    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    // get photo file extension from title
    public String getFileExtension() {
        if (title == null || title.isEmpty()) {
            return "jpg"; // Default extension
        }
        int lastDot = title.lastIndexOf('.');
        if (lastDot == -1 || lastDot == title.length() - 1) {
            return "jpg"; // No extension found, default to jpg
        }
        String extension = title.substring(lastDot + 1).toLowerCase();

        // Validate that it's one of our supported extensions
        if (extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("png") || extension.equals("bmp") ||
                extension.equals("gif")) {
            return extension;
        }

        // If not a supported extension, use jpg as default
        return "jpg";
    }

    public String getAlbumName() {
        // Use albumID to get album name
        Log.d("PHOTOS", "getAlbumName: " + albumId);
        if (albumId == null || albumId.isEmpty()) {
            Log.d("Photo", "Album ID is null or empty");
            return "Unknown Album";
        }

        try {
            // Get application context through a static reference or singleton
            Context context = PhotosApplication.getAppContext();
            StorageManager storageManager = new StorageManager(context);
            Album album = storageManager.getAlbumByID(albumId);
            if (album == null) {
                Log.d("Photo", "Album not found for ID: " + albumId);
                return "Unknown Album";
            }
            String name = album.getName();
            if (name == null || name.isEmpty()) {
                Log.d("Photo", "Album name is null or empty for ID: " + albumId);
                return "Unknown Album";
            } else {
                return name;
            }
        } catch (Exception e) {
            Log.d("Photo", "Error retrieving album name for ID: " + albumId, e);
            return "Unknown Album";
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, List<String>> getAllTags() {
        return tags;
    }

    // Function to get all tags of a specific type
    public List<String> getTagsOfType(String tagType) {
        return tags.getOrDefault(tagType, new ArrayList<>());
    }

    public String getOriginalSourceUri() {
        return originalSourceUri;
    }

    public void setOriginalSourceUri(String originalSourceUri) {
        this.originalSourceUri = originalSourceUri;
    }


    // Function to add a tag
    public boolean addTag(String tagType, String tagValue) {
        if (!isValidTagType(tagType) || tagValue == null || tagValue.trim().isEmpty()) {
            return false;
        }

        // Normalize tag value (lowercase for case-insensitive comparison)
        String normalizedValue = tagValue.trim().toLowerCase();

        // Get tags list for this type
        List<String> typeTags = tags.get(tagType);

        // For location tags, replace existing tag
        if (TAG_LOCATION.equals(tagType)) {
            // Clear existing location tags
            typeTags.clear();
            // Add the new location tag
            typeTags.add(tagValue.trim());
            return true;
        }
        // For person tags, check for duplicates then add
        else {
            // Check if tag already exists
            for (String existingTag : typeTags) {
                if (existingTag.toLowerCase().equals(normalizedValue)) {
                    return false; // Tag already exists
                }
            }

            // Add the new tag
            typeTags.add(tagValue.trim());
            return true;
        }
    }

    // Function to remove a tag
    public boolean removeTag(String tagType, String tagValue) {
        if (!isValidTagType(tagType) || tagValue == null) {
            return false;
        }

        // Get tags list for this type
        List<String> typeTags = tags.get(tagType);

        // Find and remove the tag (case-insensitive)
        for (int i = 0; i < typeTags.size(); i++) {
            if (typeTags.get(i).equalsIgnoreCase(tagValue)) {
                typeTags.remove(i);
                return true;
            }
        }

        return false; // Tag not found
    }

    // Function to check if tag type is valid
    private boolean isValidTagType(String tagType) {
        return TAG_PERSON.equals(tagType) || TAG_LOCATION.equals(tagType);
    }

    // Function to check if photo has tag
    public boolean hasTag(String tagType, String tagValue) {
        if (!isValidTagType(tagType) || tagValue == null) {
            return false;
        }

        // Get tags list for this type
        List<String> typeTags = tags.get(tagType);

        // Check for tag (case-insensitive)
        for (String existingTag : typeTags) {
            if (existingTag.equalsIgnoreCase(tagValue)) {
                return true;
            }
        }

        return false;
    }

    // Function to check if photo has tag with prefix
    public boolean hasTagWithPrefix(String tagType, String prefix) {
        if (!isValidTagType(tagType) || prefix == null) {
            return false;
        }

        // Get tags list for this type
        List<String> typeTags = tags.get(tagType);

        // Check for tag starting with prefix (case-insensitive)
        for (String existingTag : typeTags) {
            if (existingTag.toLowerCase().startsWith(prefix.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}