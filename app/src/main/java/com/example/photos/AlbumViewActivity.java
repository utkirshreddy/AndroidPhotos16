package com.example.photos;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos.models.Album;
import com.example.photos.models.Photo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AlbumViewActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView photosRecyclerView;
    private View emptyAlbumView;
    private FloatingActionButton fabAddPhoto;
    private FloatingActionButton fabPhotoOptions;

    private Album currentAlbum;
    private List<Photo> photos;
    private PhotoAdapter photoAdapter;
    private StorageManager storageManager;
    private List<Album> allAlbums;

    private static final int REQUEST_PHOTO_VIEW = 1;
    private static final int REQUEST_MOVE_PHOTO = 2;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    addPhotoFromUri(uri);
                }
            });

    // AI Mixed
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        toolbar = findViewById(R.id.album_toolbar);
        photosRecyclerView = findViewById(R.id.photos_recycler_view);
        emptyAlbumView = findViewById(R.id.empty_album_view);
        fabAddPhoto = findViewById(R.id.fab_add_photo);
        fabPhotoOptions = findViewById(R.id.fab_photo_options);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        storageManager = new StorageManager(this);
        allAlbums = storageManager.loadAlbums();

        String albumId = getIntent().getStringExtra("ALBUM_ID");
        if (albumId != null) {
            for (Album album : allAlbums) {
                if (album.getId().equals(albumId)) {
                    currentAlbum = album;
                    break;
                }
            }
        }

        if (currentAlbum == null) {
            String albumName = getIntent().getStringExtra("ALBUM_NAME");
            if (albumName == null) albumName = "Untitled Album";
            currentAlbum = new Album(albumName);
            allAlbums.add(currentAlbum);
        }

        toolbar.setTitle(currentAlbum.getName());

        photos = currentAlbum.getPhotos();
        photoAdapter = new PhotoAdapter(this, photos);


        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        photosRecyclerView.setAdapter(photoAdapter);

        photoAdapter.setOnPhotoClickListener(this::openPhotoView);


        photoAdapter.setOnPhotoLongClickListener(position -> {
            showPhotoOptions(position);
            return true;
        });
        fabAddPhoto.setOnClickListener(v -> launchGalleryPicker());

        fabPhotoOptions.setVisibility(View.GONE);

        updateUI();
    }

    // AI
    private void showPhotoOptions(int position) {
        PopupMenu popup = new PopupMenu(this, photosRecyclerView.findViewHolderForAdapterPosition(position).itemView);
        popup.getMenuInflater().inflate(R.menu.photo_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_delete_photo) {
                confirmDeletePhoto(position);
                return true;
            } else if (itemId == R.id.action_move_photo) {
                showMovePhotoDialog(position);
                return true;

            } else if (itemId == R.id.action_save_to_gallery) {

                Photo photoToSave = photos.get(position);
                savePhotoToGallery(photoToSave);
                return true;
            } else if (itemId == R.id.action_add_tag) {

                openPhotoView(position);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void confirmDeletePhoto(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deletePhoto(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void deletePhoto(int position) {
        if (position >= 0 && position < photos.size()) {

            if (position == 0 && photos.size() > 1) {
                currentAlbum.setThumbnailPath(photos.get(1).getPath());

            } else if (photos.size() == 1) {
                currentAlbum.setThumbnailPath(null);
            }

            Photo removedPhoto = currentAlbum.removePhoto(position);

            photoAdapter.notifyItemRemoved(position);
            saveChanges();

            Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
        }
    }


    // AI Mixed
    private void showMovePhotoDialog(int photoPosition) {
        if (allAlbums.size() <= 1) {
            Toast.makeText(this, "No other albums available to move to", Toast.LENGTH_SHORT).show();
            return;
        }


        Photo photoToMove = photos.get(photoPosition);


        List<Album> targetAlbums = new ArrayList<>();
        for (Album album : allAlbums) {
            if (!album.getId().equals(currentAlbum.getId())) {
                targetAlbums.add(album);
            }
        }

        if (targetAlbums.isEmpty()) {
            Toast.makeText(this, "No other albums available to move to", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_move_photo, null);
        builder.setView(dialogView);

        RecyclerView albumSelectionRecycler = dialogView.findViewById(R.id.album_selection_recycler);
        TextView noAlbumsText = dialogView.findViewById(R.id.no_albums_text);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_move);

        albumSelectionRecycler.setLayoutManager(new LinearLayoutManager(this));
        AlbumSelectionAdapter adapter = new AlbumSelectionAdapter(targetAlbums);

        final AlertDialog dialog = builder.create();

        adapter.setOnAlbumSelectedListener(selectedAlbum -> {
            if (photoExistsInAlbum(photoToMove, selectedAlbum)) {
                Toast.makeText(AlbumViewActivity.this,
                        "Photo already exists in " + selectedAlbum.getName(),
                        Toast.LENGTH_SHORT).show();
            } else {
                movePhoto(photoPosition, selectedAlbum);
                dialog.dismiss();
            }
        });

        albumSelectionRecycler.setAdapter(adapter);

        if (targetAlbums.isEmpty()) {
            albumSelectionRecycler.setVisibility(View.GONE);
            noAlbumsText.setVisibility(View.VISIBLE);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private boolean photoExistsInAlbum(Photo photo, Album album) {
        for (Photo p : album.getPhotos()) {
            if (p.getOriginalSourceUri().equals(photo.getOriginalSourceUri())) {
                return true;
            }
        }
        return false;
    }

    // AI
    private void movePhoto(int position, Album targetAlbum) {

        Photo photoToMove = photos.get(position);


        targetAlbum.addPhoto(photoToMove);


        Photo removedPhoto = currentAlbum.removePhoto(position);


        photoAdapter.notifyItemRemoved(position);
        updateUI();
        saveChanges();

        Toast.makeText(this, "Photo moved to " + targetAlbum.getName(), Toast.LENGTH_SHORT).show();
    }



    // AI
    private void updateUI() {
        if (photos.isEmpty()) {
            photosRecyclerView.setVisibility(View.GONE);
            emptyAlbumView.setVisibility(View.VISIBLE);
        } else {
            photosRecyclerView.setVisibility(View.VISIBLE);
            emptyAlbumView.setVisibility(View.GONE);
        }
    }

    private void launchGalleryPicker() {
        getContent.launch("image/*");
    }

    // AI Mixed
    private void addPhotoFromUri(Uri uri) {
        try {

            String fileName = getOriginalFileName(uri);
            Uri savedUri = saveImageToAppStorage(uri);


            if (savedUri != null) {

                String sourceUri = uri.toString();

                if (currentAlbum.containsPhotoWithSameSource(sourceUri)) {
                    Toast.makeText(this, "This photo already exists in the album", Toast.LENGTH_SHORT).show();
                    return;
                }


                Photo newPhoto = new Photo(savedUri.toString(), fileName, currentAlbum.getId());
                newPhoto.setOriginalSourceUri(sourceUri);

                boolean added = currentAlbum.addPhoto(newPhoto);

                photoAdapter.notifyItemInserted(photos.size() - 1);
                updateUI();
                saveChanges();

            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String getOriginalFileName(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } else {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }


    // AI
    private Uri saveImageToAppStorage(Uri sourceUri) {
        try {

            String fileName = "photo_" + System.currentTimeMillis() + ".jpg";

            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return null;

            File outputDir = getApplicationContext().getFilesDir();
            File outputFile = new File(outputDir, fileName);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return Uri.fromFile(outputFile);
        } catch (Exception e) {
            Log.d("AlbumViewActivity", "Error saving image", e);
            return null;
        }
    }

    // AI Mixed
    private void savePhotoToGallery(Photo currentPhoto) {
        Uri photoUri = Uri.parse(currentPhoto.getPath());

        try {
            String fileExtension = currentPhoto.getFileExtension();

            String mimeType;

            switch (fileExtension.toLowerCase()) {
                case "png":
                    mimeType = "image/png";
                    break;
                case "bmp":
                    mimeType = "image/bmp";
                    break;
                case "gif":
                    mimeType = "image/gif";
                    break;
                default:
                    mimeType = "image/jpeg";
            }


            InputStream inputStream = getContentResolver().openInputStream(photoUri);
            if (inputStream == null) {
                Toast.makeText(this, "Failed to access photo", Toast.LENGTH_SHORT).show();
                return;
            }


            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, currentPhoto.getTitle() + "." + fileExtension);
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);


            Uri insertUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (insertUri != null) {
                try (OutputStream out = getContentResolver().openOutputStream(insertUri)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    Toast.makeText(this, "Photo saved to gallery", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show();
                    Log.e("PhotoViewActivity", "Error saving photo", e);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to access photo", Toast.LENGTH_SHORT).show();
            Log.e("PhotoViewActivity", "Error accessing photo", e);
        }
    }

    // AI
    private void openPhotoView(int position) {
        if (position >= 0 && position < photos.size()) {
            Intent intent = new Intent(this, PhotoViewActivity.class);
            intent.putExtra(PhotoViewActivity.EXTRA_ALBUM_PHOTOS, new ArrayList<>(photos));
            intent.putExtra(PhotoViewActivity.EXTRA_PHOTO_POSITION, position);
            startActivityForResult(intent, REQUEST_PHOTO_VIEW);
        }
    }

    private void saveChanges() {
        storageManager.saveAlbums(allAlbums);
    }


    // AI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PHOTO_VIEW && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("DELETED_POSITION")) {
                int position = data.getIntExtra("DELETED_POSITION", -1);
                if (position != -1) {
                    deletePhoto(position);
                }
            }
            else if (data.hasExtra("UPDATED_PHOTO") && data.hasExtra("UPDATED_POSITION")) {
                Photo updatedPhoto = (Photo) data.getSerializableExtra("UPDATED_PHOTO");
                int position = data.getIntExtra("UPDATED_POSITION", -1);
                if (updatedPhoto != null && position >= 0 && position < photos.size()) {
                    photos.set(position, updatedPhoto);
                    photoAdapter.notifyItemChanged(position);
                    saveChanges();
                }
            }
        }
    }




    @Override
    public void onBackPressed() {

        super.onBackPressed();
        finish();

    }
}