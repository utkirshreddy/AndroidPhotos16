package com.example.photos;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.photos.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Show tabs initially
        showTabs();

        // Set up FAB
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("TAG", "onCreate: TEST");
                Snackbar.make(view, "Create a new album", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Create", null).show();
            }
        });

        // Load the albums fragment by default
        loadFragment(new AlbumsFragment());
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment_content_main, fragment);
        transaction.commit();
    }

    public void hideTabs() {
        binding.tabs.removeAllTabs();

        // Hide the tab indicator
        binding.tabs.setSelectedTabIndicatorColor(Color.parseColor("#FF5252"));

        // Create a special TabLayout configuration for the album view
        binding.tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tabs.setTabMode(TabLayout.MODE_FIXED);

        // Create back button tab (left side)
        TabLayout.Tab backTab = binding.tabs.newTab();
        View backButton = getLayoutInflater().inflate(R.layout.tab_back_button, null);
        backButton.setOnClickListener(v -> getSupportFragmentManager().popBackStack());
        backTab.setCustomView(backButton);

        // Create empty middle tab (takes up space)
        TabLayout.Tab spacerTab = binding.tabs.newTab();
        View spacerView = new View(this);
        spacerTab.setCustomView(spacerView);

        // Create add button tab (right side)
        TabLayout.Tab addTab = binding.tabs.newTab();
        View addButton = getLayoutInflater().inflate(R.layout.tab_add_button, null);
        addButton.setOnClickListener(v -> {
            Snackbar.make(v, "Add photo feature coming soon", Snackbar.LENGTH_SHORT).show();
        });
        addTab.setCustomView(addButton);

        // Add all tabs
        binding.tabs.addTab(backTab);
        binding.tabs.addTab(spacerTab);
        binding.tabs.addTab(addTab);

        // Configure tab weights
        TabLayout tabLayout = binding.tabs;
        ViewGroup tabStrip = (ViewGroup) tabLayout.getChildAt(0);

        // Back button gets minimal width
        View backView = tabStrip.getChildAt(0);
        ViewGroup.LayoutParams backParams = backView.getLayoutParams();
        backParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;

        // Middle spacer gets all extra space
        View spacerTabView = tabStrip.getChildAt(1);
        ViewGroup.LayoutParams middleParams = spacerTabView.getLayoutParams();
        middleParams.width = 0;
        ((LinearLayout.LayoutParams) middleParams).weight = 1;

        // Add button gets minimal width and right alignment
        View addView = tabStrip.getChildAt(2);
        ViewGroup.LayoutParams addParams = addView.getLayoutParams();
        addParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public void showTabs() {
        // Restore tabs
        binding.tabs.setVisibility(View.VISIBLE);
        binding.tabs.removeAllTabs();

        // Show the FAB when returning to albums dashboard
        binding.fab.show();

        // Reset tab indicator color
        binding.tabs.setSelectedTabIndicatorColor(getResources().getColor(android.R.color.white));

        // Reset BOTH tab properties to original
        binding.tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tabs.setTabMode(TabLayout.MODE_FIXED);

        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.tab_albums));
        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.tab_search));

        // Re-add tab listener
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    loadFragment(new AlbumsFragment());
                } else {
                    Snackbar.make(binding.getRoot(), "Search feature coming soon", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    public void showPhotoTabs() {
        binding.tabs.removeAllTabs();

        // Hide the FAB
        binding.fab.hide();

        // Hide the tab indicator
        binding.tabs.setSelectedTabIndicatorColor(Color.parseColor("#FF5252"));

        // Configure the tab layout
        binding.tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tabs.setTabMode(TabLayout.MODE_FIXED);

        // Create back button tab (left side)
        // In MainActivity.java, update the showPhotoTabs() method:

// Create back button tab (left side)
        TabLayout.Tab backTab = binding.tabs.newTab();
        View backButton = getLayoutInflater().inflate(R.layout.tab_back_button, null);
// In showPhotoTabs() method, modify the back button click handler:
// In the showPhotoTabs() method, update the back button click handler:
// In the showPhotoTabs() method, modify the back button click handler:
// In the showPhotoTabs() method, update the back button click handler:
        // In the backToAlbumDetails() method, update the back button click handler:
        backButton.setOnClickListener(v -> {
            // Check if the current fragment is PhotoViewFragment
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
            if (currentFragment instanceof PhotoViewFragment) {
                // Navigate back to AlbumDetailsFragment
                getSupportFragmentManager().popBackStackImmediate(); // Remove PhotoViewFragment
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.nav_host_fragment_content_main, new AlbumDetailsFragment());
                transaction.commit();
            } else if (currentFragment instanceof AlbumDetailsFragment) {
                // If already in AlbumDetailsFragment, just pop the back stack
                getSupportFragmentManager().popBackStack();
            }
        });
        backTab.setCustomView(backButton);

        // Create empty spacer tab (takes up space)
        TabLayout.Tab spacerTab = binding.tabs.newTab();
        View spacerView = new View(this);
        spacerTab.setCustomView(spacerView);

        // Create menu button tab (right side)
        TabLayout.Tab menuTab = binding.tabs.newTab();
        View menuButton = getLayoutInflater().inflate(R.layout.tab_menu_button, null);
        menuButton.findViewById(R.id.menu_button).setOnClickListener(v -> {
            showPhotoViewMenu(v);
        });
        menuTab.setCustomView(menuButton);

        // Add tabs
        binding.tabs.addTab(backTab);
        binding.tabs.addTab(spacerTab);
        binding.tabs.addTab(menuTab);

        // Configure tab weights
        TabLayout tabLayout = binding.tabs;
        ViewGroup tabStrip = (ViewGroup) tabLayout.getChildAt(0);

        // Back button gets minimal width
        View backView = tabStrip.getChildAt(0);
        ViewGroup.LayoutParams backParams = backView.getLayoutParams();
        backParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;

        // Middle spacer gets all extra space
        View spacerTabView = tabStrip.getChildAt(1);
        ViewGroup.LayoutParams middleParams = spacerTabView.getLayoutParams();
        middleParams.width = 0;
        ((LinearLayout.LayoutParams) middleParams).weight = 1;

        // Menu button gets minimal width
        View menuView = tabStrip.getChildAt(2);
        ViewGroup.LayoutParams menuParams = menuView.getLayoutParams();
        menuParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private void showPhotoViewMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.photo_view_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_delete_photo_view) {
                Snackbar.make(view, "Delete photo feature coming soon", Snackbar.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_move_photo_view) {
                Snackbar.make(view, "Move photo feature coming soon", Snackbar.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_add_tag) {
                Snackbar.make(view, "Add tag feature coming soon", Snackbar.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_delete_tag) {
                Snackbar.make(view, "Delete tag feature coming soon", Snackbar.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }




    // Update openAlbumDetails method
    public void openAlbumDetails(Album album) {
        hideTabs(); // Hide tabs
        binding.fab.hide(); // Hide the FAB when viewing album details

        AlbumDetailsFragment fragment = AlbumDetailsFragment.newInstance(album.getTitle());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment_content_main, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}