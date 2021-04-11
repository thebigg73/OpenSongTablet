package com.garethevans.church.opensongtablet.highlighter;

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
import com.garethevans.church.opensongtablet.databinding.SettingsHighlighterBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

public class HighlighterFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsHighlighterBinding myView;
    private Preferences preferences;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsHighlighterBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(null,getString(R.string.highlight));

        // Set helpers
        setupHelpers();

        // Set current values
        setupViews();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void setupHelpers() {
        preferences = mainActivityInterface.getPreferences();
    }

    private void setupViews() {
        boolean drawingAutoDisplay = preferences.getMyPreferenceBoolean(requireContext(),"drawingAutoDisplay",true);
        myView.autoShowHighlight.setChecked(drawingAutoDisplay);
        hideView(myView.highlighterTimeLayout,drawingAutoDisplay);
        int timeToDisplayHighlighter = preferences.getMyPreferenceInt(requireContext(),"timeToDisplayHighlighter",0);
        myView.timeToDisplayHighlighter.setProgress(timeToDisplayHighlighter);
        setTimeText(timeToDisplayHighlighter);
        hideView(myView.edit,mainActivityInterface.getMode().equals("Performance"));
    }

    private void hideView(View view, boolean drawingAutoDisplay) {
        if (drawingAutoDisplay) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }
    private void setTimeText(int timeToDisplayHighlighter) {
        String s = getString(R.string.time) + "\n";
        if (timeToDisplayHighlighter == 0) {
            s += "(" + getString(R.string.on) + ")";
        } else {
            s += "(" + timeToDisplayHighlighter + " s)";
        }
        myView.displayTime.setText(s);
    }

    private void setListeners() {
        myView.autoShowHighlight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hideView(myView.highlighterTimeLayout,isChecked);
            preferences.setMyPreferenceBoolean(requireContext(),"drawingAutoDisplay",isChecked);
        });
        myView.timeToDisplayHighlighter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                preferences.setMyPreferenceInt(requireContext(),"timeToDisplayHighlighter",progress);
                setTimeText(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        myView.edit.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.highlighterEditFragment));
    }
}
