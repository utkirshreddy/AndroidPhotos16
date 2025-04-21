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

import com.example.photos.models.Photo;

import java.io.IOException;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final Context context;
    private final List<Photo> photos;
    private OnPhotoClickListener onPhotoClickListener;
    private OnPhotoLongClickListener onPhotoLongClickListener;

    public interface OnPhotoClickListener {
        void onPhotoClick(int position);
    }

    public interface OnPhotoLongClickListener {
        boolean onPhotoLongClick(int position);
    }

    public PhotoAdapter(Context context, List<Photo> photos) {
        this.context = context;
        this.photos = photos;
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.onPhotoClickListener = listener;
    }

    public void setOnPhotoLongClickListener(OnPhotoLongClickListener listener) {
        this.onPhotoLongClickListener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photo_item, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);

        holder.photoTitle.setText(photo.getTitle());
        Log.d("PHOTOADAPT", "onBindViewHolder: " + photo.getPath());


        try {
            Uri photoUri = Uri.parse(photo.getPath());
            String extension = photo.getFileExtension().toLowerCase();


            // Check if this is a Photo Picker URI
            if (photoUri.toString().contains("content://media/picker")) {
                // For Photo Picker URIs, use a placeholder instead of trying to access
                holder.photoThumbnail.setImageResource(R.drawable.placeholder_photo);
            } else {
                // For other URIs, try to load normally
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoUri);
                holder.photoThumbnail.setImageBitmap(bitmap);
            }
        } catch (SecurityException | IOException e) {
            // Handle permission errors by using a placeholder
            holder.photoThumbnail.setImageResource(R.drawable.placeholder_photo);
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    // set photo
    public void setPhotos(List<Photo> photos) {
        this.photos.clear();
        this.photos.addAll(photos);
        notifyDataSetChanged();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoThumbnail;
        TextView photoTitle;
        ImageButton photoOptions;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoThumbnail = itemView.findViewById(R.id.photo_thumbnail);
            photoTitle = itemView.findViewById(R.id.photo_title);
            photoOptions = itemView.findViewById(R.id.photo_options);

            itemView.setOnClickListener(v -> {
                if (onPhotoClickListener != null) {
                    onPhotoClickListener.onPhotoClick(getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (onPhotoLongClickListener != null) {
                    return onPhotoLongClickListener.onPhotoLongClick(getAdapterPosition());
                }
                return false;
            });

            // If using options button instead of long press
            if (photoOptions != null) {
                photoOptions.setOnClickListener(v -> {
                    if (onPhotoLongClickListener != null) {
                        onPhotoLongClickListener.onPhotoLongClick(getAdapterPosition());
                    }
                });
            }
        }
    }
}