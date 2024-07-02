package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.CheckInternet;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDown;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsFontsBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class FontSetupFragment extends Fragment {
    private SettingsFontsBinding myView;
    private ArrayList<String> fontNames;
    private String fontLyric, fontChord, fontPreso, fontPresoInfo, fontSticky, which;
    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private String font_choose_string="", website_fonts_string="";
    private String webAddress;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "FontSetupFragment";

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(font_choose_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        displayInterface = (DisplayInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsFontsBinding.inflate(inflater, container, false);

        prepareStrings();
        webAddress = website_fonts_string;

        getPreferences();
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Got the fonts from Google
            fontNames = mainActivityInterface.getMyFonts().getFontsFromGoogle();

            try {
                mainActivityInterface.getMainHandler().post(() -> {
                    if (fontNames!=null && !fontNames.isEmpty()) {
                        // Set up the previews
                        initialisePreviews();
                        updatePreviews();

                        // Set the drop down lists
                        setupDropDowns();

                        // Set the buttons that open the web preview selector
                        setWebButtonListeners();
                        hideDropdowns(false);
                    } else {
                        hideDropdowns(true);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // The user likely left before this completed
            }
        });

        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            font_choose_string = getString(R.string.font_choose);
            website_fonts_string = getString(R.string.website_fonts);
        }
    }
    private void hideDropdowns(boolean hide) {
        int visibility = View.VISIBLE;
        if (hide) {
            visibility = View.GONE;
        }
        myView.chordFont.setVisibility(visibility);
        myView.lyricFont.setVisibility(visibility);
        myView.presoFont.setVisibility(visibility);
        myView.presoInfoFont.setVisibility(visibility);
        myView.stickyFont.setVisibility(visibility);
    }

    private void getPreferences() {
        fontLyric = mainActivityInterface.getPreferences().getMyPreferenceString("fontLyric","Lato");
        fontChord = mainActivityInterface.getPreferences().getMyPreferenceString("fontChord","Lato");
        fontPreso = mainActivityInterface.getPreferences().getMyPreferenceString("fontPreso","Lato");
        fontPresoInfo = mainActivityInterface.getPreferences().getMyPreferenceString("fontPresoInfo","Lato");
        fontSticky = mainActivityInterface.getPreferences().getMyPreferenceString("fontSticky","Lato");
        mainActivityInterface.getMyThemeColors().getDefaultColors();
    }

    private void setupDropDowns() {
        prepareExposedDropdown("fontLyric", myView.lyricFont, fontLyric);
        prepareExposedDropdown("fontChord", myView.chordFont, fontChord);
        prepareExposedDropdown("fontSticky", myView.stickyFont, fontSticky);
        prepareExposedDropdown("fontPreso", myView.presoFont, fontPreso);
        prepareExposedDropdown("fontPresoInfo", myView.presoInfoFont, fontPresoInfo);
    }

    private void prepareExposedDropdown(String which, ExposedDropDown exposedDropDown, String defaultValue) {
        if (exposedDropDown!=null && isAdded()) {
            try {
                exposedDropDown.post(() -> {
                    if (getContext()!=null) {
                        ExposedDropDownArrayAdapter exposedDropDownArrayAdapter =
                                new ExposedDropDownArrayAdapter(getContext(), exposedDropDown,
                                        R.layout.view_exposed_dropdown_item, fontNames);
                        exposedDropDown.setAdapter(exposedDropDownArrayAdapter);
                    }
                    exposedDropDown.setText(defaultValue);
                    exposedDropDown.addTextChangedListener(new MyTextWatcher(which));
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setWebButtonListeners() {
        myView.lyricWebPreview.setOnClickListener(v -> openWebPreview("fontLyric"));
        myView.chordWebPreview.setOnClickListener(v -> openWebPreview("fontChord"));
        myView.presoWebPreview.setOnClickListener(v -> openWebPreview("fontPreso"));
        myView.presoInfoWebPreview.setOnClickListener(v -> openWebPreview("fontPresoInfo"));
        myView.stickyWebPreview.setOnClickListener(v -> openWebPreview("fontSticky"));
    }

    private void initialisePreviews() {
        myView.lyricWebPreview.setVisibility(mainActivityInterface.getAlertChecks().getHasPlayServices() ? View.VISIBLE:View.GONE);
        myView.chordWebPreview.setVisibility(mainActivityInterface.getAlertChecks().getHasPlayServices() ? View.VISIBLE:View.GONE);
        myView.presoWebPreview.setVisibility(mainActivityInterface.getAlertChecks().getHasPlayServices() ? View.VISIBLE:View.GONE);
        myView.presoInfoWebPreview.setVisibility(mainActivityInterface.getAlertChecks().getHasPlayServices() ? View.VISIBLE:View.GONE);
        myView.stickyWebPreview.setVisibility(mainActivityInterface.getAlertChecks().getHasPlayServices() ? View.VISIBLE:View.GONE);

        try {
            myView.songPreview.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
            myView.lyricPreview.setTextColor(mainActivityInterface.getMyThemeColors().getLyricsTextColor());
            myView.chordPreview.setTextColor(mainActivityInterface.getMyThemeColors().getLyricsChordsColor());
            myView.lyricPreview.setTextSize(24.0f);
            myView.chordPreview.setTextSize(24.0f * mainActivityInterface.getPreferences().getMyPreferenceFloat("scaleChords", 0.8f));

            // Set the presentation preview
            if (getContext()!=null) {
                myView.presoPreview.setBackground(ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.preso_default_bg, null));
            }
            myView.presoLorem.setTextColor(mainActivityInterface.getMyThemeColors().getPresoFontColor());
            myView.presoInfoLorem.setBackgroundColor(mainActivityInterface.getMyThemeColors().getPresoShadowColor());
            myView.presoInfoLorem.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
            myView.presoLorem.setTextSize(22.0f);
            myView.presoInfoLorem.setTextSize(22.0f * 0.5f);
            myView.presoLorem.setGravity(mainActivityInterface.getPreferences().getMyPreferenceInt("presoLyricsAlign", Gravity.CENTER_HORIZONTAL)|
                    mainActivityInterface.getPreferences().getMyPreferenceInt("presoLyricsVAlign", Gravity.CENTER_VERTICAL));
            myView.presoInfoLorem.setGravity(mainActivityInterface.getPreferences().getMyPreferenceInt("presoInfoAlign", Gravity.END));

            // Set the sticky preview
            myView.stickyLorem.setBackgroundColor(mainActivityInterface.getMyThemeColors().getStickyBackgroundColor());
            myView.stickyLorem.setTextSize(22.0f);
            myView.stickyLorem.setTextColor(mainActivityInterface.getMyThemeColors().getStickyTextColor());

            // Clicking on the previews will update them
            myView.songPreview.setOnClickListener(v -> updatePreviews());
            myView.presoPreview.setOnClickListener(v -> updatePreviews());
            myView.stickyLorem.setOnClickListener(v -> updatePreviews());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePreviews() {

        Log.d(TAG,"isAdded():"+isAdded());
        // Run this now and again in about 500ms and 3 seconds (to check loading of the font has happened)
        try {
            if (getActivity()!=null && isAdded()) {
                Runnable runnable = (() -> {
                    // Set up the song preview
                    myView.lyricPreview.post(() -> myView.lyricPreview.setTypeface(mainActivityInterface.getMyFonts().getLyricFont()));
                    myView.chordPreview.post(() -> myView.chordPreview.setTypeface(mainActivityInterface.getMyFonts().getChordFont()));

                    // Set the presentation preview
                    myView.presoLorem.post(() -> myView.presoLorem.setTypeface(mainActivityInterface.getMyFonts().getPresoFont()));
                    myView.presoInfoLorem.post(() -> myView.presoInfoLorem.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont()));

                    // Set the sticky preview
                    myView.stickyLorem.post(() -> myView.stickyLorem.setTypeface(mainActivityInterface.getMyFonts().getStickyFont()));

                    // Change the values in other used locations
                    displayInterface.updateDisplay("setInfoStyles");
                    displayInterface.updateDisplay("newSongLoaded");
                    displayInterface.updateDisplay("setSongContent");
                });
                mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                    mainActivityInterface.getMainHandler().post(runnable);
                    mainActivityInterface.getMainHandler().postDelayed(runnable,500);
                    mainActivityInterface.getMainHandler().postDelayed(runnable,3000);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openWebPreview(String which) {
        this.which = which;
        // Only allow if an internet connection is detected
        // This then sends the result to isConnected
        CheckInternet checkInternet = new CheckInternet();
        checkInternet.checkConnection(getContext(),this,R.id.fontSetupFragment,mainActivityInterface);
    }

    public void isConnected(boolean connected) {
        if (connected) {
            // Because this is called from another thread, we need to post back via the UIThread
            mainActivityInterface.getThreadPoolExecutor().execute(() -> mainActivityInterface.getMainHandler().post(() -> {
                mainActivityInterface.setWhattodo(which);
                mainActivityInterface.navigateToFragment(null, R.id.fontSetupPreviewFragment);
            }));
        }
    }

    private class MyTextWatcher implements TextWatcher {

        String which;

        MyTextWatcher(String which) {
            this.which = which;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            // The preview method in setTypeFace deals with saving
            Log.d(TAG,"which:"+which+"  to:"+s);
            mainActivityInterface.getMyFonts().changeFont(which,s.toString(),new Handler());
            updatePreviews();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
