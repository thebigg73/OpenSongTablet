package com.garethevans.church.opensongtablet.highlighter;

import android.annotation.SuppressLint;
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsHighlighterBinding.inflate(inflater,container,false);
        mainActivityInterface.updateToolbar(getString(R.string.highlight));

        // Set current values
        setupViews();

        // Set listeners
        setListeners();

        return myView.getRoot();
    }

    private void setupViews() {
        boolean drawingAutoDisplay = mainActivityInterface.getPreferences().getMyPreferenceBoolean(requireContext(),"drawingAutoDisplay",true);
        myView.autoShowHighlight.setChecked(drawingAutoDisplay);
        hideView(myView.timeToDisplayHighlighter,drawingAutoDisplay);
        int timeToDisplayHighlighter = mainActivityInterface.getPreferences().getMyPreferenceInt(requireContext(),"timeToDisplayHighlighter",0);
        myView.timeToDisplayHighlighter.setValue(timeToDisplayHighlighter);
        setHintTime(timeToDisplayHighlighter);
        hideView(myView.edit,mainActivityInterface.getMode().equals("Performance"));
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
            s = getString(R.string.on);
        } else {
            s = timeToDisplayHighlighter + "s";
        }
        myView.timeToDisplayHighlighter.setHint(s);
    }

    private void setListeners() {
        myView.autoShowHighlight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hideView(myView.timeToDisplayHighlighter,isChecked);
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"drawingAutoDisplay",isChecked);
        });
        myView.timeToDisplayHighlighter.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) { }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getPreferences().setMyPreferenceInt(requireContext(),"timeToDisplayHighlighter",Math.round(myView.timeToDisplayHighlighter.getValue()));
            }
        });
        myView.timeToDisplayHighlighter.addOnChangeListener((slider, value, fromUser) -> setHintTime(Math.round(value)));

        myView.nestedScrollView.setFabToAnimate(myView.edit);
        
        myView.edit.setOnClickListener(v -> mainActivityInterface.navigateToFragment(null,R.id.highlighterEditFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
