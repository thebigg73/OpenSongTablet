package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsThemeBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

import java.util.ArrayList;
import java.util.Map;

public class ThemeSetupFragment extends Fragment {

    MainActivityInterface mainActivityInterface;
    SettingsThemeBinding myView;

    private Preferences preferences;
    private ThemeColors themeColors;

    private int lyricsColor, chordsColor, capoColor, pageColor, verseColor, chorusColor, prechorusColor,
            tagColor, bridgeColor, customColor, commentColor, presoColor, metronomeColor, pagebuttonColor,
            stickytextColor, stickybackgroundColor, extraInfoTextColor, extraInfoBgColor;
    private String myTheme;
    private ArrayList<String> themes;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsThemeBinding.inflate(inflater,container,false);

        // Set up the helper classes
        setUpHelpers();

        // Initialise the themes
        setUpTheme();

        // Set the button listeners
        setListeners();

        return myView.getRoot();
    }

    private void setUpHelpers() {
        preferences = new Preferences();
        themeColors = new ThemeColors();
    }
    private void setUpTheme() {
        themes = new ArrayList<>();
        themes.add(getContext().getResources().getString(R.string.dark_theme));
        themes.add(getContext().getResources().getString(R.string.light_theme));
        themes.add(getContext().getResources().getString(R.string.custom1_theme));
        themes.add(getContext().getResources().getString(R.string.custom2_theme));

        ExposedDropDownArrayAdapter arrayAdapter = new ExposedDropDownArrayAdapter(getActivity(),R.layout.exposed_dropdown,themes);
        myTheme = preferences.getMyPreferenceString(getContext(),"appTheme","dark");
        switch (myTheme) {
            case "dark":
            default:
                myView.themeName.setText(themes.get(0));
                break;
            case "light":
                myView.themeName.setText(themes.get(1));
                break;
            case "custom1":
                myView.themeName.setText(themes.get(2));
                break;
            case "custom2":
                myView.themeName.setText(themes.get(3));
                break;
        }
        myView.themeName.setAdapter(arrayAdapter);
        myView.themeName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s!=null && s.toString().equals(themes.get(0))) {
                    myTheme = "dark";
                } else if (s!=null && s.toString().equals(themes.get(1))) {
                    myTheme = "light";
                } else if (s!=null && s.toString().equals(themes.get(2))) {
                    myTheme = "custom1";
                } else if (s!=null && s.toString().equals(themes.get(3))) {
                    myTheme = "custom2";
                }
                preferences.setMyPreferenceString(getContext(),"appTheme",myTheme);
                updateColors();
                updateButtons();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        updateColors();
        updateButtons();
    }

    private void updateColors() {
        Map<String, Integer> colors = themeColors.getDefaultColors(getContext(), preferences);
        pageColor = colors.get("lyricsBackgroundColor");
        lyricsColor = colors.get("lyricsTextColor");
        chordsColor = colors.get("lyricsChordsColor");
        capoColor = colors.get("lyricsCapoColor");

        verseColor = colors.get("lyricsVerseColor");
        chorusColor = colors.get("lyricsChorusColor");
        bridgeColor = colors.get("lyricsBridgeColor");
        commentColor = colors.get("lyricsCommentColor");
        prechorusColor = colors.get("lyricsPreChorusColor");
        tagColor = colors.get("lyricsTagColor");
        customColor = colors.get("lyricsCustomColor");

        presoColor = colors.get("presoFontColor");

        metronomeColor = colors.get("metronomeColor");
        pagebuttonColor = colors.get("pageButtonsColor");
        stickytextColor = colors.get("stickyTextColor");
        stickybackgroundColor = colors.get("stickyBackgroundColor");
        extraInfoTextColor = colors.get("extraInfoTextColor");
        extraInfoBgColor = colors.get("extraInfoBgColor");
    }

    private void updateButtons() {
        myView.pageButton.getIcon().setColorFilter(new PorterDuffColorFilter(pageColor, PorterDuff.Mode.SRC_IN));
        myView.lyricsButton.getIcon().setColorFilter(new PorterDuffColorFilter(lyricsColor, PorterDuff.Mode.SRC_IN));
        myView.chordsButton.getIcon().setColorFilter(new PorterDuffColorFilter(chordsColor, PorterDuff.Mode.SRC_IN));
        myView.capoButton.getIcon().setColorFilter(new PorterDuffColorFilter(capoColor, PorterDuff.Mode.SRC_IN));

        myView.verseButton.getIcon().setColorFilter(new PorterDuffColorFilter(verseColor, PorterDuff.Mode.SRC_IN));
        myView.chorusButton.getIcon().setColorFilter(new PorterDuffColorFilter(chorusColor, PorterDuff.Mode.SRC_IN));
        myView.bridgeButton.getIcon().setColorFilter(new PorterDuffColorFilter(bridgeColor, PorterDuff.Mode.SRC_IN));
        myView.commentButton.getIcon().setColorFilter(new PorterDuffColorFilter(commentColor, PorterDuff.Mode.SRC_IN));
        myView.prechorusButton.getIcon().setColorFilter(new PorterDuffColorFilter(prechorusColor, PorterDuff.Mode.SRC_IN));
        myView.tagButton.getIcon().setColorFilter(new PorterDuffColorFilter(tagColor, PorterDuff.Mode.SRC_IN));
        myView.customButton.getIcon().setColorFilter(new PorterDuffColorFilter(customColor, PorterDuff.Mode.SRC_IN));

        myView.presoButton.getIcon().setColorFilter(new PorterDuffColorFilter(presoColor, PorterDuff.Mode.SRC_IN));

        myView.metronomeButton.getIcon().setColorFilter(new PorterDuffColorFilter(metronomeColor, PorterDuff.Mode.SRC_IN));
        myView.pagebuttonButton.getIcon().setColorFilter(new PorterDuffColorFilter(pagebuttonColor, PorterDuff.Mode.SRC_IN));
        myView.stickytextButton.getIcon().setColorFilter(new PorterDuffColorFilter(stickytextColor, PorterDuff.Mode.SRC_IN));
        myView.stickybackgroundButton.getIcon().setColorFilter(new PorterDuffColorFilter(stickybackgroundColor, PorterDuff.Mode.SRC_IN));
        myView.extratextButton.getIcon().setColorFilter(new PorterDuffColorFilter(extraInfoTextColor, PorterDuff.Mode.SRC_IN));
        myView.extrabackgroundButton.getIcon().setColorFilter(new PorterDuffColorFilter(extraInfoBgColor, PorterDuff.Mode.SRC_IN));
    }

    private void setListeners() {
        myView.lyricsButton.setOnClickListener(v-> chooseColor("lyricsTextColor"));
        myView.presoButton.setOnClickListener(v-> chooseColor("presoFontColor"));
        myView.chordsButton.setOnClickListener(v-> chooseColor("lyricsChordsColor"));
        myView.capoButton.setOnClickListener(v-> chooseColor("lyricsCapoColor"));
        myView.pageButton.setOnClickListener(v-> chooseColor("lyricsBackgroundColor"));
        myView.verseButton.setOnClickListener(v-> chooseColor("lyricsVerseColor"));
        myView.chorusButton.setOnClickListener(v-> chooseColor("lyricsChorusColor"));
        myView.prechorusButton.setOnClickListener(v-> chooseColor("lyricsPreChorusColor"));
        myView.bridgeButton.setOnClickListener(v-> chooseColor("lyricsBridgeColor"));
        myView.tagButton.setOnClickListener(v-> chooseColor("lyricsTagColor"));
        myView.commentButton.setOnClickListener(v-> chooseColor("lyricsCommentColor"));
        myView.customButton.setOnClickListener(v-> chooseColor("lyricsCustomColor"));
        myView.metronomeButton.setOnClickListener(v-> chooseColor("metronomeColor"));
        myView.pagebuttonButton.setOnClickListener(v-> chooseColor("pageButtonsColor"));
        myView.stickytextButton.setOnClickListener(v-> chooseColor("stickyTextColor"));
        myView.stickybackgroundButton.setOnClickListener(v-> chooseColor("stickyBackgroundColor"));
        myView.extratextButton.setOnClickListener(v-> chooseColor("extraInfoTextColor"));
        myView.extrabackgroundButton.setOnClickListener(v-> chooseColor("extraInfoBgColor"));

        /*dark_presoShadowColor           int         The color for the presentation text shadow in the dark theme
        dark_presoInfoColor             int         The color for the presentation info text in the dark theme
        dark_presoAlertColor            int         The color for the presentation alert text in the dark theme*/
    }

    private void chooseColor(String which) {
        // This moves to the color chooser fragment
        StaticVariables.whattodo = which;
        mainActivityInterface.navigateToFragment(R.id.chooseColorFragment);
    }

}
