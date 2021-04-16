package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.animation.CustomAnimation;
import com.garethevans.church.opensongtablet.ccli.CCLILog;
import com.garethevans.church.opensongtablet.databinding.FragmentEditSongBinding;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.SaveSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.EditSongFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songsandsetsmenu.SongListBuildIndex;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.NonOpenSongSQLiteHelper;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

// When editing a song, we don't rely on the database values
// The song gets loaded from storage, parsed into a new song object (held in EditContent)
// When we save, we write this into the new/existing file

public class EditSongFragment extends Fragment implements EditSongFragmentInterface {

    private FragmentEditSongBinding myView;
    private MainActivityInterface mainActivityInterface;
    private EditContent editContent;
    private CustomAnimation customAnimation;
    private StorageAccess storageAccess;
    private Preferences preferences;
    private ConvertChoPro convertChoPro;
    private ConvertOnSong convertOnSong;
    private ProcessSong processSong;
    private LoadSong loadSong;
    private SaveSong saveSong;
    private ShowToast showToast;
    private NonOpenSongSQLiteHelper nonOpenSongSQLiteHelper;
    private CommonSQL commonSQL;
    private SQLiteHelper sqLiteHelper;
    private CCLILog ccliLog;
    private boolean imgOrPDF;
    private Bundle savedInstanceState;
    private Fragment fragment;
    private SongListBuildIndex songListBuildIndex;

    Song song,originalSong;

    boolean saveOK = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.registerFragment(this,"EditSongFragment");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mainActivityInterface.registerFragment(null,"EditSongFragment");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = FragmentEditSongBinding.inflate(inflater, container, false);
        this.savedInstanceState = savedInstanceState;
        fragment = this;

        // Initialise helpers
        initialiseHelpers();

        // Set up toolbar
        mainActivityInterface.updateToolbar(null,getResources().getString(R.string.edit));

        // Stop the song menu from opening and hide the page button
        mainActivityInterface.hideActionButton(true);
        mainActivityInterface.lockDrawer(true);

        song = processSong.initialiseSong(mainActivityInterface,song.getFolder(),song.getFilename());

        // The folder and filename were passed in the starting song object.  Now update with the file contents
        song = loadSong.doLoadSong(getActivity(),mainActivityInterface,song, false);

        // Can't just do song=originalSong as they become clones.  Changing one changes the other.
        originalSong = new Song(song);

        // A quick test of images or PDF files (sent when saving)
        imgOrPDF = (song.getFiletype().equals("PDF") || song.getFiletype().equals("IMG"));

        // Initialise views
        setUpTabs();

        myView.saveChanges.setOnClickListener(v -> doSaveChanges());
        return myView.getRoot();
    }

    private void initialiseHelpers() {
        customAnimation = mainActivityInterface.getCustomAnimation();
        storageAccess = mainActivityInterface.getStorageAccess();
        preferences = mainActivityInterface.getPreferences();
        convertChoPro = mainActivityInterface.getConvertChoPro();
        processSong = mainActivityInterface.getProcessSong();
        saveSong = mainActivityInterface.getSaveSong();
        loadSong = mainActivityInterface.getLoadSong();
        showToast = mainActivityInterface.getShowToast();
        ccliLog = mainActivityInterface.getCCLILog();
        sqLiteHelper = mainActivityInterface.getSQLiteHelper();
        nonOpenSongSQLiteHelper = mainActivityInterface.getNonOpenSongSQLiteHelper();
        commonSQL = mainActivityInterface.getCommonSQL();
        songListBuildIndex = mainActivityInterface.getSongListBuildIndex();
    }

    private void setUpTabs() {
        EditSongViewPagerAdapter adapter = new EditSongViewPagerAdapter(requireActivity().getSupportFragmentManager(), requireActivity().getLifecycle());
        adapter.createFragment(0);
        EditSongFragmentMain editSongFragmentMain = (EditSongFragmentMain) adapter.menuFragments[0];
        EditSongFragmentFeatures editSongFragmentFeatures = (EditSongFragmentFeatures) adapter.createFragment(1);
        EditSongFragmentTags editSongFragmentTags = (EditSongFragmentTags) adapter.createFragment(2);
        ViewPager2 viewPager = myView.viewpager;
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
                    tab.setText(getString(R.string.tag));
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
            /*saveOK = saveSong.doSave(requireContext(),mainActivityInterface,song,
                    editContent.getCurrentSong(), imgOrPDF);
*/
            if (saveOK) {
                // If successful, go back to the home page.  Otherwise stay here and await user decision from toast
                requireActivity().runOnUiThread(() -> mainActivityInterface.returnToHome(fragment, savedInstanceState));
            } else {
                showToast.doIt(getActivity(),requireContext().getResources().getString(R.string.not_saved));
            }
        }).start();
    }

    // These are triggered from one of the viewpager pages, through the mainactivity, back here
    public void updateSong(Song song) {
        this.song = song;
    }
    public Song getSong() {
        return song;
    }
    public void setOriginalSong(Song originalSong) {
        this.originalSong = originalSong;
    }
    public Song getOriginalSong() {
        return originalSong;
    }
    public boolean songChanged() {
        return !song.equals(originalSong);
    }
}
