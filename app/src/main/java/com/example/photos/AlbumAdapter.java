package com.example.photos;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos.models.Album;
import com.example.photos.models.Photo;

import java.io.IOException;
import java.util.List;



public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private final Context context;
    private final List<Album> albums;
    private OnAlbumClickListener onAlbumClickListener;
    private OnOptionsClickListener onOptionsClickListener;

    // set albums
    public void setAlbums(List<Album> albums) {
        this.albums.clear();
        this.albums.addAll(albums);
        notifyDataSetChanged();
    }
    public interface OnAlbumClickListener {
        void onAlbumClick(int position);
    }

    public interface OnOptionsClickListener {
        void onOptionsClick(int position);
    }

    public AlbumAdapter(Context context, List<Album> albums) {
        this.context = context;
        this.albums = albums;
    }

    public void setOnAlbumClickListener(OnAlbumClickListener listener) {
        this.onAlbumClickListener = listener;
    }

    public void setOnOptionsClickListener(OnOptionsClickListener listener) {
        this.onOptionsClickListener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.albumTitle.setText(album.getName());

        String thumbnailPath = album.getThumbnailPath();

        if (thumbnailPath == null || thumbnailPath.isEmpty()) {
            holder.albumThumbnail.setImageResource(R.drawable.placeholder_album);
            return;
        }

        try {
            Uri thumbnailUri = Uri.parse(thumbnailPath);

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), thumbnailUri);
            holder.albumThumbnail.setImageBitmap(bitmap);
        } catch (SecurityException | IOException e) {
            holder.albumThumbnail.setImageResource(R.drawable.placeholder_album);
            Log.e("AlbumAdapter", "Error loading thumbnail for " + album.getName(), e);
        }
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView albumThumbnail;
        TextView albumTitle;
        ImageButton albumOptions;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumThumbnail = itemView.findViewById(R.id.album_thumbnail);
            albumTitle = itemView.findViewById(R.id.album_title);
            albumOptions = itemView.findViewById(R.id.album_options);

            itemView.setOnClickListener(v -> {
                if (onAlbumClickListener != null) {
                    onAlbumClickListener.onAlbumClick(getAdapterPosition());
                }
            });

            albumOptions.setOnClickListener(v -> {
                if (onOptionsClickListener != null) {
                    onOptionsClickListener.onOptionsClick(getAdapterPosition());
                }
            });
        }
    }
}