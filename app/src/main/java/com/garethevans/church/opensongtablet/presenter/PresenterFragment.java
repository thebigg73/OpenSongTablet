package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.os.Bundle;
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

        // Hide the main page buttons
        mainActivityInterface.getAppActionBar().setPerformanceMode(false);
        mainActivityInterface.getAppActionBar().showActionBar();

        mainActivityInterface.lockDrawer(false);
        mainActivityInterface.hideActionButton(true);

        // Set up the the pager
        setupPager();

        // Set up the main action listeners
        setupListeners();

        doSongLoad(mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"whichSongFolder",getString(R.string.mainfoldername)),
                mainActivityInterface.getPreferences().getMyPreferenceString(requireContext(),"songfilename","Welcome to OpenSongApp"));


        return myView.getRoot();
    }

    private void setupPager() {
        PageAdapter pageAdapter = new PageAdapter(mainActivityInterface.getMyFragmentManager(), this.getLifecycle());
        pageAdapter.createFragment(0);
        songSectionsFragment = (SongSectionsFragment) pageAdapter.menuFragments[0];
        mediaFragment = (MediaFragment) pageAdapter.createFragment(1);
        alertFragment = (AlertFragment) pageAdapter.createFragment(2);
        settingsFragment = (SettingsFragment) pageAdapter.createFragment(3);
        myView.viewPager.setAdapter(pageAdapter);

        new TabLayoutMediator(myView.presenterTabs, myView.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.song));
                    break;
                case 1:
                    tab.setText(getString(R.string.video));
                    //tab.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_format_list_numbers_white_36dp, null));
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
        mainActivityInterface.getSong().setFolder(folder);
        mainActivityInterface.getSong().setFilename(filename);
        mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSong(getContext(),mainActivityInterface,
                mainActivityInterface.getSong(),false));
        mainActivityInterface.setSong(mainActivityInterface.getLoadSong().doLoadSong(getContext(),mainActivityInterface,
                mainActivityInterface.getSong(),false));
        songSectionsFragment.showSongInfo();


    }

    private void setupListeners() {
        myView.showLogo.setOnCheckedChangeListener((compoundButton, b) -> displayInterface.presenterShowLogo(b));
        myView.blackScreen.setOnCheckedChangeListener(((compoundButton, b) -> displayInterface.presenterBlackScreen(b)));
    }
}
