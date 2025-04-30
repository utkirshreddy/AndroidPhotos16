package com.example.photos;

import android.content.Context;
import android.util.Log;

import com.example.photos.models.Album;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {
    private static final String TAG = "StorageManager";
    private static final String ALBUMS_FILE = "albums.json";
    private final Context context;
    private final Gson gson;

    public StorageManager(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    public void saveAlbums(List<Album> albums) {
        try {
            File file = new File(context.getFilesDir(), ALBUMS_FILE);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            String json = gson.toJson(albums);
            writer.write(json);
            writer.close();
            Log.d(TAG, "Albums saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving albums", e);
        }
    }

    public List<Album> loadAlbums() {
        List<Album> albums = new ArrayList<>();
        try {
            File file = new File(context.getFilesDir(), ALBUMS_FILE);
            if (!file.exists()) {
                return albums;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            Type type = new TypeToken<List<Album>>(){}.getType();
            albums = gson.fromJson(json.toString(), type);
        } catch (IOException e) {
            Log.e(TAG, "Error loading albums", e);
        }

        return albums != null ? albums : new ArrayList<>();
    }

    public Album getAlbumByID(String id) {
        List<Album> albums = loadAlbums();
        for (Album album : albums) {
            if (album.getId().equals(id)) {
                return album;
            }
        }
        return null;
    }


}