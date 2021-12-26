package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.EditSongBinding;
import com.garethevans.church.opensongtablet.interfaces.EditSongFragmentInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayoutMediator;

// When we edit a song, we create a write the current song to tempSong in MainActivity
// We compare the two objects to look for changes and save if requested
// This way changes are temporary until specifically saved

public class EditSongFragment extends Fragment implements EditSongFragmentInterface {

    private EditSongBinding myView;
    private MainActivityInterface mainActivityInterface;
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
        Window w = requireActivity().getWindow();
        if (w!=null) {
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = EditSongBinding.inflate(inflater, container, false);

        // Update the toolbar
        mainActivityInterface.updateToolbar(getResources().getString(R.string.edit));

        // Set up the updated song (a copy of the current song for editing)
        mainActivityInterface.setTempSong(new Song(mainActivityInterface.getSong()));

        // Initialise views
        setUpTabs();

        // Set the save listener
        myView.saveChanges.setOnClickListener(v -> doSaveChanges());
        return myView.getRoot();
    }

    private void setUpTabs() {
        EditSongViewPagerAdapter adapter = new EditSongViewPagerAdapter(requireActivity().getSupportFragmentManager(),
                requireActivity().getLifecycle());
        adapter.createFragment(0);
        myView.viewpager.setAdapter(adapter);
        myView.viewpager.setOffscreenPageLimit(1);
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

    public void showSaveAllowed(boolean showSave) {
        if (showSave) {
            myView.saveChanges.setVisibility(View.VISIBLE);
        } else {
            myView.saveChanges.setVisibility(View.GONE);
        }
    }

    public void enableSwipe(boolean canSwipe) {
        Log.d(TAG,"canSwipe: "+canSwipe);
        myView.viewpager.setUserInputEnabled(canSwipe);
    }

    private void doSaveChanges() {
        // Send this off for processing in a new Thread
        new Thread(() -> {
            if (mainActivityInterface.getSaveSong().doSave(requireContext(), mainActivityInterface,
                    mainActivityInterface.getTempSong())) {
                // If successful, go back to the home page.  Otherwise stay here and await user decision from toast

                requireActivity().runOnUiThread(() -> mainActivityInterface.navHome());
            } else {
                mainActivityInterface.getShowToast().doIt(getActivity(),requireContext().getResources().getString(R.string.not_saved));
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }

    public ExtendedFloatingActionButton getSaveButton() {
        return myView.saveChanges;
    }
}
