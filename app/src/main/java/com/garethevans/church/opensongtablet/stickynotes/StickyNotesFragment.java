package com.garethevans.church.opensongtablet.stickynotes;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsStickynotesBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class StickyNotesFragment extends Fragment {

    SettingsStickynotesBinding myView;
    MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsStickynotesBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(getString(R.string.stickynotes));

        // Set up the views
        setupViews();

        // Set up listeners
        setupListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        myView.stickyNotes.setText(mainActivityInterface.getSong().getNotes());
        myView.autoShowSticky.setChecked(mainActivityInterface.getPreferences().
                getMyPreferenceBoolean(requireContext(),"stickyAuto",true));
        int time = mainActivityInterface.getPreferences().
                getMyPreferenceInt(requireContext(),"timeToDisplaySticky",0);
        myView.timeSeekBar.setProgress(time);
        progressToTime(time);
        float alpha = mainActivityInterface.getPreferences().
                getMyPreferenceFloat(requireContext(),"stickyAlpha",0.8f);
        myView.alphaSeekBar.setProgress(alphaToProgress(alpha));
    }

    private void progressToTime(int time) {
        String val = "s";
        if (time==0) {
            val = getString(R.string.on);
        } else {
            val = time + val;
        }
        myView.timeText.setText(val);
    }

    private int alphaToProgress(float alpha) {
        int val = (int)(alpha*100.0f);
        String s = val + "%";
        // Min seekbar position is 10;
        myView.alphaText.setText(s);
        return val-10;
    }
    private float progressToAlphaText(int progress) {
        // Min seekbar position is 0, but is worth 10;
        String s = (progress+10) + "%";
        myView.alphaText.setText(s);
        return ((float)progress+10.0f)/100.0f;
    }

    private void setupListeners() {
        myView.autoShowSticky.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"stickyAuto",isChecked));
        myView.saveButton.setOnClickListener(v -> {
            if (myView.stickyNotes.getText()!=null) {
                mainActivityInterface.getSong().setNotes(myView.stickyNotes.getText().toString());
                mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.success));
                // TODO Save the actual song file
            } else {
                mainActivityInterface.getShowToast().doIt(requireContext(),getString(R.string.error));
            }
        });
        myView.alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressToAlphaText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float pref = progressToAlphaText(seekBar.getProgress());
                mainActivityInterface.getPreferences().setMyPreferenceFloat(
                        requireContext(), "stickyAlpha", pref);
            }
        });
        myView.timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressToTime(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                progressToTime(progress);
                mainActivityInterface.getPreferences().setMyPreferenceInt(
                        requireContext(),"timeToDisplaySticky",progress);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
