package com.garethevans.church.opensongtablet.highlighter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.SettingsHighlighterBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

public class HighlighterFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsHighlighterBinding myView;
    private String highlight_string="", website_highlighter_string="", mode_performance_string="",
            on_string="";
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(highlight_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsHighlighterBinding.inflate(inflater,container,false);

        prepareStrings();

        webAddress = website_highlighter_string;

        // Set current values
        setupViews();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            highlight_string = getString(R.string.highlight);
            website_highlighter_string = getString(R.string.website_highlighter);
            mode_performance_string = getString(R.string.mode_performance);
            on_string = getString(R.string.on);
        }
    }

    private void setupViews() {
        boolean drawingAutoDisplay = mainActivityInterface.getPreferences().getMyPreferenceBoolean("drawingAutoDisplay",true);
        myView.autoShowHighlight.setChecked(drawingAutoDisplay);
        hideView(myView.timeToDisplayHighlighter,drawingAutoDisplay);
        int timeToDisplayHighlighter = mainActivityInterface.getPreferences().getMyPreferenceInt("timeToDisplayHighlighter",0);
        myView.timeToDisplayHighlighter.setValue(timeToDisplayHighlighter);
        myView.timeToDisplayHighlighter.setLabelFormatter(value -> ((int)value)+"s");
        setHintTime(timeToDisplayHighlighter);
        hideView(myView.edit,mainActivityInterface.getMode().equals(mode_performance_string));
        if (!mainActivityInterface.validScreenShotFile() && mainActivityInterface.getSong().getFiletype().equals("XML")) {
            myView.edit.hide();
        } else {
            myView.edit.show();
        }
        myView.warning.setVisibility((!mainActivityInterface.validScreenShotFile() && mainActivityInterface.getSong().getFiletype().equals("XML")) ? View.VISIBLE:View.GONE);
    }

    private void hideView(View view, boolean drawingAutoDisplay) {
        if (drawingAutoDisplay) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }
    private void setHintTime(int timeToDisplayHighlighter) {
        String s;
        if (timeToDisplayHighlighter == 0) {
            s = on_string;
        } else {
            s = timeToDisplayHighlighter + "s";
        }
        myView.timeToDisplayHighlighter.setHint(s);
    }

    private void setListeners() {
        myView.autoShowHighlight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hideView(myView.timeToDisplayHighlighter,isChecked);
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("drawingAutoDisplay",isChecked);
        });
        myView.timeToDisplayHighlighter.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) { }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getPreferences().setMyPreferenceInt("timeToDisplayHighlighter",Math.round(myView.timeToDisplayHighlighter.getValue()));
            }
        });
        myView.timeToDisplayHighlighter.addOnChangeListener((slider, value, fromUser) -> setHintTime(Math.round(value)));

        myView.nestedScrollView.setFabToAnimate(myView.edit);
        
        myView.edit.setOnClickListener(v -> {
            if (mainActivityInterface.validScreenShotFile() || !mainActivityInterface.getSong().getFiletype().equals("XML")) {
                mainActivityInterface.navigateToFragment(null, R.id.highlighterEditFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
