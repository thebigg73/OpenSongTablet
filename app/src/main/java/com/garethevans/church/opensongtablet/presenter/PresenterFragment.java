package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.AlertInfoBottomSheet;
import com.garethevans.church.opensongtablet.databinding.ModePresenterBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class PresenterFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private ModePresenterBinding myView;
    private PageAdapter pageAdapter;
    private SongSectionsFragment songSectionsFragment;
    private AdvancedFragment advancedFragment;
    private final String TAG = "PresenterFragment";
    private boolean landscape;
    private String presenter_mode_string="", mainfoldername_string="", song_string="",
            extra_settings_string="";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        displayInterface = (DisplayInterface) context;
        mainActivityInterface.registerFragment(this,"Presenter");
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                mainActivityInterface.onBackPressed();
            }
        };
        if (getActivity()!=null) {
            getActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface.registerFragment(null,"Presenter");
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = ModePresenterBinding.inflate(inflater,container,false);

        prepareStrings();

        mainActivityInterface.updateToolbar(presenter_mode_string);

        // Get the orientation
        landscape = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        setPortraitLandscape();

        // Register this fragment
        mainActivityInterface.registerFragment(this,"Presenter");
        mainActivityInterface.updateFragment("updateSongMenuSortTitles",this,null);

        // Hide the main page buttons
        mainActivityInterface.lockDrawer(false);
        mainActivityInterface.getToolbar().setPerformanceMode(false);
        mainActivityInterface.showActionBar();

        mainActivityInterface.hideActionButton(true);

        // Get preferences
        getPreferences();

        // Initialise the inline set
        if (getContext()!=null) {
            myView.inlineSetList.initialisePreferences(getContext(), mainActivityInterface);
        }
        myView.inlineSetList.prepareSet();

        // Set up the the pager
        setupPager();

        if (mainActivityInterface.getWhattodo().equals("pendingLoadSet")) {
            mainActivityInterface.setWhattodo("");
            // Check if the current song is in the set
            int position = mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
            if (position<0) {
                mainActivityInterface.loadSongFromSet(0);
            } else {
                mainActivityInterface.loadSongFromSet(position);
            }
        } else {
            doSongLoad(mainActivityInterface.getPreferences().getMyPreferenceString("songFolder", mainfoldername_string),
                    mainActivityInterface.getPreferences().getMyPreferenceString("songFilename", "Welcome to OpenSongApp"));
        }

        // Prepare the song menu (will be called again after indexing from the main activity index songs)
        if (mainActivityInterface.getSongListBuildIndex().getIndexRequired() &&
                !mainActivityInterface.getSongListBuildIndex().getCurrentlyIndexing()) {
            mainActivityInterface.fullIndex();
        }

        // Set up the main action listeners for the switches
        setupListeners();

        // Set up any connected displays with the correct background
        // MainActivity initialisation has firstRun set as true.
        // Check for connected displays now we have loaded preferences, etc
        if (mainActivityInterface.getFirstRun()) {
            displayInterface.checkDisplays();
            displayInterface.updateDisplay("changeBackground");
            mainActivityInterface.setFirstRun(false);
        }

        // Check if we need to show an alert
        if (mainActivityInterface.getAlertChecks().showPlayServicesAlert() ||
                mainActivityInterface.getAlertChecks().showBackup() || mainActivityInterface.getAlertChecks().showUpdateInfo()) {
            AlertInfoBottomSheet alertInfoBottomSheet = new AlertInfoBottomSheet();
            alertInfoBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "AlertInfoBottomSheet");
        }

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            presenter_mode_string = getString(R.string.presenter_mode);
            mainfoldername_string = getString(R.string.mainfoldername);
            song_string = getString(R.string.song);
            extra_settings_string = getString(R.string.extra_settings);
        }
    }
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Change the new orientation views (before we declare the orientation has changed)
        // Doing this first means that the listeners of the new views aren't called
        if (landscape) {
            myView.showLogoSide.setChecked(mainActivityInterface.getPresenterSettings().getLogoOn());
            myView.blankScreenSide.setChecked(mainActivityInterface.getPresenterSettings().getBlankscreenOn());
            myView.blackScreenSide.setChecked(mainActivityInterface.getPresenterSettings().getBlackscreenOn());
        } else {
            myView.showLogo.setChecked(mainActivityInterface.getPresenterSettings().getLogoOn());
            myView.blankScreen.setChecked(mainActivityInterface.getPresenterSettings().getBlankscreenOn());
            myView.blackScreen.setChecked(mainActivityInterface.getPresenterSettings().getBlackscreenOn());
        }
        // Now register the new orientation so the oncheckchanged listeners work
        landscape = newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE;
        // Show and hide the correct views
        setPortraitLandscape();
    }

    private void setPortraitLandscape() {
        if (landscape) {
            myView.bottomBit.setVisibility(View.GONE);
            myView.sideBit.setVisibility(View.VISIBLE);
        } else {
            myView.sideBit.setVisibility(View.GONE);
            myView.bottomBit.setVisibility(View.VISIBLE);
        }
    }
    private void setupPager() {
        new Handler(Looper.getMainLooper()).post(() -> {
            pageAdapter = new PageAdapter(mainActivityInterface.getMyFragmentManager(), this.getLifecycle());
            pageAdapter.createFragment(0);
            songSectionsFragment = (SongSectionsFragment) pageAdapter.menuFragments[0];
            //mediaFragment = (MediaFragment) pageAdapter.createFragment(1);
            advancedFragment = (AdvancedFragment) pageAdapter.createFragment(1);
            //settingsFragment = (SettingsFragment) pageAdapter.createFragment(3);

            if (getContext()!=null) {
                mainActivityInterface.getPresenterSettings().setSongSectionsAdapter(
                        new SongSectionsAdapter(getContext(), mainActivityInterface,
                                songSectionsFragment, displayInterface));
            }

            myView.viewPager.setAdapter(pageAdapter);
            new TabLayoutMediator(myView.presenterTabs, myView.viewPager, (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText(song_string);
                        break;
                    case 1:
                        tab.setText(extra_settings_string);
                        break;
                }
            }).attach();

            // Show any showcase instructions required
            showTutorial();

            tryToImportIntent();
        });

    }

    public void tryToImportIntent() {
        // We may have opened the app at this fragment by clicking on an openable file
        // Get the main activity to check and fix backstack to this as home if required
        mainActivityInterface.dealWithIntent(R.id.presenterFragment);
    }

    public void doSongLoad(String folder, String filename) {
        myView.viewPager.setCurrentItem(0);
        mainActivityInterface.getSong().setFolder(folder);
        mainActivityInterface.getSong().setFilename(filename);
        mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSong(
                mainActivityInterface.getSong(),false));

        mainActivityInterface.closeDrawer(true);

        // Because we have loaded the song, figure out any presentation order requirements
        mainActivityInterface.getSong().setPresoOrderSongSections(null);
        mainActivityInterface.getProcessSong().processSongIntoSections(
                mainActivityInterface.getSong(), true);

        // Get the song views
        getSongViews();

        // Set up the new song for the secondary display.  Doesn't necessarily show it yet though
        displayInterface.updateDisplay("newSongLoaded");
        displayInterface.updateDisplay("setSongInfo");

        // IV - Consume any later pending client section change received from Host (-ve value)
        if (mainActivityInterface.getNearbyConnections().hasValidConnections() &&
                !mainActivityInterface.getNearbyConnections().getIsHost() &&
                mainActivityInterface.getNearbyConnections().getWaitingForSectionChange()) {
            int pendingSection = mainActivityInterface.getNearbyConnections().getPendingCurrentSection();

            // Reset the flags to off
            mainActivityInterface.getNearbyConnections().setWaitingForSectionChange(false);
            mainActivityInterface.getNearbyConnections().setPendingCurrentSection(-1);

            mainActivityInterface.getNearbyConnections().doSectionChange(pendingSection);
        } else {
            mainActivityInterface.getPresenterSettings().setCurrentSection(-1);
        }

        // State we haven't started the projection (for the song info bar check)
        mainActivityInterface.getPresenterSettings().setStartedProjection(false);

        // Prepare the song content views - doesn't show them though
        displayInterface.updateDisplay("setSongContent");

        if (songSectionsFragment!=null) {
            myView.viewPager.postDelayed(() -> {
                songSectionsFragment.setContext(getContext());
                myView.viewPager.setCurrentItem(0);
                songSectionsFragment.showSongInfo();
            }, 50);
        }

        // If we are in a set, send that info to the inline set custom view to see if it should draw
        myView.inlineSetList.checkVisibility();

        // If a song has MIDI messages and we're intent on sending it automatically, do that
        // The MIDI class checks for valid connections
        // Update any midi commands (if any)
        if (mainActivityInterface.getPreferences().getMyPreferenceBoolean("midiSendAuto",false)) {
            mainActivityInterface.getMidi().buildSongMidiMessages();
            mainActivityInterface.getMidi().sendSongMessages();
        }
    }

    private void getPreferences() {
        mainActivityInterface.getProcessSong().updateProcessingPreferences();
        mainActivityInterface.getPresenterSettings().getAllPreferences();
        mainActivityInterface.getMyThemeColors().getDefaultColors();
    }

    public void getSongViews() {
        if (mainActivityInterface.getSectionViews()==null) {
            mainActivityInterface.setSectionViews(null);
        } else {
            mainActivityInterface.getSectionViews().clear();
        }

        // Assume for now, we are loading a standard XML file
        mainActivityInterface.setSectionViews(mainActivityInterface.getProcessSong().setSongInLayout(
                mainActivityInterface.getSong(), false, true));


        // Set the load status to the song (used to enable nearby section change listener)
        mainActivityInterface.getSong().setCurrentlyLoading(false);

    }

    public void updateButtons() {
        songSectionsFragment.updateAllButtons();
    }

    private void setupListeners() {
        myView.showLogo.setOnCheckedChangeListener(new MyCheckChangeListener());
        myView.showLogoSide.setOnCheckedChangeListener(new MyCheckChangeListener());
        myView.blackScreen.setOnCheckedChangeListener(new MyCheckChangeListener());
        myView.blackScreenSide.setOnCheckedChangeListener(new MyCheckChangeListener());
        myView.blankScreen.setOnCheckedChangeListener(new MyCheckChangeListener());
        myView.blankScreenSide.setOnCheckedChangeListener(new MyCheckChangeListener());

        myView.panicBottom.setOnClickListener(view -> {
            myView.showLogo.setChecked(true);
            displayInterface.checkDisplays();
        });
        myView.panicSide.setOnClickListener(view -> {
            myView.showLogo.setChecked(true);
            displayInterface.checkDisplays();
        });
    }

    public void showTutorial() {
        // Send these views to the song sections layout so we can highlight them
        ArrayList<View> viewsToHighlight = new ArrayList<>();
        viewsToHighlight.add(myView.showLogo);
        viewsToHighlight.add(myView.blankScreen);
        viewsToHighlight.add(myView.blackScreen);
        viewsToHighlight.add(myView.panicBottom);
        if (songSectionsFragment!=null) {
            songSectionsFragment.showTutorial(viewsToHighlight);
        }
    }

    public void setAllowPager(boolean allowPager) {
        //pageAdapter.setAllowPager(allowPager);
        Log.d(TAG,"swipe enable="+allowPager);
        myView.viewPager.setUserInputEnabled(allowPager);
    }

    private class MyCheckChangeListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if ((landscape && compoundButton==myView.showLogoSide) || compoundButton==myView.showLogo) {
                mainActivityInterface.getPresenterSettings().setLogoOn(b);
                displayInterface.updateDisplay("showLogo");
            } else if ((landscape && compoundButton==myView.blankScreenSide) || compoundButton==myView.blankScreen) {
                mainActivityInterface.getPresenterSettings().setBlankscreenOn(b);
                displayInterface.updateDisplay("showBlankscreen");
            } else if ((landscape && compoundButton==myView.blackScreenSide) || compoundButton==myView.blackScreen) {
                mainActivityInterface.getPresenterSettings().setBlackscreenOn(b);
                displayInterface.updateDisplay("showBlackscreen");
            }
        }
    }

    public void selectSection(int section) {
        songSectionsFragment.selectSection(section);
    }

    // Inline set
    public void toggleInlineSet() {
        myView.inlineSetList.toggleInlineSet();
    }
    public void orientationInlineSet(int orientation) {
        myView.inlineSetList.orientationChanged(orientation);
    }
    public void updateInlineSetSet() {
        if (myView!=null) {
            myView.inlineSetList.prepareSet();
        }
    }
    public void updateInlineSetItem(int position) {
        Log.d(TAG,"update :"+position);
        myView.inlineSetList.updateSelected(position);
    }
    public void updateInlineSetMove(int from, int to) {
        myView.inlineSetList.updateInlineSetMove(from,to);
    }
    public void updateInlineSetRemoved(int from) {
        myView.inlineSetList.updateInlineSetRemoved(from);
    }
    public void updateInlineSetAdded(SetItemInfo setItemInfo) {
        myView.inlineSetList.updateInlineSetAdded(setItemInfo);
    }
    public void initialiseInlineSetItem(int position) {
        myView.inlineSetList.initialiseInlineSetItem(position);
    }

}
