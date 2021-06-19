package com.garethevans.church.opensongtablet.presentation;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.databinding.PresentationBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;


public class PresentationFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private PresentationBinding myView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        myView = PresentationBinding.inflate(inflater, container, false);
        //mainActivityInterface.updateToolbar(getString(R.string.presentation_mode));

        // Initialise the settings
        //mainActivityInterface.lockDrawer(false);
        //mainActivityInterface.hideActionBar(false);
        //mainActivityInterface.hideActionButton(true);

        // Deal with the action bar
        //mainActivityInterface.getAppActionBar().setHideActionBar(mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"hideActionBar",false));
        //mainActivityInterface.getAppActionBar().setPerformanceMode(false);
        //mainActivityInterface.getAppActionBar().showActionBar();

        return myView.getRoot();
    }


    // Displaying the song
    public void doSongLoad(String folder,String filename) {
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
