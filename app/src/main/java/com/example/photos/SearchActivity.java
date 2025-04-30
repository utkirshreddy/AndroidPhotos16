package com.example.photos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos.models.Album;
import com.example.photos.models.Photo;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SearchActivity extends AppCompatActivity {

    // UI Components
    private Toolbar toolbar;
    private AutoCompleteTextView tagTypeInput;
    private AutoCompleteTextView tagValueInput;
    private RadioGroup conjunctionGroup;
    private Button btnAddTagCriteria;
    private Button btnSearch;
    private RecyclerView searchCriteriaList;
    private RecyclerView searchResultsRecyclerView;
    private TextView resultsTitle;
    private TextView emptyResultsView;

    // Data
    private List<Photo> searchResults;
    private Set<String> availableTagValues;
    private List<Album> albums;
    private List<Photo> allPhotos;
    private PhotoAdapter photoAdapter;

    // Search logic
    private boolean useAndLogic = false;
    private boolean isMultipleSearch = false;

    private RadioGroup searchModeGroup;
    private LinearLayout multipleSearchSection;
    private AutoCompleteTextView tagTypeInput2;
    private AutoCompleteTextView tagValueInput2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        albums = (List<Album>) getIntent().getSerializableExtra("ALBUM_LIST");
        if (albums == null) {
            albums = new ArrayList<>();
        }

        Toolbar toolbar = findViewById(R.id.search_toolbar);
        searchModeGroup = findViewById(R.id.search_mode_group);
        multipleSearchSection = findViewById(R.id.multiple_search_section);
        tagTypeInput = findViewById(R.id.tag_type_input);
        tagValueInput = findViewById(R.id.tag_value_input);
        tagTypeInput2 = findViewById(R.id.tag_type_input2);
        tagValueInput2 = findViewById(R.id.tag_value_input2);
        conjunctionGroup = findViewById(R.id.conjunction_group);
        btnSearch = findViewById(R.id.btn_search);
        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view);
        resultsTitle = findViewById(R.id.results_title);
        emptyResultsView = findViewById(R.id.empty_results_view);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        searchResults = new ArrayList<>();
        availableTagValues = new HashSet<>();
        allPhotos = new ArrayList<>();

        loadAllPhotos();
        loadAvailableTagValues();

        String[] tagTypes = {Photo.TAG_PERSON, Photo.TAG_LOCATION};
        ArrayAdapter<String> tagTypeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, tagTypes);

        tagTypeInput.setAdapter(tagTypeAdapter);
        tagTypeInput.setText(tagTypes[0], false);

        tagTypeInput2.setAdapter(tagTypeAdapter);
        tagTypeInput2.setText(tagTypes[0], false);

        tagTypeInput.setThreshold(1);
        tagValueInput.setThreshold(1);
        tagTypeInput2.setThreshold(1);
        tagValueInput2.setThreshold(1);

        tagTypeInput.setOnItemClickListener((parent, view, position, id) -> {
            updateTagValueAdapter(tagValueInput, tagTypes[position]);
        });

        tagTypeInput2.setOnItemClickListener((parent, view, position, id) -> {
            updateTagValueAdapter(tagValueInput2, tagTypes[position]);
        });

        updateTagValueAdapter(tagValueInput, tagTypes[0]);
        updateTagValueAdapter(tagValueInput2, tagTypes[0]);

        searchModeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            isMultipleSearch = checkedId == R.id.radio_multiple_tags;
            multipleSearchSection.setVisibility(isMultipleSearch ? View.VISIBLE : View.GONE);
        });

        conjunctionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            useAndLogic = checkedId == R.id.radio_and;
        });

        photoAdapter = new PhotoAdapter(this, searchResults);
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        searchResultsRecyclerView.setAdapter(photoAdapter);

        photoAdapter.setOnPhotoClickListener(position -> {
            Photo selectedPhoto = searchResults.get(position);
            Intent intent = new Intent(SearchActivity.this, PhotoViewActivity.class);
            ArrayList<Photo> photoList = new ArrayList<>(searchResults);
            intent.putExtra(PhotoViewActivity.EXTRA_ALBUM_PHOTOS, photoList);
            intent.putExtra(PhotoViewActivity.EXTRA_PHOTO_POSITION, position);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> performSearch());
    }


    private void updateTagValueAdapter(AutoCompleteTextView inputField, String tagType) {
        Set<String> uniqueTagValues = new HashSet<>();

        for (Photo photo : allPhotos) {
            uniqueTagValues.addAll(photo.getTagsOfType(tagType));
        }

        List<String> tagValues = new ArrayList<>(uniqueTagValues);

        ArrayAdapter<String> tagValueAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, tagValues);
        inputField.setAdapter(tagValueAdapter);
    }

    private void performSearch() {
        searchResults.clear();

        if (isMultipleSearch) {
            performMultipleTagSearch();
        } else {
            performSingleTagSearch();
        }

        photoAdapter.notifyDataSetChanged();
        resultsTitle.setVisibility(View.VISIBLE);

        if (searchResults.isEmpty()) {
            searchResultsRecyclerView.setVisibility(View.GONE);
            emptyResultsView.setVisibility(View.VISIBLE);
        } else {
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
            emptyResultsView.setVisibility(View.GONE);
        }
    }

    private void performSingleTagSearch() {
        String tagType = tagTypeInput.getText().toString().trim();
        String tagValue = tagValueInput.getText().toString().trim();

        if (tagValue.isEmpty()) {
            Toast.makeText(this, "Tag value cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Photo photo : allPhotos) {
            if (photo.hasTag(tagType, tagValue)) {
                searchResults.add(photo);
            }
        }
    }

    private void performMultipleTagSearch() {
        String tagType1 = tagTypeInput.getText().toString().trim();
        String tagValue1 = tagValueInput.getText().toString().trim();
        String tagType2 = tagTypeInput2.getText().toString().trim();
        String tagValue2 = tagValueInput2.getText().toString().trim();

        if (tagValue1.isEmpty() || tagValue2.isEmpty()) {
            Toast.makeText(this, "Tag values cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Photo photo : allPhotos) {
            boolean matches1 = photo.hasTag(tagType1, tagValue1);
            boolean matches2 = photo.hasTag(tagType2, tagValue2);

            if (useAndLogic) {
                if (matches1 && matches2) {
                    searchResults.add(photo);
                }
            } else {
                if (matches1 || matches2) {
                    searchResults.add(photo);
                }
            }
        }
    }


    private void loadAllPhotos() {
        allPhotos.clear();
        for (Album album : albums) {
            allPhotos.addAll(album.getPhotos());
        }
    }

    private void loadAvailableTagValues() {
        availableTagValues.clear();
        for (Photo photo : allPhotos) {
            for (String tagType : new String[]{Photo.TAG_PERSON, Photo.TAG_LOCATION}) {
                List<String> typeTags = photo.getTagsOfType(tagType);
                availableTagValues.addAll(typeTags);
            }
        }
    }


}