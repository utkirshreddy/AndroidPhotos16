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

    // UI Components
    private Toolbar toolbar;
    private RecyclerView photosRecyclerView;
    private View emptyAlbumView;
    private FloatingActionButton fabAddPhoto;
    private FloatingActionButton fabPhotoOptions;

    // Data
    private Album currentAlbum;
    private List<Photo> photos;
    private PhotoAdapter photoAdapter;
    private StorageManager storageManager;
    private List<Album> allAlbums;

    // Constants for activity results
    private static final int REQUEST_PHOTO_VIEW = 1;
    private static final int REQUEST_MOVE_PHOTO = 2;

    // Activity result launcher for selecting images
    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    addPhotoFromUri(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        // Initialize UI components
        toolbar = findViewById(R.id.album_toolbar);
        photosRecyclerView = findViewById(R.id.photos_recycler_view);
        emptyAlbumView = findViewById(R.id.empty_album_view);
        fabAddPhoto = findViewById(R.id.fab_add_photo);
        fabPhotoOptions = findViewById(R.id.fab_photo_options);

        // Set up toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize storage manager
        storageManager = new StorageManager(this);
        allAlbums = storageManager.loadAlbums();

        // Get album from intent
        String albumId = getIntent().getStringExtra("ALBUM_ID");
        if (albumId != null) {
            for (Album album : allAlbums) {
                if (album.getId().equals(albumId)) {
                    currentAlbum = album;
                    break;
                }
            }
        }

        // If album not found, create a new one
        if (currentAlbum == null) {
            String albumName = getIntent().getStringExtra("ALBUM_NAME");
            if (albumName == null) albumName = "Untitled Album";
            currentAlbum = new Album(albumName);
            allAlbums.add(currentAlbum);
        }

        // Set up toolbar title
        toolbar.setTitle(currentAlbum.getName());

        // Set up recycler view
        photos = currentAlbum.getPhotos();
        photoAdapter = new PhotoAdapter(this, photos);
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        photosRecyclerView.setAdapter(photoAdapter);

        // Set click listener for photos
        photoAdapter.setOnPhotoClickListener(this::openPhotoView);

        // Set long click listener for photos
// Replace the method reference with a lambda expression that properly handles the return value
        photoAdapter.setOnPhotoLongClickListener(position -> {
            showPhotoOptions(position);
            return true; // Indicate that the long click was consumed
        });
        // Set click listener for add photo button
        fabAddPhoto.setOnClickListener(v -> launchGalleryPicker());

        // Hide photo options FAB (we'll use long press instead)
        fabPhotoOptions.setVisibility(View.GONE);

        // Update UI
        updateUI();
    }

    // Function to show photo options when long-pressed
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
                // Save photo to gallery
                Photo photoToSave = photos.get(position);
                savePhotoToGallery(photoToSave);
                return true;
            } else if (itemId == R.id.action_add_tag) {
                // This will be handled in PhotoViewActivity
                openPhotoView(position);
                return true;
            }
            return false;
        });

        popup.show();
    }
    // Function to confirm photo deletion
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

    // Function to delete a photo
    private void deletePhoto(int position) {
        if (position >= 0 && position < photos.size()) {

            // if the photo is the first one, set the thumbnail to the next photo
            if (position == 0 && photos.size() > 1) {
                currentAlbum.setThumbnailPath(photos.get(1).getPath());

                //print currentAlbum thumbnail
                Log.d("AlbumViewActivity", "Current Album Thumbnail: " + currentAlbum.getThumbnailPath());

            } else if (photos.size() == 1) {
                currentAlbum.setThumbnailPath(null);
            }

            currentAlbum.removePhoto(position);
            // Update the album thumbnail (set it to the first photo or placeholder)
            //currentAlbum.setFirstPhotoAsThumbnail();
            photoAdapter.notifyItemRemoved(position);
            saveChanges();

            //print photos in album
            for (Photo photo : currentAlbum.getPhotos()) {
                Log.d("AlbumViewActivity", "Photo path: " + photo.getPath());
            }

            Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
        }
    }

// Add these methods to AlbumViewActivity.java

    // Function to show album selection dialog for moving a photo
    private void showMovePhotoDialog(int photoPosition) {
        // Check if there are other albums to move to
        if (allAlbums.size() <= 1) {
            Toast.makeText(this, "No other albums available to move to", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the photo to move
        Photo photoToMove = photos.get(photoPosition);



        // Create a list of albums excluding the current one
        List<Album> targetAlbums = new ArrayList<>();
        for (Album album : allAlbums) {
            if (!album.getId().equals(currentAlbum.getId())) {
                targetAlbums.add(album);
            }
        }

        // If no other albums, show a message
        if (targetAlbums.isEmpty()) {
            Toast.makeText(this, "No other albums available to move to", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_move_photo, null);
        builder.setView(dialogView);

        // Set up recycler view
        RecyclerView albumSelectionRecycler = dialogView.findViewById(R.id.album_selection_recycler);
        TextView noAlbumsText = dialogView.findViewById(R.id.no_albums_text);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel_move);

        albumSelectionRecycler.setLayoutManager(new LinearLayoutManager(this));
        AlbumSelectionAdapter adapter = new AlbumSelectionAdapter(targetAlbums);

        // Create and show dialog
        final AlertDialog dialog = builder.create();

        // Set up album selection
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

        // Show no albums text if needed
        if (targetAlbums.isEmpty()) {
            albumSelectionRecycler.setVisibility(View.GONE);
            noAlbumsText.setVisibility(View.VISIBLE);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Function to check if a photo exists in an album
    private boolean photoExistsInAlbum(Photo photo, Album album) {
        // Check if the photo already exists in the target album
        for (Photo p : album.getPhotos()) {
            if (p.getOriginalSourceUri().equals(photo.getOriginalSourceUri())) {
                return true;
            }
        }
        return false;
    }

    // Function to move a photo from the current album to another album
    private void movePhoto(int position, Album targetAlbum) {
        // Get the photo to move
        Photo photoToMove = photos.get(position);

        // Add to target album
        targetAlbum.addPhoto(photoToMove);

        // Remove from current album
        currentAlbum.removePhoto(position);

        // Update UI
        photoAdapter.notifyItemRemoved(position);
        updateUI();
        saveChanges();

        Toast.makeText(this, "Photo moved to " + targetAlbum.getName(), Toast.LENGTH_SHORT).show();
    }




    // Function to update UI based on album content
    private void updateUI() {
        if (photos.isEmpty()) {
            photosRecyclerView.setVisibility(View.GONE);
            emptyAlbumView.setVisibility(View.VISIBLE);
        } else {
            photosRecyclerView.setVisibility(View.VISIBLE);
            emptyAlbumView.setVisibility(View.GONE);
        }
    }

    // Function to launch gallery picker
    private void launchGalleryPicker() {
        getContent.launch("image/*");
    }

    // Function to add photo from URI
    private void addPhotoFromUri(Uri uri) {
        try {

            // Get the original file name
            String fileName = getOriginalFileName(uri);
            // Save the image to the app's storage right when it's selected
            Uri savedUri = saveImageToAppStorage(uri);


            if (savedUri != null) {
                // Create a new photo with the saved URI
                // In the method where you add photos to the album
                String sourceUri = uri.toString();

                if (currentAlbum.containsPhotoWithSameSource(sourceUri)) {
                    Toast.makeText(this, "This photo already exists in the album", Toast.LENGTH_SHORT).show();
                    return;
                }

// When creating a new Photo object

                Photo newPhoto = new Photo(savedUri.toString(), fileName, currentAlbum.getId());
                // log album id
                Log.d("AlbumViewActivity", "Album ID: " + currentAlbum.getId());
                newPhoto.setOriginalSourceUri(sourceUri); // Store the original source URI

                boolean added = currentAlbum.addPhoto(newPhoto);

                // Update UI
                photoAdapter.notifyItemInserted(photos.size() - 1);
                updateUI();
                saveChanges();

                if (added) {
                    Log.d("AlbumViewActivity", "Photo added: " + newPhoto.getPath());
                } else {
                    Log.d("AlbumViewActivity", "Photo already exists: " + newPhoto.getPath());
                    Log.d("AlbumViewActivity", "Photo already exists: " + newPhoto.getPath());
                }
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

    // Add this method to AlbumViewActivity
    private Uri saveImageToAppStorage(Uri sourceUri) {
        try {
            // Create a unique filename
            String fileName = "photo_" + System.currentTimeMillis() + ".jpg";

            // Get input stream from the content URI
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return null;

            // Create output file in app's files directory
            File outputDir = getApplicationContext().getFilesDir();
            File outputFile = new File(outputDir, fileName);

            // Copy the file
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            // Return the new URI that points to your app's private storage
            return Uri.fromFile(outputFile);
        } catch (Exception e) {
            Log.d("AlbumViewActivity", "Error saving image", e);
            return null;
        }
    }

    // Add this method to AlbumViewActivity class
    private void savePhotoToGallery(Photo currentPhoto) {
        Uri photoUri = Uri.parse(currentPhoto.getPath());

        try {
            String fileExtension = currentPhoto.getFileExtension();
            //log the fileextension
            Log.d("PhotoViewActivity", "File extension: " + fileExtension);
            String mimeType;

            // Determine MIME type from extension
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
                    mimeType = "image/jpeg"; // Default to JPEG
            }

            Log.d("PhotoViewActivity", "Saving photo with MIME type: " + mimeType);

            // Get input stream from the source URI
            InputStream inputStream = getContentResolver().openInputStream(photoUri);
            if (inputStream == null) {
                Toast.makeText(this, "Failed to access photo", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create values for the new image
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, currentPhoto.getTitle() + "." + fileExtension);
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            // Insert the image
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



    // Function to open photo view
    private void openPhotoView(int position) {
        if (position >= 0 && position < photos.size()) {
            Intent intent = new Intent(this, PhotoViewActivity.class);
            intent.putExtra(PhotoViewActivity.EXTRA_ALBUM_PHOTOS, new ArrayList<>(photos));
            intent.putExtra(PhotoViewActivity.EXTRA_PHOTO_POSITION, position);
            startActivityForResult(intent, REQUEST_PHOTO_VIEW);
        }
    }

    // Function to save changes to storage
    private void saveChanges() {
        storageManager.saveAlbums(allAlbums);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PHOTO_VIEW && resultCode == RESULT_OK && data != null) {
            // Handle photo deletion from PhotoViewActivity
            if (data.hasExtra("DELETED_POSITION")) {
                int position = data.getIntExtra("DELETED_POSITION", -1);
                if (position != -1) {
                    deletePhoto(position);
                }
            }
            // Handle photo updates from PhotoViewActivity
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



    // pressing back button
    @Override
    public void onBackPressed() {
        // print current album thumbnail
        Log.d("88888AlbumViewActivity", "Current Album Thumbnail: " + currentAlbum.getThumbnailPath());
        super.onBackPressed();
        finish();
        Log.d("99999AlbumViewActivity", "Current Album Thumbnail: " + currentAlbum.getThumbnailPath());

    }
}