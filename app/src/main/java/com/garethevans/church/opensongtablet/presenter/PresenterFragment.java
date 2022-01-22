package com.garethevans.church.opensongtablet.presenter;

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
import com.garethevans.church.opensongtablet.databinding.ModePresenterBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.tabs.TabLayoutMediator;

public class PresenterFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private ModePresenterBinding myView;
    private SongSectionsFragment songSectionsFragment;
    private SongSectionsAdapter songSectionsAdapter;
    private MediaFragment mediaFragment;
    private AlertFragment alertFragment;
    private SettingsFragment settingsFragment;
    private final String TAG = "PresenterFragment";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        displayInterface = (DisplayInterface) context;
        mainActivityInterface.registerFragment(this,"Presenter");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface.registerFragment(null,"Presenter");
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = ModePresenterBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(getString(R.string.presenter_mode));

        // Register this fragment
        mainActivityInterface.registerFragment(this,"Presenter");

        // Hide the main page buttons
        mainActivityInterface.getAppActionBar().setPerformanceMode(false);
        mainActivityInterface.showHideActionBar();

        mainActivityInterface.lockDrawer(false);
        mainActivityInterface.hideActionButton(true);

        // Get preferences
        getPreferences();

        // Set up the the pager
        setupPager();

        doSongLoad(mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"whichSongFolder",getString(R.string.mainfoldername)),
                mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"songfilename","Welcome to OpenSongApp"));

        // Prepare the song menu (will be called again after indexing from the main activity index songs)
        if (mainActivityInterface.getSongListBuildIndex().getIndexRequired() &&
                !mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
            mainActivityInterface.fullIndex();
        }



        // Set up the main action listeners
        setupListeners();

        // Set up any connected displays with the correct background
        // MainActivity initialisation has firstRun set as true.
        // Check for connected displays now we have loaded preferences, etc
        if (mainActivityInterface.getFirstRun()) {
            displayInterface.checkDisplays();
            displayInterface.updateDisplay("changeBackground");
            mainActivityInterface.setFirstRun(false);
        }

        return myView.getRoot();
    }

    private void setupPager() {
        PageAdapter pageAdapter = new PageAdapter(mainActivityInterface.getMyFragmentManager(), this.getLifecycle());
        pageAdapter.createFragment(0);
        songSectionsFragment = (SongSectionsFragment) pageAdapter.menuFragments[0];
        mediaFragment = (MediaFragment) pageAdapter.createFragment(1);
        alertFragment = (AlertFragment) pageAdapter.createFragment(2);
        settingsFragment = (SettingsFragment) pageAdapter.createFragment(3);

        mainActivityInterface.getPresenterSettings().setSongSectionsAdapter(new SongSectionsAdapter(requireContext(),mainActivityInterface,this,displayInterface));

        myView.viewPager.setAdapter(pageAdapter);
        new TabLayoutMediator(myView.presenterTabs, myView.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.song));
                    break;
                case 1:
                    tab.setText(getString(R.string.media));
                    break;
                case 2:
                    tab.setText(getString(R.string.alert));
                    break;
                case 3:
                    tab.setText(getString(R.string.settings));
                    break;
            }
        }).attach();

    }

    public void doSongLoad(String folder, String filename) {
        Log.d(TAG,"doSongLoad() called");
        mainActivityInterface.getSong().setFolder(folder);
        mainActivityInterface.getSong().setFilename(filename);
        mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSong(getContext(),mainActivityInterface,
                mainActivityInterface.getSong(),false));

        // Get the song views
        getSongViews();

        // Initialise the requirement for the song info bar to be shown
        displayInterface.updateDisplay("initialiseInfoBarRequired");
        displayInterface.updateDisplay("setSongInfo");

        // If we are a client and connected to someone else, we move to their section
        if (mainActivityInterface.getNearbyConnections().isConnected &&
                !mainActivityInterface.getNearbyConnections().isHost &&
                mainActivityInterface.getNearbyConnections().getReceiveHostSongSections() &&
                mainActivityInterface.getNearbyConnections().getHostSection()<mainActivityInterface.getSong().getSongSections().size()) {
            mainActivityInterface.getPresenterSettings().setCurrentSection(mainActivityInterface.getNearbyConnections().getHostSection());
        } else {
            mainActivityInterface.getPresenterSettings().setCurrentSection(-1);
        }

        displayInterface.updateDisplay("setSongContent");
        songSectionsFragment.showSongInfo();

    }

    private void getPreferences() {
        mainActivityInterface.getProcessSong().updateProcessingPreferences(requireContext(), mainActivityInterface);
        mainActivityInterface.getPresenterSettings().getAllPreferences(requireContext(),mainActivityInterface);
        mainActivityInterface.getMyThemeColors().getDefaultColors(getContext(),mainActivityInterface);
    }

    public void getSongViews() {
        if (mainActivityInterface.getSectionViews()==null) {
            mainActivityInterface.setSectionViews(null);
        } else {
            mainActivityInterface.getSectionViews().clear();
        }

        if (mainActivityInterface.getSong().getFiletype().equals("PDF")) {
            // Get the pages as required

        } else if (mainActivityInterface.getSong().getFiletype().equals("IMG")) {
            // Get the image as required (will be 1 page)
        } else if (mainActivityInterface.getSong().getFolder().contains("Images/")) {
            // This will be a custom slide with images
        } else {
            // A standard XML file
            mainActivityInterface.setSectionViews(mainActivityInterface.getProcessSong().setSongInLayout(requireContext(), mainActivityInterface,
                    mainActivityInterface.getSong().getLyrics(), false, true));
        }
    }

    public void updateButtons() {
        songSectionsFragment.updateAllButtons();
    }

    private void setupListeners() {
        myView.showLogo.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPresenterSettings().setLogoOn(b);
            displayInterface.updateDisplay("showLogo");
        });
        myView.blackScreen.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPresenterSettings().setBlackscreenOn(b);
            displayInterface.updateDisplay("showBlackscreen");
        });
        myView.blankScreen.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPresenterSettings().setBlankscreenOn(b);
            displayInterface.updateDisplay("showBlankscreen");
        });
        myView.updateProjector.setOnClickListener(view -> {
            myView.showLogo.setChecked(true);
            displayInterface.checkDisplays();
        });
    }

}
