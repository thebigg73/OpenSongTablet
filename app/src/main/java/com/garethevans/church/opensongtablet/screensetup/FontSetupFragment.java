package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class FontSetupFragment extends Fragment {

    private SettingsFontsBinding myView;
    private ArrayList<String> fontNames;
    private String fontLyric, fontChord, fontPreso, fontPresoInfo, fontSticky, which;
    private MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsFontsBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(getString(R.string.font_choose));

        getPreferences();

        new Thread(() -> {

            // Got the fonts from Google
            fontNames = mainActivityInterface.getMyFonts().getFontsFromGoogle();

            try {
                requireActivity().runOnUiThread(() -> {
                    // Set up the previews
                    initialisePreviews();
                    updatePreviews();

                    // Set the drop down lists
                    setupDropDowns();

                    // Set the buttons that open the web preview selector
                    setWebButtonListeners();
                });
            } catch (Exception e) {
                e.printStackTrace();
                // The user likely left before this completed
            }
        }).start();

        return myView.getRoot();
    }

    private void getPreferences() {
        fontLyric = mainActivityInterface.getPreferences().getMyPreferenceString(getContext(),"fontLyric","Lato");
        fontChord = mainActivityInterface.getPreferences().getMyPreferenceString(getContext(),"fontChord","Lato");
        fontPreso = mainActivityInterface.getPreferences().getMyPreferenceString(getContext(),"fontPreso","Lato");
        fontPresoInfo = mainActivityInterface.getPreferences().getMyPreferenceString(getContext(),"fontPresoInfo","Lato");
        fontSticky = mainActivityInterface.getPreferences().getMyPreferenceString(getContext(),"fontSticky","Lato");
        mainActivityInterface.getMyThemeColors().getDefaultColors(getContext(),mainActivityInterface);
    }

    private void setupDropDowns() {
        prepareExposedDropdown("fontLyric", myView.lyricFont, fontLyric);
        prepareExposedDropdown("fontChord", myView.chordFont, fontChord);
        prepareExposedDropdown("fontSticky", myView.presoFont, fontSticky);
        prepareExposedDropdown("fontPreso", myView.stickyFont, fontPreso);
        prepareExposedDropdown("fontPresoInfo", myView.presoInfoFont, fontPresoInfo);
    }

    private void prepareExposedDropdown(String which, ExposedDropDown exposedDropDown, String defaultValue) {
        if (exposedDropDown!=null && isAdded()) {
            try {
                exposedDropDown.post(() -> {
                    ExposedDropDownArrayAdapter exposedDropDownArrayAdapter =
                            new ExposedDropDownArrayAdapter(requireContext(), exposedDropDown,
                                    R.layout.view_exposed_dropdown_item, fontNames);
                    exposedDropDown.setAdapter(exposedDropDownArrayAdapter);
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
        try {
            myView.songPreview.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
            myView.lyricPreview.setTextColor(mainActivityInterface.getMyThemeColors().getLyricsTextColor());
            myView.chordPreview.setTextColor(mainActivityInterface.getMyThemeColors().getLyricsChordsColor());
            myView.lyricPreview.setTextSize(24.0f);
            myView.chordPreview.setTextSize(24.0f * mainActivityInterface.getPreferences().getMyPreferenceFloat(getContext(), "scaleChords", 0.8f));

            // Set the presentation preview
            myView.presoPreview.setBackground(ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.preso_default_bg, null));
            myView.presoLorem.setTextColor(mainActivityInterface.getMyThemeColors().getPresoFontColor());
            myView.presoInfoLorem.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
            myView.presoInfoLorem.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
            myView.presoLorem.setTextSize(24.0f);
            myView.presoInfoLorem.setTextSize(24.0f * 0.5f);
            myView.presoLorem.setGravity(mainActivityInterface.getPreferences().getMyPreferenceInt(getContext(), "presoInfoAlign", Gravity.CENTER));
            myView.presoInfoLorem.setGravity(mainActivityInterface.getPreferences().getMyPreferenceInt(getContext(), "presoInfoAlign", Gravity.END));

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
                });
                new Thread(runnable).start();
                new Handler().postDelayed(runnable,500);
                new Handler().postDelayed(runnable,3000);
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
        checkInternet.checkConnection(this,R.id.fontSetupFragment,mainActivityInterface);
    }

    public void isConnected(boolean connected) {
        if (connected) {
            // Because this is called from another thread, we need to post back via the UIThread
            new Thread(() -> requireActivity().runOnUiThread(() -> {
                mainActivityInterface.setWhattodo(which);
                mainActivityInterface.navigateToFragment(null, R.id.fontSetupPreviewFragment);
            })).start();
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
            mainActivityInterface.getMyFonts().changeFont(getContext(),mainActivityInterface,which,s.toString(),new Handler());
            updatePreviews();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
