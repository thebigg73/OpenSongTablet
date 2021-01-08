package com.garethevans.church.opensongtablet.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.animation.CustomAnimation;
import com.garethevans.church.opensongtablet.databinding.FragmentPresentationBinding;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ThemeColors;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;


public class PresentationFragment extends Fragment {

    // Helper classes for the heavy lifting
    private StorageAccess storageAccess;
    private Preferences preferences;
    private ProcessSong processSong;
    private LoadSong loadSong;
    private SQLiteHelper sqLiteHelper;
    private ConvertChoPro convertChoPro;
    private ConvertOnSong convertOnSong;
    private CustomAnimation customAnimation;
    private ThemeColors themeColors;


    private FragmentPresentationBinding myView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = FragmentPresentationBinding.inflate(inflater, container, false);
        View root = myView.getRoot();

        // Initialise the helper classes for the heavy lifting
        initialiseHelpers();

        doSongLoad();

        return root;
    }


    // Getting the preferences and helpers ready
    private void initialiseHelpers() {
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        loadSong = new LoadSong();
        processSong = new ProcessSong();
        sqLiteHelper = new SQLiteHelper(getActivity());
        convertOnSong = new ConvertOnSong();
        convertChoPro = new ConvertChoPro();
        customAnimation = new CustomAnimation();
        themeColors = new ThemeColors();
    }



    // Displaying the song
    public void doSongLoad() {
        new Thread(() -> {/*
            // Quick fade the current page
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> customAnimation.faderAnimation(myView.pageHolder,100,false));
            // Load up the song
            loadSong.doLoadSong(getActivity(),storageAccess,preferences,songXML,processSong,sqLiteHelper,
                    convertOnSong, convertChoPro);

            Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                // Get the song in the layout
                sectionViews = processSong.setSongInLayout(getActivity(),trimSections, addSectionSpace,
                        trimLines, lineSpacing, getColor("lyricsBackground"),getColor("lyricsText"),
                        getColor("lyricsChords"), scaleHeadings, scaleChords, scaleComments,
                        StaticVariables.mLyrics);

                // We now have the 1 column layout ready, so we can set the view observer to measure once drawn
                setUpVTO();

                // Update the toolbar
                mListener.updateToolbar();
            });*/
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
