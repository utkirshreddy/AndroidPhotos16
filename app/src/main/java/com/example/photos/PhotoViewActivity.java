package com.example.photos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos.models.Album;
import com.example.photos.models.Photo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.bumptech.glide.Glide;


public class PhotoViewActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView photoDisplay;
    private TextView photoTags;
    private Button btnPrevious;
    private Button btnNext;
    private FloatingActionButton fabPhotoOptions;

    private List<Photo> albumPhotos;
    private int currentPhotoPosition;
    private Photo currentPhoto;

    public static final String EXTRA_ALBUM_PHOTOS = "album_photos";
    public static final String EXTRA_PHOTO_POSITION = "photo_position";

    // AI Mixed
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);


        toolbar = findViewById(R.id.photo_toolbar);
        photoDisplay = findViewById(R.id.photo_display);
        photoTags = findViewById(R.id.photo_tags);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        fabPhotoOptions = findViewById(R.id.fab_photo_options);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        Intent intent = getIntent();
        albumPhotos = (List<Photo>) intent.getSerializableExtra(EXTRA_ALBUM_PHOTOS);
        currentPhotoPosition = intent.getIntExtra(EXTRA_PHOTO_POSITION, 0);


        if (albumPhotos == null) {
            albumPhotos = new ArrayList<>();
        }


        btnPrevious.setOnClickListener(v -> showPreviousPhoto());
        btnNext.setOnClickListener(v -> showNextPhoto());
        fabPhotoOptions.setOnClickListener(v -> showPhotoOptionsDialog());


        displayPhoto();
    }

    // AI
    private String getOriginalFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DISPLAY_NAME}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                    result = cursor.getString(columnIndex);
                    int dotIndex = result.lastIndexOf('.');
                    if (dotIndex != -1) {
                        result = result.substring(0, dotIndex);
                    }
                }
            } catch (Exception e) {
                Log.e("PhotoViewActivity", "Error getting file name", e);
            }
        } else if ("file".equals(uri.getScheme())) {
            result = uri.getLastPathSegment();
            if (result != null) {
                int dotIndex = result.lastIndexOf('.');
                if (dotIndex != -1) {
                    result = result.substring(0, dotIndex);
                }
            }
        }
        return result;
    }

    // AI
    private Uri saveImageToAppStorage(Uri sourceUri) {
        try {
            String originalFileName = getOriginalFileName(sourceUri);
            String fileExtension = getFileExtension(sourceUri);

            if (fileExtension == null || fileExtension.isEmpty()) {
                fileExtension = "jpg";
            }

            String fileName = "photo-" + System.currentTimeMillis() + "." + fileExtension;

            try {
                if ("content".equals(sourceUri.getScheme())) {
                    getContentResolver().takePersistableUriPermission(
                            sourceUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            } catch (SecurityException e) {
                Log.w("PhotoViewActivity", "Unable to take persistable permission", e);
            }

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

            if (currentPhoto != null && originalFileName != null && !originalFileName.isEmpty()) {
                currentPhoto.setTitle(originalFileName);
                Log.d("PhotoViewActivity", "Set photo title to original filename: " + originalFileName);
            }

            return Uri.fromFile(outputFile);
        } catch (IOException e) {
            Log.e("PhotoViewActivity", "Error saving image", e);
            return null;
        }
    }

    // AI Mixed
    private String getFileExtension(Uri uri) {
        String extension = null;

        try {
            if ("content".equals(uri.getScheme())) {
                String mimeType = getContentResolver().getType(uri);
                if (mimeType != null) {
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                }
            }

            if (extension == null) {
                String path = uri.getPath();
                if (path != null) {
                    int dot = path.lastIndexOf(".");
                    if (dot >= 0) {
                        extension = path.substring(dot + 1).toLowerCase();
                    }
                }
            }

            if (extension == null) {
                extension = "jpg";
            }

            extension = extension.toLowerCase();
            if (extension.equals("jpeg")) {
                extension = "jpg";
            }

        } catch (Exception e) {
            Log.e("PhotoViewActivity", "Error getting file extension", e);
            extension = "jpg";
        }

        return extension;
    }

    // AI Mixed
    private void displayPhoto() {
        if (albumPhotos.isEmpty()) {
            Toast.makeText(this, "No photos to display", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentPhoto = albumPhotos.get(currentPhotoPosition);
        String albumname = currentPhoto.getAlbumName();
        toolbar.setTitle(currentPhoto.getTitle() + " - " + albumname);

        try {
            Uri photoUri = Uri.parse(currentPhoto.getPath());
            // Check if this is a Photo Picker URI
            if (photoUri.toString().contains("content://media/picker")) {
                try {
                    // Try to save a copy of the image to our app's storage
                    Uri savedUri = saveImageToAppStorage(photoUri);
                    if (savedUri != null) {
                        // Update the photo's path with our saved copy
                        currentPhoto.setPath(savedUri.toString());
                        savePhotoChanges();

                        // Use Glide to load the image (handles all formats including GIF)
                        Glide.with(this)
                                .load(savedUri)
                                .into(photoDisplay);

                        Toast.makeText(this, "Photo saved to app storage", Toast.LENGTH_SHORT).show();
                    } else {
                        photoDisplay.setImageResource(R.drawable.placeholder_photo);
                        Toast.makeText(this, "Couldn't save photo. Please re-add it.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Log.e("PhotoViewActivity", "Error saving image from picker", e);
                    photoDisplay.setImageResource(R.drawable.placeholder_photo);
                    Toast.makeText(this, "Photo access permission expired. Please re-add this photo.", Toast.LENGTH_LONG).show();
                }
            } else {
                // Use Glide for all image loading to properly support all formats
                Glide.with(this)
                        .load(photoUri)
                        .error(R.drawable.placeholder_photo)
                        .into(photoDisplay);
            }
        } catch (SecurityException e) {
            photoDisplay.setImageResource(R.drawable.placeholder_photo);
            Toast.makeText(this, "Unable to load photo. Please re-add it.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // Update navigation buttons
        btnPrevious.setEnabled(currentPhotoPosition > 0);
        btnNext.setEnabled(currentPhotoPosition < albumPhotos.size() - 1);

        // Update tags display
        updateTagsDisplay();
    }


    private void showPreviousPhoto() {
        if (currentPhotoPosition > 0) {
            currentPhotoPosition--;
            displayPhoto();
        }
    }


    private void showNextPhoto() {
        if (currentPhotoPosition < albumPhotos.size() - 1) {
            currentPhotoPosition++;
            displayPhoto();
        }
    }


    private void showPhotoOptionsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_photo_options, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_add_tag).setOnClickListener(v -> {
            dialog.dismiss();
            showAddTagDialog();
        });

        dialogView.findViewById(R.id.btn_delete_tag).setOnClickListener(v -> {
            dialog.dismiss();
            showRemoveTagDialog();
        });

        dialogView.findViewById(R.id.btn_delete_photo).setOnClickListener(v -> {
            dialog.dismiss();
            showDeletePhotoConfirmation();
        });


        dialogView.findViewById(R.id.btn_save_to_gallery).setOnClickListener(v -> {
            dialog.dismiss();
            savePhotoToGallery();
        });

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // AI Mixed
    private void savePhotoToGallery() {
        Photo currentPhoto = albumPhotos.get(currentPhotoPosition);
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
                    mimeType = "image/jpeg"; // Default to JPEG
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


    // AI Mixed
    private void showAddTagDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_tag, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        AutoCompleteTextView tagTypeDropdown = dialogView.findViewById(R.id.tag_type_dropdown);
        String[] tagTypes = {Photo.TAG_PERSON, Photo.TAG_LOCATION};
        ArrayAdapter<String> tagTypeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, tagTypes);
        tagTypeDropdown.setAdapter(tagTypeAdapter);
        tagTypeDropdown.setText(tagTypes[0], false);

        EditText tagValueInput = dialogView.findViewById(R.id.tag_value_input);

        dialogView.findViewById(R.id.btn_add_tag).setOnClickListener(v -> {
            String tagType = tagTypeDropdown.getText().toString();
            String tagValue = tagValueInput.getText().toString().trim();

            if (tagValue.isEmpty()) {
                Toast.makeText(this, "Tag value cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean added = currentPhoto.addTag(tagType, tagValue);
            if (added) {
                updateTagsDisplay();
                savePhotoChanges();
                Toast.makeText(this, "Tag added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tag already exists or invalid type", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    private void showRemoveTagDialog() {
        Map<String, List<String>> allTags = currentPhoto.getAllTags();
        List<String> tagItems = new ArrayList<>();

        for (String tagType : allTags.keySet()) {
            List<String> values = allTags.get(tagType);
            for (String value : values) {
                tagItems.add(tagType + ": " + value);
            }
        }

        if (tagItems.isEmpty()) {
            Toast.makeText(this, "No tags to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Tag to Remove")
                .setItems(tagItems.toArray(new String[0]), (dialog, which) -> {
                    String selected = tagItems.get(which);
                    String[] parts = selected.split(":", 2);
                    String tagType = parts[0].trim();
                    String tagValue = parts[1].trim();

                    boolean removed = currentPhoto.removeTag(tagType, tagValue);
                    if (removed) {
                        updateTagsDisplay();
                        savePhotoChanges();
                        Toast.makeText(this, "Tag removed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to remove tag", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeletePhotoConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("DELETED_POSITION", currentPhotoPosition);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // AI Mixed
    private void updateTagsDisplay() {
        StringBuilder tagsText = new StringBuilder();
        Map<String, List<String>> allTags = currentPhoto.getAllTags();

        // Add person tags
        List<String> personTags = allTags.get(Photo.TAG_PERSON);
        if (personTags != null && !personTags.isEmpty()) {
            for (String tag : personTags) {
                tagsText.append("Person: ").append(tag).append("\n");
            }
        }

        // Add location tags
        List<String> locationTags = allTags.get(Photo.TAG_LOCATION);
        if (locationTags != null && !locationTags.isEmpty()) {
            for (String tag : locationTags) {
                tagsText.append("Location: ").append(tag).append("\n");
            }
        }

        if (tagsText.length() == 0) {
            photoTags.setText("No tags");
        } else {
            // Remove the last newline if needed
            if (tagsText.length() > 0 && tagsText.charAt(tagsText.length() - 1) == '\n') {
                tagsText.setLength(tagsText.length() - 1);
            }
            photoTags.setText(tagsText.toString());
        }
    }


    // AI
    private void savePhotoChanges() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("UPDATED_PHOTO", currentPhoto);
        resultIntent.putExtra("UPDATED_POSITION", currentPhotoPosition);
        setResult(RESULT_OK, resultIntent);
    }


    @Override
    public void onBackPressed() {
        savePhotoChanges();
        super.onBackPressed();
        finish();
    }
}