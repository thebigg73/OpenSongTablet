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
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.appdata.SetTypeFace;
import com.garethevans.church.opensongtablet.databinding.SettingsFontsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

import java.util.ArrayList;

public class FontSetupFragment extends Fragment {

    SettingsFontsBinding myView;
    Preferences preferences;
    SetTypeFace setTypeFace;
    ThemeColors themeColors;

    ArrayList<String> fontNames;
    ExposedDropDownArrayAdapter arrayAdapter;
    String fontLyric, fontChord, fontPreso, fontPresoInfo, fontSticky;

    MainActivityInterface mainActivityInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsFontsBinding.inflate(inflater, container, false);

        mainActivityInterface.updateToolbar(null,getString(R.string.settings) + " / " + getString(R.string.display) + " / " + getString(R.string.choose_fonts));

        setHelpers();
        getPreferences();

        new Thread(() -> {


            // Got the fonts from Google
            fontNames = setTypeFace.getFontsFromGoogle();

            requireActivity().runOnUiThread(() -> {
                // Set up the previews
                initialisePreviews();
                updatePreviews();

                // Set the drop down lists
                setupDropDowns();

                // Set the buttons that open the web preview selector
                setWebButtonListeners();
            });
        }).start();


        return myView.getRoot();
    }

    private void setHelpers() {
        preferences = new Preferences();
        setTypeFace = mainActivityInterface.getMyFonts();
        themeColors = mainActivityInterface.getMyThemeColors();
    }

    private void getPreferences() {
        Log.d("FontSetup","Loading preferences");
        fontLyric = preferences.getMyPreferenceString(getContext(),"fontLyric","Lato");
        fontChord = preferences.getMyPreferenceString(getContext(),"fontChord","Lato");
        fontPreso = preferences.getMyPreferenceString(getContext(),"fontPreso","Lato");
        fontPresoInfo = preferences.getMyPreferenceString(getContext(),"fontPresoInfo","Lato");
        fontSticky = preferences.getMyPreferenceString(getContext(),"fontSticky","Lato");
        themeColors.getDefaultColors(getContext(),preferences);
    }

    private void setupDropDowns() {
        arrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.exposed_dropdown,fontNames);
        myView.lyricFont.setAdapter(arrayAdapter);
        myView.chordFont.setAdapter(arrayAdapter);
        myView.presoFont.setAdapter(arrayAdapter);
        myView.presoInfoFont.setAdapter(arrayAdapter);
        myView.stickyFont.setAdapter(arrayAdapter);

        myView.lyricFont.setText(fontLyric);
        myView.chordFont.setText(fontChord);
        myView.stickyFont.setText(fontSticky);
        myView.presoFont.setText(fontPreso);
        myView.presoInfoFont.setText(fontPresoInfo);

        myView.lyricFont.addTextChangedListener(new MyTextWatcher("fontLyric"));
        myView.chordFont.addTextChangedListener(new MyTextWatcher("fontChord"));
        myView.stickyFont.addTextChangedListener(new MyTextWatcher("fontSticky"));
        myView.presoFont.addTextChangedListener(new MyTextWatcher("fontPreso"));
        myView.presoInfoFont.addTextChangedListener(new MyTextWatcher("fontPresoInfo"));
    }

    private void setWebButtonListeners() {
        myView.lyricWebPreview.setOnClickListener(v -> openWebPreview("fontLyric"));
        myView.chordWebPreview.setOnClickListener(v -> openWebPreview("fontChord"));
        myView.presoWebPreview.setOnClickListener(v -> openWebPreview("fontPreso"));
        myView.presoInfoWebPreview.setOnClickListener(v -> openWebPreview("fontPresoInfo"));
        myView.stickyWebPreview.setOnClickListener(v -> openWebPreview("fontSticky"));
    }

    private void initialisePreviews() {
        // Set up the song preview
        myView.songPreview.setBackgroundColor(themeColors.getLyricsBackgroundColor());
        myView.lyricPreview.setTextColor(themeColors.getLyricsTextColor());
        myView.chordPreview.setTextColor(themeColors.getLyricsChordsColor());
        myView.lyricPreview.setTextSize(24.0f);
        myView.chordPreview.setTextSize(24.0f*preferences.getMyPreferenceFloat(getContext(),"scaleChords",0.8f));

        // Set the presentation preview
        myView.presoPreview.setBackground(ResourcesCompat.getDrawable(getContext().getResources(),R.drawable.preso_default_bg,null));
        myView.presoLorem.setTextColor(themeColors.getPresoFontColor());
        myView.presoInfoLorem.setTextColor(themeColors.getPresoInfoFontColor());
        myView.presoInfoLorem.setTextColor(themeColors.getPresoInfoFontColor());
        myView.presoLorem.setTextSize(24.0f);
        myView.presoInfoLorem.setTextSize(24.0f*0.5f);
        myView.presoLorem.setGravity(preferences.getMyPreferenceInt(getContext(),"presoInfoAlign", Gravity.CENTER));
        myView.presoInfoLorem.setGravity(preferences.getMyPreferenceInt(getContext(),"presoInfoAlign", Gravity.END));

        // Set the sticky preview
        myView.stickyLorem.setBackgroundColor(themeColors.getStickyBackgroundColor());
        myView.stickyLorem.setTextSize(22.0f);
        myView.stickyLorem.setTextColor(themeColors.getStickyTextColor());
    }

    private void updatePreviews() {
        // Set up the song preview
        myView.lyricPreview.setTypeface(setTypeFace.getLyricFont());
        myView.chordPreview.setTypeface(setTypeFace.getChordFont());

        // Set the presentation preview
        myView.presoLorem.setTypeface(setTypeFace.getPresoFont());
        myView.presoInfoLorem.setTypeface(setTypeFace.getPresoInfoFont());

        // Set the sticky preview
        myView.stickyLorem.setTypeface(setTypeFace.getStickyFont());
    }

    private void openWebPreview(String which) {
        // Only allow if an internet connection is detected
        new CheckInternet(internet -> {
            StaticVariables.whattodo = which;
            mainActivityInterface.navigateToFragment(R.id.fontSetupPreviewFragment);
        });
    }

    private class MyTextWatcher implements TextWatcher {

        String which;

        MyTextWatcher(String which) {
            this.which = which;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // The preview method in setTypeFace deals with saving
            setTypeFace.changeFont(getContext(),preferences,which,s.toString(),new Handler());
            updatePreviews();
        }

        @Override
        public void afterTextChanged(Editable s) { }
    }
}
