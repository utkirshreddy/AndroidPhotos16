package com.example.photos;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AlbumDetailsFragment extends Fragment {

    private static final String ARG_ALBUM_TITLE = "album_title";
    private String albumTitle;
    private RecyclerView photosRecyclerView;
    private PhotoAdapter photoAdapter;

    public static AlbumDetailsFragment newInstance(String albumTitle) {
        AlbumDetailsFragment fragment = new AlbumDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ALBUM_TITLE, albumTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            albumTitle = getArguments().getString(ARG_ALBUM_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up RecyclerView
        photosRecyclerView = view.findViewById(R.id.photos_recycler_view);
        photosRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Get sample photos
        List<Photo> photos = getSamplePhotos();
        photoAdapter = new PhotoAdapter(photos);

        // Set up action listener
        photoAdapter.setPhotoActionListener(new PhotoAdapter.PhotoActionListener() {
            @Override
            public void onDeletePhoto(Photo photo, int position) {
                showDeleteConfirmation(photo, position);
            }

            @Override
            public void onMovePhoto(Photo photo, int position) {
                showMoveDialog(photo, position);
            }
        });

        // Set up click listener for photo viewing
        photoAdapter.setOnPhotoClickListener(photo -> {
            openPhotoView(photo);
        });

        photosRecyclerView.setAdapter(photoAdapter);
    }

    private void openPhotoView(Photo photo) {
        // Create and navigate to the photo view fragment
        PhotoViewFragment fragment = PhotoViewFragment.newInstance(photo.getTitle());

        // Update the tab layout for photo view
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showPhotoTabs();
        }

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment_content_main, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showDeleteConfirmation(Photo photo, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Photo");
        builder.setMessage("Are you sure you want to delete this photo?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Delete logic would go here
            Toast.makeText(getContext(), "Photo deleted", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showMoveDialog(Photo photo, int position) {
        // This would show a dialog with album options
        Toast.makeText(getContext(), "Move photo feature coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showTabs();
        }
    }

    // Sample data
    private List<Photo> getSamplePhotos() {
        List<Photo> photos = new ArrayList<>();
        photos.add(new Photo("Beach Day", null));
        photos.add(new Photo("Mountains", null));
        photos.add(new Photo("City Lights", null));
        photos.add(new Photo("Dinner Party", null));
        photos.add(new Photo("Birthday", null));
        photos.add(new Photo("Concert", null));
        return photos;
    }
}