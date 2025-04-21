package com.example.photos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos.models.Album;

import java.util.List;

public class AlbumSelectionAdapter extends RecyclerView.Adapter<AlbumSelectionAdapter.AlbumSelectionViewHolder> {

    private final List<Album> albums;
    private OnAlbumSelectedListener listener;

    public interface OnAlbumSelectedListener {
        void onAlbumSelected(Album album);
    }

    public AlbumSelectionAdapter(List<Album> albums) {
        this.albums = albums;
    }

    public void setOnAlbumSelectedListener(OnAlbumSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new AlbumSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumSelectionViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.textView.setText(album.getName() + " (" + album.getPhotos().size() + " photos)");
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    class AlbumSelectionViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public AlbumSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onAlbumSelected(albums.get(getAdapterPosition()));
                }
            });
        }
    }


}