package com.example.photos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos.models.Album;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView albumsRecyclerView;
    private AlbumAdapter albumAdapter;
    private List<Album> albums;
    private View emptyView;
    private StorageManager storageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize storage manager
        storageManager = new StorageManager(this);

        // Initialize views
        albumsRecyclerView = findViewById(R.id.albums_recycler_view);
        emptyView = findViewById(R.id.empty_view);
        FloatingActionButton fabCreateAlbum = findViewById(R.id.fab_create_album);

        // Load existing albums
        albums = storageManager.loadAlbums();

        // Setup RecyclerView
        albumAdapter = new AlbumAdapter(this, albums);
        albumsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        albumsRecyclerView.setAdapter(albumAdapter);

        // Update UI based on albums list
        updateEmptyView();

        // Set FAB click listener
        fabCreateAlbum.setOnClickListener(v -> showCreateAlbumDialog());

        // Set click listener for album items
        albumAdapter.setOnAlbumClickListener(position -> {
            Album album = albums.get(position);
            Intent intent = new Intent(MainActivity.this, AlbumViewActivity.class);
            intent.putExtra("ALBUM_ID", album.getId());
            intent.putExtra("ALBUM_NAME", album.getName());
            startActivity(intent);
        });

        // Set options click listener
        albumAdapter.setOnOptionsClickListener(position -> {
            showAlbumOptionsMenu(position);
        });
    }

    private void updateEmptyView() {
        if (albums.isEmpty()) {
            albumsRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            albumsRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showCreateAlbumDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_album, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        EditText albumNameInput = dialogView.findViewById(R.id.album_name_input);

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_create).setOnClickListener(v -> {
            String albumName = albumNameInput.getText().toString().trim();
            if (albumName.isEmpty()) {
                Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }



            // Create new album
            Album newAlbum = new Album(albumName);
            albums.add(newAlbum);

            // Save albums to storage
            storageManager.saveAlbums(albums);

            // Notify adapter about the new album
            albumAdapter.setAlbums(albums);

            // Update UI
            albumAdapter.notifyItemInserted(albums.size() - 1);
            updateEmptyView();

            Toast.makeText(this, "Album created: " + albumName, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showAlbumOptionsMenu(int position) {
        Album album = albums.get(position);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rename_album, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Album Options")
                .setItems(new String[]{"Rename Album", "Delete Album"}, (dialog1, which) -> {
                    if (which == 0) {
                        showRenameAlbumDialog(position);
                    } else if (which == 1) {
                        showDeleteAlbumConfirmation(position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    private void showRenameAlbumDialog(int position) {
        Album album = albums.get(position);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rename_album, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        EditText newNameInput = dialogView.findViewById(R.id.new_album_name_input);


        newNameInput.setText(album.getName());



        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_rename).setOnClickListener(v -> {
            String newName = newNameInput.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean nameExists = false;
            for (Album a : albums) {
                if (a.getName().equalsIgnoreCase(newName) && !a.getId().equals(album.getId())) {
                    nameExists = true;
                    break;
                }
            }
            if (nameExists) {
                Toast.makeText(this, "Album name already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            album.setName(newName);
            storageManager.saveAlbums(albums);
            // Notify adapter about the change
            albumAdapter.setAlbums(albums);
            albumAdapter.notifyItemChanged(position);
            Toast.makeText(this, "Album renamed", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteAlbumConfirmation(int position) {
        Album album = albums.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Delete Album")
                .setMessage("Are you sure you want to delete the album '" + album.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    albums.remove(position);
                    storageManager.saveAlbums(albums);
                    // Notify adapter about the removed album
                    albumAdapter.setAlbums(albums);
                    albumAdapter.notifyItemRemoved(position);
                    updateEmptyView();
                    Toast.makeText(this, "Album deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu == null) {
            return false;
        }
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("ALBUM_LIST", new ArrayList<>(albums));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Reload albums from storage to get latest changes
        albums = storageManager.loadAlbums();

        // Update the adapter with the new list
        albumAdapter.setAlbums(albums);

        // Notify the adapter about the refreshed data
        albumAdapter.notifyDataSetChanged();

        // Update UI based on albums list
        updateEmptyView();

        // Log all album thumbnails
        if (albums != null && !albums.isEmpty()) {
            Log.d("AlbumDashboard", "--- Album Thumbnails ---");
            for (int i = 0; i < albums.size(); i++) {
                Album album = albums.get(i);
                Log.d("TESTINGTESINGAlbumDashboard", "Album: " + album.getName() +
                        ", Thumbnail: " + album.getThumbnailPath());
            }
            Log.d("AlbumDashboard", "------------------------");
        }

//        // Update album thumbnails
//        for (Album album : albums) {
//            try {
//                album.updateThumbnail();
//            } catch (Exception e) {
//                Log.e("MainActivity", "Error updating thumbnail for album: " + album.getName(), e);
//            }
//        }
    }
}