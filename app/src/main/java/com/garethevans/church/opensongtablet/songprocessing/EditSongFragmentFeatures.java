package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownSelection;
import com.garethevans.church.opensongtablet.databinding.EditSongFeaturesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class EditSongFragmentFeatures extends Fragment {

    EditSongFeaturesBinding myView;
    MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
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
        myView = EditSongFeaturesBinding.inflate(inflater, container, false);

        // Set up the values
        setupValues();

        // Set up the listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupValues() {
        // The key
        ExposedDropDownArrayAdapter keyArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.view_exposed_dropdown_item, getResources().getStringArray(R.array.key_choice));
        myView.key.setAdapter(keyArrayAdapter);
        ExposedDropDownSelection exposedDropDownSelection = new ExposedDropDownSelection();
        exposedDropDownSelection.keepSelectionPosition(myView.key, getResources().getStringArray(R.array.key_choice));
        myView.key.setText(mainActivityInterface.getTempSong().getKey());

        // The capo
        ArrayList<String> capos = new ArrayList<>();
        capos.add("");
        for (int x=0; x<12; x++) {
            capos.add(x+"");
        }
        ExposedDropDownArrayAdapter capoArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.view_exposed_dropdown_item, capos);
        exposedDropDownSelection.keepSelectionPosition(myView.capo, capos);
        myView.capo.setAdapter(capoArrayAdapter);
        myView.capo.setText(mainActivityInterface.getTempSong().getCapo());

        // The pad file
        ArrayList<String> padfiles = new ArrayList<>();
        padfiles.add(getString(R.string.pad_auto));
        padfiles.add(getString(R.string.custom));
        padfiles.add(getString(R.string.link_audio));
        ExposedDropDownArrayAdapter padArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.view_exposed_dropdown_item, padfiles);
        exposedDropDownSelection.keepSelectionPosition(myView.pad, padfiles);
        myView.pad.setAdapter(padArrayAdapter);
        myView.pad.setText(niceTextFromPref(mainActivityInterface.getTempSong().getPadfile()));


        // The loop
        myView.loop.setChecked(mainActivityInterface.getTempSong().getPadloop().equals("true"));

        // The tempo
        myView.tempo.setText(mainActivityInterface.getTempSong().getTempo());

        // The timesig
        ArrayList<String> timesigs = new ArrayList<>();
        for (int divisions=1; divisions<=16; divisions++) {
            if (divisions==1 || divisions==2 || divisions==4 || divisions==8 || divisions==16) {
                for (int beats=1; beats<=16; beats++) {
                    timesigs.add(beats + "/" + divisions);
                }
            }
        }
        ExposedDropDownArrayAdapter timesigArrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),
                R.layout.view_exposed_dropdown_item, timesigs);
        exposedDropDownSelection.keepSelectionPosition(myView.timesig, timesigs);
        myView.timesig.setAdapter(timesigArrayAdapter);
        myView.timesig.setText(mainActivityInterface.getTempSong().getTimesig());

        // The midi, abc and customchords
        myView.midi.setText(mainActivityInterface.getTempSong().getMidi());
        myView.abc.setText(mainActivityInterface.getTempSong().getAbc());
        myView.customChords.setText(mainActivityInterface.getTempSong().getCustomchords());
    }

    private void setupListeners() {

    }

    private String niceTextFromPref(String prefText) {
        switch (prefText) {
            default:
                return "";
            case "auto":
                return getString(R.string.pad_auto);
            case "link":
                return getString(R.string.link_audio);
            case "custom":
                return getString(R.string.custom);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
