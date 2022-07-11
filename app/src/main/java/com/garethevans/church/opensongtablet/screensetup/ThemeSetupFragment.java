package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
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
        myView.pageButton.setColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
        myView.lyricsButton.setColor(mainActivityInterface.getMyThemeColors().getLyricsTextColor());
        myView.chordsButton.setColor(mainActivityInterface.getMyThemeColors().getLyricsChordsColor());
        myView.capoButton.setColor(mainActivityInterface.getMyThemeColors().getLyricsCapoColor());

        myView.verseButton.setColor(mainActivityInterface.getMyThemeColors().getLyricsVerseColor());
        myView.chorusButton.setColor(mainActivityInterface.getMyThemeColors().getLyricsChorusColor());
        myView.bridgeButton.setColor(mainActivityInterface.getMyThemeColors().getLyricsBridgeColor());
        myView.commentButton.setColor(mainActivityInterface.getMyThemeColors().getLyricsCommentColor());
        myView.prechorusButton.setColor(mainActivityInterface.getMyThemeColors().getLyricsPreChorusColor());
        myView.tagButton.setColor(mainActivityInterface.getMyThemeColors().getLyricsTagColor());
        myView.customButton.setColor(mainActivityInterface.getMyThemeColors().getLyricsCustomColor());
        myView.chordHighlighting.setColor(mainActivityInterface.getMyThemeColors().getHighlightChordColor());
        myView.titleHighlighting.setColor(mainActivityInterface.getMyThemeColors().getHighlightHeadingColor());

        myView.presoButton.setColor(mainActivityInterface.getMyThemeColors().getPresoFontColor());
        myView.presoInfoButton.setColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        myView.presoAlertButton.setColor(mainActivityInterface.getMyThemeColors().getPresoAlertColor());
        myView.presoShadowButton.setColor(mainActivityInterface.getMyThemeColors().getPresoShadowColor());

        myView.metronomeButton.setColor(mainActivityInterface.getMyThemeColors().getMetronomeColor());
        myView.pagebuttonButton.setColor(mainActivityInterface.getMyThemeColors().getPageButtonsColor());
        myView.stickytextButton.setColor(mainActivityInterface.getMyThemeColors().getStickyTextColor());
        myView.stickybackgroundButton.setColor(mainActivityInterface.getMyThemeColors().getStickyBackgroundColor());
        myView.extratextButton.setColor(mainActivityInterface.getMyThemeColors().getExtraInfoTextColor());
        myView.extrabackgroundButton.setColor(mainActivityInterface.getMyThemeColors().getExtraInfoBgColor());
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
