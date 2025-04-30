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
    private String path;
    private String title;
    private Map<String, List<String>> tags;

    public static final String TAG_PERSON = "person";
    public static final String TAG_LOCATION = "location";

    private String originalSourceUri;

    private String albumId;


    public Photo(String path, String title, String albumId) {
        this.id = UUID.randomUUID().toString();
        this.path = path;
        this.title = title;
        this.tags = new HashMap<>();
        this.tags.put(TAG_PERSON, new ArrayList<>());
        this.tags.put(TAG_LOCATION, new ArrayList<>());
        this.originalSourceUri = path;
        this.albumId = albumId;
    }


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

    // AI
    public String getFileExtension() {
        if (title == null || title.isEmpty()) {
            return "jpg";
        }
        int lastDot = title.lastIndexOf('.');
        if (lastDot == -1 || lastDot == title.length() - 1) {
            return "jpg";
        }
        String extension = title.substring(lastDot + 1).toLowerCase();

        if (extension.equals("jpg") || extension.equals("jpeg") ||
                extension.equals("png") || extension.equals("bmp") ||
                extension.equals("gif")) {
            return extension;
        }

        return "jpg";
    }

    // AI
    public String getAlbumName() {
        if (albumId == null || albumId.isEmpty()) {
            return "Unknown Album";
        }

        try {
            Context context = PhotosApplication.getAppContext();
            StorageManager storageManager = new StorageManager(context);
            Album album = storageManager.getAlbumByID(albumId);
            if (album == null) {
                return "Unknown Album";
            }
            String name = album.getName();
            if (name == null || name.isEmpty()) {
                return "Unknown Album";
            } else {
                return name;
            }
        } catch (Exception e) {
            return "Unknown Album";
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, List<String>> getAllTags() {
        return tags;
    }

    public List<String> getTagsOfType(String tagType) {
        return tags.getOrDefault(tagType, new ArrayList<>());
    }

    public String getOriginalSourceUri() {
        return originalSourceUri;
    }

    public void setOriginalSourceUri(String originalSourceUri) {
        this.originalSourceUri = originalSourceUri;
    }


    // AI Mixed
    public boolean addTag(String tagType, String tagValue) {
        if (!isValidTagType(tagType) || tagValue == null || tagValue.trim().isEmpty()) {
            return false;
        }
        String normalizedValue = tagValue.trim().toLowerCase();

        List<String> typeTags = tags.get(tagType);

        if (TAG_LOCATION.equals(tagType)) {
            typeTags.clear();
            typeTags.add(tagValue.trim());
            return true;
        }
        else {
            for (String existingTag : typeTags) {
                if (existingTag.toLowerCase().equals(normalizedValue)) {
                    return false;
                }
            }

            typeTags.add(tagValue.trim());
            return true;
        }
    }

    public boolean removeTag(String tagType, String tagValue) {
        if (!isValidTagType(tagType) || tagValue == null) {
            return false;
        }

        List<String> typeTags = tags.get(tagType);

        for (int i = 0; i < typeTags.size(); i++) {
            if (typeTags.get(i).equalsIgnoreCase(tagValue)) {
                typeTags.remove(i);
                return true;
            }
        }

        return false;
    }


    private boolean isValidTagType(String tagType) {
        return TAG_PERSON.equals(tagType) || TAG_LOCATION.equals(tagType);
    }

    public boolean hasTag(String tagType, String tagValue) {
        boolean isValidType = isValidTagType(tagType);
        if (!isValidType || tagValue == null) {
            return false;
        }
        List<String> tagsForType = tags.get(tagType);

        for (int i = 0; i < tagsForType.size(); i++) {
            String currentTag = tagsForType.get(i);


            if (currentTag.equalsIgnoreCase(tagValue)) {
                return true;
            }
        }
        return false;
    }
}