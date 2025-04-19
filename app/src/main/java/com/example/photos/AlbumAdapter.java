package com.example.photos;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private final List<Album> albums;

    // Interface for album operations
    public interface AlbumActionListener {
        void onRenameAlbum(Album album, int position);
        void onDeleteAlbum(Album album, int position);
    }

    private AlbumActionListener actionListener;

    public AlbumAdapter(List<Album> albums) {
        this.albums = albums;
    }

    public void setAlbumActionListener(AlbumActionListener listener) {
        this.actionListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Album album);
    }

    private OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.titleTextView.setText(album.getTitle());

        if (album.getThumbnail() != null) {
            holder.thumbnailImageView.setImageBitmap(album.getThumbnail());
        } else {
            // Set a default placeholder image
            holder.thumbnailImageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Setup menu click listener
        holder.menuImageView.setOnClickListener(v -> {
            showPopupMenu(holder.menuImageView, album, position);
        });

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(album);
            }
        });
    }

    private void showPopupMenu(View view, Album album, int position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.album_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_rename) {
                if (actionListener != null) {
                    actionListener.onRenameAlbum(album, position);
                }
                return true;
            } else if (id == R.id.action_delete) {
                if (actionListener != null) {
                    actionListener.onDeleteAlbum(album, position);
                }
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView titleTextView;
        ImageView menuImageView;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.album_thumbnail);
            titleTextView = itemView.findViewById(R.id.album_title);
            menuImageView = itemView.findViewById(R.id.album_menu);
        }
    }
}