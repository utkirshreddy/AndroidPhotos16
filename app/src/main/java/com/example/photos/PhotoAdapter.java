package com.example.photos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final List<Photo> photos;

    // Interface for photo operations
    public interface PhotoActionListener {
        void onDeletePhoto(Photo photo, int position);
        void onMovePhoto(Photo photo, int position);
    }

    private PhotoActionListener actionListener;
    private OnPhotoClickListener clickListener;

    public void setPhotoActionListener(PhotoActionListener listener) {
        this.actionListener = listener;
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.clickListener = listener;
    }

    public PhotoAdapter(List<Photo> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_item, parent, false);
        return new PhotoViewHolder(view);
    }

    // Interface for photo click
    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);
        holder.titleTextView.setText(photo.getTitle());

        if (photo.getImage() != null) {
            holder.imageView.setImageBitmap(photo.getImage());
        } else {
            // Set default placeholder
            holder.imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Setup menu
        holder.menuImageView.setOnClickListener(v -> {
            showPopupMenu(holder.menuImageView, photo, position);
        });

        // Setup item click
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPhotoClick(photo);
            }
        });
    }

    private void showPopupMenu(View view, Photo photo, int position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.photo_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_delete_photo) {
                if (actionListener != null) {
                    actionListener.onDeletePhoto(photo, position);
                }
                return true;
            } else if (id == R.id.action_move_photo) {
                if (actionListener != null) {
                    actionListener.onMovePhoto(photo, position);
                }
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        ImageView menuImageView;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photo_image);
            titleTextView = itemView.findViewById(R.id.photo_title);
            menuImageView = itemView.findViewById(R.id.photo_menu);
        }
    }
}