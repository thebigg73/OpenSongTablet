package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
    private final String TAG = "EditSongFragment";

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

        // Update the toolbar
        mainActivityInterface.updateToolbar(getResources().getString(R.string.edit));
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_edit_song));

        // Set up the updated song (a copy of the current song for editing)
        mainActivityInterface.setTempSong(new Song(mainActivityInterface.getSong()));
        mainActivityInterface.getTempSong().setLyricsUndosPos(0);
        mainActivityInterface.getTempSong().setLyricsUndos(0,mainActivityInterface.getTempSong().getLyrics());

        Log.d(TAG,"songFolder:"+mainActivityInterface.getSong().getFolder()+"  tempSongFolder:"+ mainActivityInterface.getTempSong().getFolder());
        // Initialise views
        setUpTabs();

        // Set the save listener
        myView.saveChanges.setOnClickListener(v -> doSaveChanges());

        // If we tried to edit a temporary variation or an actual variation - let the user know
        InformationBottomSheet informationBottomSheet = null;
        if (mainActivityInterface.getWhattodo()!=null &&
                mainActivityInterface.getWhattodo().equals("editTempVariation")) {
            informationBottomSheet = new InformationBottomSheet(getString(R.string.information),
                    getString(R.string.edit_song_variation_temp),getString(R.string.okay),null);
        } else if (mainActivityInterface.getWhattodo()!=null &&
                mainActivityInterface.getWhattodo().equals("editActualVariation")) {
            informationBottomSheet = new InformationBottomSheet(getString(R.string.information),
                    getString(R.string.edit_song_variation),getString(R.string.okay),null);
        }

        if (informationBottomSheet!=null) {
            mainActivityInterface.setWhattodo("");
            informationBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"Information");
        }
        return myView.getRoot();

    }

    private void setUpTabs() {
        EditSongViewPagerAdapter adapter = new EditSongViewPagerAdapter(requireActivity().getSupportFragmentManager(),
                requireActivity().getLifecycle());
        adapter.createFragment(0);

        myView.viewpager.setAdapter(adapter);
        myView.viewpager.setOffscreenPageLimit(1);
        callback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d(TAG,"position:"+position);
                switch (position) {
                    case 0:
                        mainActivityInterface.updateToolbarHelp(getString(R.string.website_edit_song));
                        break;
                    case 1:
                        mainActivityInterface.updateToolbarHelp(getString(R.string.website_edit_song_main));
                        break;
                    case 2:
                        mainActivityInterface.updateToolbarHelp(getString(R.string.website_edit_song_features));
                        break;
                    case 3:
                        mainActivityInterface.updateToolbarHelp(getString(R.string.website_edit_song_tag));
                        break;
                }
                mainActivityInterface.forceImmersive();
            }
        };
        myView.viewpager.registerOnPageChangeCallback(callback);
        new TabLayoutMediator(myView.tabButtons, myView.viewpager, (tab, position) -> {
            switch (position) {
                case 0:
                default:
                    tab.setText(getString(R.string.lyrics));
                    break;
                case 1:
                    tab.setText(getString(R.string.mainfoldername));
                    break;
                case 2:
                    tab.setText(getString(R.string.song_features));
                    break;
                case 3:
                    tab.setText(getString(R.string.tag));
                    break;
            }
        }).attach();
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
            if (mainActivityInterface.getSaveSong().doSave(mainActivityInterface.getTempSong())) {
                // If successful, go back to the home page.  Otherwise stay here and await user decision from toast
                handler.post(() -> mainActivityInterface.navHome());
            } else {
                handler.post(() -> mainActivityInterface.getShowToast().doIt(requireContext().getString(R.string.not_saved)));
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
