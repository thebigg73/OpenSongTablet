package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.bible.BibleGatewayBottomSheet;
import com.garethevans.church.opensongtablet.bible.BibleOfflineBottomSheet;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.ModePresenterMediaBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class MediaFragment extends Fragment {

    // TODO GET THIS WORKING

    private final String TAG = "MediaFragment";
    private MainActivityInterface mainActivityInterface;
    private ModePresenterMediaBinding myView;
    private String padKey, padFile, linkYouTube, linkAudio, mediaHint;
    private Uri padUri;
    ArrayList<String> audioSources;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        myView = ModePresenterMediaBinding.inflate(inflater, container, false);

        onSongLoad();

        initialiseDropdowns();

        setListeners();
        return myView.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        onSongLoad();
    }

    private void initialiseDropdowns() {
        audioSources = new ArrayList<>();
        audioSources.add(getString(R.string.pad_auto));
        audioSources.add(getString(R.string.link_audio));
        audioSources.add(getString(R.string.link_youtube));
        audioSources.add(getString(R.string.file_chooser));
        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                myView.audioSource, R.layout.view_exposed_dropdown_item, audioSources);
        myView.audioSource.setAdapter(exposedDropDownArrayAdapter);
        myView.audioSource.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                // Show the appropriate text in the view below
                String string = editable.toString();
                if (string.equals(audioSources.get(0))) {
                    prepareAutoPad();
                } else if (string.equals(audioSources.get(1))) {
                    prepareLinkFile();
                } else if (string.equals(audioSources.get(2))) {
                    prepareFileChosen();
                }
            }
        });
    }

    private void prepareAutoPad() {
        //
        boolean isAutoPad = mainActivityInterface.getPad().isAutoPad();
        boolean isCustomAutoPad = mainActivityInterface.getPad().isCustomAutoPad();
        if (isAutoPad && !isCustomAutoPad) {
            // Just use the built in pad

        } else if (isAutoPad && isCustomAutoPad) {
            // Use the custom auto pad

        } else {
            // No valid autopad
        }

    }

    private void prepareLinkFile() {

    }

    private void prepareFileChosen() {

    }

    private void showHideAudioSources(boolean pad, boolean link, boolean youtube, boolean file) {

    }

    private int getVisibilityFromBoolean(boolean visible) {
        if (visible) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }
    private void setListeners() {
        myView.bibleOffline.setOnClickListener(view -> {
            BibleOfflineBottomSheet bibleOfflineBottomSheet = new BibleOfflineBottomSheet();
            bibleOfflineBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"BibleOfflineBottomSheet");
        });
        myView.bibleOnline.setOnClickListener(view -> {
            BibleGatewayBottomSheet bibleGatewayBottomSheet = new BibleGatewayBottomSheet();
            bibleGatewayBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "BibleGatewayBottomSheet");
        });
    }

    public void onSongLoad() {
        padKey = mainActivityInterface.getSong().getKey();
        padFile = mainActivityInterface.getSong().getPadfile();
        linkYouTube = mainActivityInterface.getSong().getLinkyoutube();
        linkAudio = mainActivityInterface.getSong().getLinkaudio();

        // Get the padUri
        padUri = mainActivityInterface.getPad().getPadUri();
        Log.d(TAG,"padKey: "+padKey);
        Log.d(TAG,"padFile: "+padFile);
        Log.d(TAG,"linkYouTube: "+linkYouTube);
        Log.d(TAG,"linkAudio: "+linkAudio);
        Log.d(TAG,"padUri: "+padUri);
    }

}
