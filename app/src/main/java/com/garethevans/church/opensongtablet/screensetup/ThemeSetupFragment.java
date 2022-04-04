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
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsThemeBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class ThemeSetupFragment extends Fragment {

    private MainActivityInterface mainActivityInterface;
    private SettingsThemeBinding myView;

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

        mainActivityInterface.updateToolbar(getString(R.string.theme));
        mainActivityInterface.updateToolbarHelp(getString(R.string.website_themes));

        // Initialise the themes
        setUpTheme();

        // Set the button listeners
        setListeners();

        return myView.getRoot();
    }

    private void setUpTheme() {
        themes = new ArrayList<>();
        themes.add(getString(R.string.theme_dark));
        themes.add(getString(R.string.theme_light));
        themes.add(getString(R.string.theme_custom1));
        themes.add(getString(R.string.theme_custom2));

        ExposedDropDownArrayAdapter arrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),R.layout.view_exposed_dropdown_item,themes);
        myTheme = mainActivityInterface.getPreferences().getMyPreferenceString("appTheme","dark");
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
                mainActivityInterface.getPreferences().setMyPreferenceString("appTheme",myTheme);
                updateColors();
                updateButtons();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        updateColors();
        updateButtons();
    }

    public void updateColors() {
        mainActivityInterface.getMyThemeColors().getDefaultColors();
    }

    public void updateButtons() {
        myView.pageButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor(), PorterDuff.Mode.SRC_IN));
        myView.lyricsButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getLyricsTextColor(), PorterDuff.Mode.SRC_IN));
        myView.chordsButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getLyricsChordsColor(), PorterDuff.Mode.SRC_IN));
        myView.capoButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getLyricsCapoColor(), PorterDuff.Mode.SRC_IN));

        myView.verseButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getLyricsVerseColor(), PorterDuff.Mode.SRC_IN));
        myView.chorusButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getLyricsChorusColor(), PorterDuff.Mode.SRC_IN));
        myView.bridgeButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getLyricsBridgeColor(), PorterDuff.Mode.SRC_IN));
        myView.commentButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getLyricsCommentColor(), PorterDuff.Mode.SRC_IN));
        myView.prechorusButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getLyricsPreChorusColor(), PorterDuff.Mode.SRC_IN));
        myView.tagButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getLyricsTagColor(), PorterDuff.Mode.SRC_IN));
        myView.customButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getLyricsCustomColor(), PorterDuff.Mode.SRC_IN));
        myView.chordHighlighting.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getHighlightChordColor(), PorterDuff.Mode.SRC_IN));
        myView.titleHighlighting.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getHighlightHeadingColor(), PorterDuff.Mode.SRC_IN));

        myView.presoButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getPresoFontColor(), PorterDuff.Mode.SRC_IN));
        myView.presoInfoButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor(), PorterDuff.Mode.SRC_IN));
        myView.presoAlertButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getPresoAlertColor(), PorterDuff.Mode.SRC_IN));
        myView.presoShadowButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getPresoShadowColor(), PorterDuff.Mode.SRC_IN));

        myView.metronomeButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getMetronomeColor(), PorterDuff.Mode.SRC_IN));
        myView.pagebuttonButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getPageButtonsColor(), PorterDuff.Mode.SRC_IN));
        myView.stickytextButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getStickyTextColor(), PorterDuff.Mode.SRC_IN));
        myView.stickybackgroundButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getStickyBackgroundColor(), PorterDuff.Mode.SRC_IN));
        myView.extratextButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getExtraInfoTextColor(), PorterDuff.Mode.SRC_IN));
        myView.extrabackgroundButton.getIcon().setColorFilter(new PorterDuffColorFilter(mainActivityInterface.getMyThemeColors().getExtraInfoBgColor(), PorterDuff.Mode.SRC_IN));
    }

    private void setListeners() {
        myView.lyricsButton.setOnClickListener(v-> chooseColor("lyricsTextColor"));
        myView.presoButton.setOnClickListener(v-> chooseColor("presoFontColor"));
        myView.presoInfoButton.setOnClickListener(v -> chooseColor("presoInfoFontColor"));
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
        myView.chordHighlighting.setOnClickListener(v -> chooseColor("highlightChordColor"));
        myView.titleHighlighting.setOnClickListener(v -> chooseColor("highlightHeadingColor"));
        myView.metronomeButton.setOnClickListener(v-> chooseColor("metronomeColor"));
        myView.pagebuttonButton.setOnClickListener(v-> chooseColor("pageButtonsColor"));
        myView.stickytextButton.setOnClickListener(v-> chooseColor("stickyTextColor"));
        myView.stickybackgroundButton.setOnClickListener(v-> chooseColor("stickyBackgroundColor"));
        myView.extratextButton.setOnClickListener(v-> chooseColor("extraInfoTextColor"));
        myView.extrabackgroundButton.setOnClickListener(v-> chooseColor("extraInfoBgColor"));
        myView.presoAlertButton.setOnClickListener(v -> chooseColor("presoAlertColor"));
        myView.presoShadowButton.setOnClickListener(v -> chooseColor("presoShadowColor"));
        myView.resetTheme.setOnClickListener(v -> mainActivityInterface.displayAreYouSure("resetColors",myView.themeName.getText().toString() + ": "+getString(R.string.reset_colours),null,"themeSetupFragment",this,null));
    }

    private void chooseColor(String which) {
        // This moves to the color chooser bottom sheet dialog fragment
        ChooseColorBottomSheet chooseColorBottomSheet = new ChooseColorBottomSheet(this,"themeSetupFragment",which);
        chooseColorBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"ChooseColorBottomSheet");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}
