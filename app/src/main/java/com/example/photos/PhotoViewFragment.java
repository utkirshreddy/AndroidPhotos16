package com.example.photos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class PhotoViewFragment extends Fragment {

    private static final String ARG_PHOTO_TITLE = "photo_title";
    private String photoTitle;

    public static PhotoViewFragment newInstance(String photoTitle) {
        PhotoViewFragment fragment = new PhotoViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO_TITLE, photoTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photoTitle = getArguments().getString(ARG_PHOTO_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // For now this is just a blank screen with the photo title
        TextView titleView = view.findViewById(R.id.photo_view_title);
        titleView.setText(photoTitle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof MainActivity) {
            // Restore the original tabs with the plus button
            ((MainActivity) getActivity()).hideTabs();
        }
    }
}