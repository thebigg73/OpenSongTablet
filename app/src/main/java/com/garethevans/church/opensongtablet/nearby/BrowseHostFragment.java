package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsNearbyBrowseBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class BrowseHostFragment extends Fragment {
    // This fragment is used to request, then display a list of files available on the host device
    // It is only accessible on devices that are connected and are not running as hosts themselves
    // This fragment can be called from the SetActionsFragment, ProfileActionsFragment and SongActionsFragment

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BrowseHostFragment";
    private MainActivityInterface mainActivityInterface;
    private SettingsNearbyBrowseBinding myView;
    @SuppressWarnings("FieldCanBeLocal")
    private String browse_host_files_string="", set_string="", profile_string="", song_string="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.getNearbyConnections().setBrowseHostFragment(this);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = SettingsNearbyBrowseBinding.inflate(inflater, container, false);
        return myView.getRoot();
    }

    @Override
    public void onResume() {
        prepareStrings();
        setupViews();
        setupListeners();
        mainActivityInterface.getNearbyConnections().setBrowseHostFragment(this);
        // Now request the files from the host and wait for a response
        mainActivityInterface.getNearbyConnections().sendRequestHostItems();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mainActivityInterface.getNearbyConnections().setBrowseHostFragment(null);
        super.onDestroy();
    }

    private void prepareStrings() {
        if (getContext()!=null && mainActivityInterface!=null && myView!=null) {
            browse_host_files_string = getString(R.string.connections_browse_host);
            set_string = getString(R.string.set);
            profile_string = getString(R.string.profile);
            song_string = getString(R.string.song);
            String title_string;
            String web_help = "";
            switch (mainActivityInterface.getWhattodo()) {
                case "browsesets":
                default:
                    title_string = browse_host_files_string + ": " + set_string;
                    web_help = getString(R.string.website_browse_host_files_set);
                    break;
                case "browseprofiles":
                    title_string = browse_host_files_string + ": " + profile_string;
                    //web_help = getString(R.string.website_browse_host_files_profile);
                    break;
                case "broswesongs":
                    title_string = browse_host_files_string + ": " + song_string;
                    //web_help = getString(R.string.website_browse_host_files_song);
                    break;
            }
            mainActivityInterface.updateToolbar(title_string);
            mainActivityInterface.updateToolbarHelp(web_help);
        }
    }

    private void setupViews() {
        // When we start up, we need to show the dimmed background and progress bar and hide the rest
        myView.dimBackground.setVisibility(View.VISIBLE);
        myView.hostProgressBar.setVisibility(View.VISIBLE);
        myView.nestedScrollView.setVisibility(View.GONE);
    }

    private void setupListeners() {

    }

    public void displayHostItems(String[] hostItems) {
        // We can now update the arrayAdapter
        for (String hostItem:hostItems) {
            Log.d(TAG,hostItem);
        }
    }
}
