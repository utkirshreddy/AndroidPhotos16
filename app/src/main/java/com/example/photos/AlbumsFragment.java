package com.example.photos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos.databinding.FragmentAlbumsBinding;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

public class AlbumsFragment extends Fragment {

    private FragmentAlbumsBinding binding;
    private AlbumAdapter albumAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAlbumsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        binding.albumsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Get albums and create adapter
        List<Album> albums = getSampleAlbums();
        albumAdapter = new AlbumAdapter(albums);

        // Set up the action listener
        albumAdapter.setAlbumActionListener(new AlbumAdapter.AlbumActionListener() {
            @Override
            public void onRenameAlbum(Album album, int position) {
                showRenameDialog(album, position);
            }

            @Override
            public void onDeleteAlbum(Album album, int position) {
                showDeleteConfirmation(album, position);
            }
        });

        // Set up item click listener
        albumAdapter.setOnItemClickListener(album -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openAlbumDetails(album);
            }
        });

        // Assign adapter to RecyclerView
        binding.albumsRecyclerView.setAdapter(albumAdapter);
    }

    private void showRenameDialog(Album album, int position) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Rename Album");

        // Set up the input
        final EditText input = new EditText(getContext());
        input.setText(album.getTitle());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String newTitle = input.getText().toString();
            // Update album title logic here
            Toast.makeText(getContext(), "Album renamed to: " + newTitle, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteConfirmation(Album album, int position) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Delete Album");
        builder.setMessage("Are you sure you want to delete the album \"" + album.getTitle() + "\"?");

        // Set up the buttons
        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Delete album logic here
            Toast.makeText(getContext(), "Album deleted: " + album.getTitle(), Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Sample data
    private List<Album> getSampleAlbums() {
        List<Album> albums = new ArrayList<>();
        albums.add(new Album("Family", null));
        albums.add(new Album("Vacation", null));
        albums.add(new Album("Friends", null));
        return albums;
    }
}