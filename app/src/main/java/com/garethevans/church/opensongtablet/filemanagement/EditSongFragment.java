package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.animation.CustomAnimation;
import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.databinding.FragmentEditSongBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.SongXML;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLite;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLite;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class EditSongFragment extends Fragment {

    EditSongViewPagerAdapter adapter;
    ViewPager2 viewPager;
    EditSongFragmentMain editSongFragmentMain;
    EditSongFragmentFeatures editSongFragmentFeatures;
    EditSongFragmentTags editSongFragmentTags;
    FragmentEditSongBinding myView;
    MainActivityInterface mainActivityInterface;
    EditContent editContent;
    CustomAnimation customAnimation;
    StorageAccess storageAccess;
    Preferences preferences;
    ConvertChoPro convertChoPro;
    ProcessSong processSong;
    SongXML songXML;
    SaveSong saveSong;
    NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    NonOpenSongSQLite nonOpenSongSQLite;
    SQLiteHelper sqLiteHelper;
    SQLite sqLite;
    CCLILog ccliLog;
    boolean imgOrPDF;
    Bundle savedInstanceState;
    Fragment fragment;

    boolean createNewSong;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.registerFragment(this,"EditSongFragment");
        if (StaticVariables.songfilename.contains("*NEWSONG*")) {
            createNewSong = true;
            // If we cancel, we'll be back to the original song in the view.
            StaticVariables.songfilename = StaticVariables.songfilename.replace("*NEWSONG*","");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface.registerFragment(null,"EditSongFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = FragmentEditSongBinding.inflate(inflater, container, false);
        this.savedInstanceState = savedInstanceState;
        fragment = this;

        // Initialise helpers
        initialiseHelpers();

        // Initialise Views
        initialiseViews();

        // Set up toolbar
        setupToolbar();

        // Stop the song menu from opening and hide the page button
        mainActivityInterface.hideActionButton(true);
        mainActivityInterface.lockDrawer(true);

        if (createNewSong) {
            StaticVariables.fileType = "XML";
            songXML.initialiseSongTags();
            StaticVariables.mTitle = getActivity().getResources().getString(R.string.song_new_name);
        }

        // A quick test of images or PDF files (sent when saving)
        imgOrPDF = (StaticVariables.fileType.equals("PDF") || StaticVariables.fileType.equals("IMG"));

        // Initialise the correct SQL helper (used when saving).  Null mean they aren't needed.
        if (imgOrPDF) {
            nonOpenSongSQLiteHelper = new NonOpenSongSQLiteHelper(requireContext());
            nonOpenSongSQLite = new NonOpenSongSQLite();
        } else {
            sqLiteHelper = new SQLiteHelper(requireContext());
            sqLite = new SQLite();
        }

        // Initialise views
        setUpTabs();

        myView.saveChanges.setOnClickListener(v -> doSaveChanges());
        return myView.getRoot();
    }

    private void initialiseHelpers() {
        editContent = new EditContent(true);
        customAnimation = new CustomAnimation();
        storageAccess = new StorageAccess();
        preferences = new Preferences();
        convertChoPro = new ConvertChoPro();
        processSong = new ProcessSong();
        songXML = new SongXML();
        saveSong = new SaveSong();
        ccliLog = new CCLILog();
    }

    private void initialiseViews() {
    }


    private void setupToolbar() {
        mainActivityInterface.updateToolbar(getResources().getString(R.string.edit));
    }

    private void setUpTabs() {
        adapter = new EditSongViewPagerAdapter(getActivity().getSupportFragmentManager(), requireActivity().getLifecycle());
        adapter.createFragment(0);
        editSongFragmentMain = (EditSongFragmentMain)adapter.menuFragments[0];
        editSongFragmentFeatures = (EditSongFragmentFeatures) adapter.createFragment(1);
        editSongFragmentTags = (EditSongFragmentTags) adapter.createFragment(2);
        viewPager = myView.viewpager;
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        TabLayout tabLayout = myView.tabButtons;
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                default:
                    tab.setText(getString(R.string.mainfoldername));
                    break;
                case 1:
                    tab.setText(getString(R.string.song_features));
                    break;
                case 2:
                    tab.setText(getString(R.string.tag_tag));
                    break;
            }
        }).attach();
    }

    public void pulseSaveChanges(boolean pulse) {
        if (pulse) {
            myView.saveChanges.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
            customAnimation.pulse(requireContext(),myView.saveChanges);
        } else {
            myView.saveChanges.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondary)));
            myView.saveChanges.clearAnimation();
        }
    }

    private void doSaveChanges() {
        // Send this off for processing in a new Thread
        new Thread(() -> {
            saveSong.doSave(requireContext(),preferences,storageAccess,editContent,songXML,convertChoPro,
                    processSong,sqLite,sqLiteHelper,nonOpenSongSQLite,nonOpenSongSQLiteHelper,ccliLog,imgOrPDF);

        requireActivity().runOnUiThread(() -> mainActivityInterface.returnToHome(fragment,savedInstanceState));
        }).start();
    }
}
