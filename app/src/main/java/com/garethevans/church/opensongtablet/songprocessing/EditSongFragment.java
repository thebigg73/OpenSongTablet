package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.InformationBottomSheet;
import com.garethevans.church.opensongtablet.databinding.EditSongBinding;
import com.garethevans.church.opensongtablet.interfaces.EditSongFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// When we edit a song, we create a write the current song to tempSong in MainActivity
// We compare the two objects to look for changes and save if requested
// This way changes are temporary until specifically saved

public class EditSongFragment extends Fragment implements EditSongFragmentInterface {

    private EditSongBinding myView;
    private MainActivityInterface mainActivityInterface;
    private ViewPager2.OnPageChangeCallback callback;
    @SuppressWarnings("unused")
    private final String TAG = "EditSongFragment";
    private String edit_string="", website_edit_song_string="", information_string="", okay_string="",
            edit_song_variation_temp_string="", edit_song_variation_string="",
            website_edit_song_main_string="", website_edit_song_features_string="",
            website_edit_song_tag_string="", lyrics_string="", mainfoldername_string="",
            song_features_string="", tag_string="", not_saved_filename_string="",
            not_saved_folder_string="", not_saved_string="";

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
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = EditSongBinding.inflate(inflater, container, false);

        prepareStrings();

        // Update the toolbar
        mainActivityInterface.updateToolbar(edit_string);
        mainActivityInterface.updateToolbarHelp(website_edit_song_string);

        // Set up the updated song (a copy of the current song for editing)
        mainActivityInterface.setTempSong(new Song(mainActivityInterface.getSong()));
        mainActivityInterface.getTempSong().setLyricsUndosPos(0);
        mainActivityInterface.getTempSong().setLyricsUndos(0,mainActivityInterface.getTempSong().getLyrics());

        // Initialise views
        setUpTabs();

        // Set the save listener
        myView.saveChanges.setOnClickListener(v -> doSaveChanges());

        // If we tried to edit a temporary variation or an actual variation - let the user know
        InformationBottomSheet informationBottomSheet = null;
        if (mainActivityInterface.getWhattodo()!=null &&
                mainActivityInterface.getWhattodo().equals("editTempVariation")) {
            informationBottomSheet = new InformationBottomSheet(information_string,
                    edit_song_variation_temp_string,okay_string,null);
        } else if (mainActivityInterface.getWhattodo()!=null &&
                mainActivityInterface.getWhattodo().equals("editActualVariation")) {
            informationBottomSheet = new InformationBottomSheet(information_string,
                    edit_song_variation_string,okay_string,null);
        }

        if (informationBottomSheet!=null) {
            mainActivityInterface.setWhattodo("");
            informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"Information");
        }

        // Clear the back stack to the root editSongFragment (otherwise it includes the tabs)
        try {
            mainActivityInterface.popTheBackStack(R.id.editSongFragment, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return myView.getRoot();

    }

    private void prepareStrings() {
        if (getContext()!=null) {
            edit_string = getString(R.string.edit);
            website_edit_song_string = getString(R.string.website_edit_song);
            information_string = getString(R.string.information);
            okay_string = getString(R.string.okay);
            edit_song_variation_temp_string = getString(R.string.edit_song_variation_temp);
            edit_song_variation_string = getString(R.string.edit_song_variation);
            website_edit_song_main_string = getString(R.string.website_edit_song_main);
            website_edit_song_features_string = getString(R.string.website_edit_song_features);
            website_edit_song_tag_string = getString(R.string.website_edit_song_tag);
            lyrics_string = getString(R.string.lyrics);
            mainfoldername_string = getString(R.string.mainfoldername);
            song_features_string = getString(R.string.song_features);
            tag_string = getString(R.string.tag);
            not_saved_filename_string = getString(R.string.not_saved_filename);
            not_saved_folder_string = getString(R.string.not_saved_folder);
            not_saved_string = getString(R.string.not_saved);
        }
    }
    private void setUpTabs() {
        if (getActivity()!=null) {
            EditSongViewPagerAdapter adapter = new EditSongViewPagerAdapter(getActivity().getSupportFragmentManager(),
                    getActivity().getLifecycle());
            adapter.createFragment(0);

            myView.viewpager.setAdapter(adapter);
            myView.viewpager.setOffscreenPageLimit(1);
            callback = new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    switch (position) {
                        case 0:
                            mainActivityInterface.updateToolbarHelp(website_edit_song_string);
                            break;
                        case 1:
                            mainActivityInterface.updateToolbarHelp(website_edit_song_main_string);
                            break;
                        case 2:
                            mainActivityInterface.updateToolbarHelp(website_edit_song_features_string);
                            break;
                        case 3:
                            mainActivityInterface.updateToolbarHelp(website_edit_song_tag_string);
                            break;
                    }
                    mainActivityInterface.getWindowFlags().hideKeyboard();
                }
            };
            myView.viewpager.registerOnPageChangeCallback(callback);
            new TabLayoutMediator(myView.tabButtons, myView.viewpager, (tab, position) -> {
                switch (position) {
                    case 0:
                    default:
                        tab.setText(lyrics_string);
                        break;
                    case 1:
                        tab.setText(mainfoldername_string);
                        break;
                    case 2:
                        tab.setText(song_features_string);
                        break;
                    case 3:
                        tab.setText(tag_string);
                        break;
                }
            }).attach();
        }
    }

    public void enableSwipe(boolean canSwipe) {
        myView.viewpager.setUserInputEnabled(canSwipe);
    }

    private void doSaveChanges() {
        // Send this off for processing in a new Thread
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            // If we were editing the lyrics as ChoPro, convert to OpenSong
            if (mainActivityInterface.getTempSong().getEditingAsChoPro()) {
                String lyrics = mainActivityInterface.getTempSong().getLyrics();
                lyrics = mainActivityInterface.getConvertChoPro().fromChordProToOpenSong(lyrics);
                mainActivityInterface.getTempSong().setLyrics(lyrics);
                mainActivityInterface.getTempSong().setEditingAsChoPro(false);
            }

            // For a new song, check we have a folder/filename set
            boolean oktoproceed = true;
            if (mainActivityInterface.getTempSong().getFilename()==null || mainActivityInterface.getTempSong().getFilename().isEmpty()) {
                mainActivityInterface.getShowToast().doIt(not_saved_filename_string);
                oktoproceed = false;
            } else if (mainActivityInterface.getTempSong().getFolder()==null || mainActivityInterface.getTempSong().getFolder().isEmpty()) {
                mainActivityInterface.getShowToast().doIt(not_saved_folder_string);
                oktoproceed = false;
            } else if (mainActivityInterface.getTempSong().getTitle()==null || mainActivityInterface.getTempSong().getTitle().isEmpty()) {
                // Ok to proceed, but copy the filename into the empty title
                mainActivityInterface.getTempSong().setTitle(mainActivityInterface.getTempSong().getFilename());
            }

            if (oktoproceed && mainActivityInterface.getSaveSong().doSave(mainActivityInterface.getTempSong())) {
                // If successful, go back to the home page.  Otherwise stay here and await user decision from toast
                handler.post(() -> mainActivityInterface.navHome());
            } else if (oktoproceed) {
                handler.post(() -> mainActivityInterface.getShowToast().doIt(not_saved_string));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView.viewpager.unregisterOnPageChangeCallback(callback);
        myView = null;
    }

    public ExtendedFloatingActionButton getSaveButton() {
        return myView.saveChanges;
    }
}
