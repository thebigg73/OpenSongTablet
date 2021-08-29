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
        ExposedDropDownArrayAdapter lyricAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.lyricFont, R.layout.view_exposed_dropdown_item, fontNames);
        ExposedDropDownArrayAdapter chordAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.chordFont, R.layout.view_exposed_dropdown_item, fontNames);
        ExposedDropDownArrayAdapter presoAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.presoFont, R.layout.view_exposed_dropdown_item, fontNames);
        ExposedDropDownArrayAdapter presoInfoAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.presoInfoFont, R.layout.view_exposed_dropdown_item, fontNames);
        ExposedDropDownArrayAdapter stickyAdapter = new ExposedDropDownArrayAdapter(requireContext(), myView.stickyFont, R.layout.view_exposed_dropdown_item, fontNames);

        myView.lyricFont.setAdapter(lyricAdapter);
        myView.chordFont.setAdapter(chordAdapter);
        myView.presoFont.setAdapter(presoAdapter);
        myView.presoInfoFont.setAdapter(presoInfoAdapter);
        myView.stickyFont.setAdapter(stickyAdapter);

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
        myView.songPreview.setBackgroundColor(mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor());
        myView.lyricPreview.setTextColor(mainActivityInterface.getMyThemeColors().getLyricsTextColor());
        myView.chordPreview.setTextColor(mainActivityInterface.getMyThemeColors().getLyricsChordsColor());
        myView.lyricPreview.setTextSize(24.0f);
        myView.chordPreview.setTextSize(24.0f*mainActivityInterface.getPreferences().getMyPreferenceFloat(getContext(),"scaleChords",0.8f));

        // Set the presentation preview
        myView.presoPreview.setBackground(ResourcesCompat.getDrawable(requireContext().getResources(),R.drawable.preso_default_bg,null));
        myView.presoLorem.setTextColor(mainActivityInterface.getMyThemeColors().getPresoFontColor());
        myView.presoInfoLorem.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        myView.presoInfoLorem.setTextColor(mainActivityInterface.getMyThemeColors().getPresoInfoFontColor());
        myView.presoLorem.setTextSize(24.0f);
        myView.presoInfoLorem.setTextSize(24.0f*0.5f);
        myView.presoLorem.setGravity(mainActivityInterface.getPreferences().getMyPreferenceInt(getContext(),"presoInfoAlign", Gravity.CENTER));
        myView.presoInfoLorem.setGravity(mainActivityInterface.getPreferences().getMyPreferenceInt(getContext(),"presoInfoAlign", Gravity.END));

        // Set the sticky preview
        myView.stickyLorem.setBackgroundColor(mainActivityInterface.getMyThemeColors().getStickyBackgroundColor());
        myView.stickyLorem.setTextSize(22.0f);
        myView.stickyLorem.setTextColor(mainActivityInterface.getMyThemeColors().getStickyTextColor());

        // Clicking on the previews will update them
        myView.songPreview.setOnClickListener(v -> updatePreviews());
        myView.presoPreview.setOnClickListener(v -> updatePreviews());
        myView.stickyLorem.setOnClickListener(v -> updatePreviews());
    }

    private void updatePreviews() {
        Runnable runnable = () -> requireActivity().runOnUiThread(() -> {
            // Set up the song preview
            myView.lyricPreview.setTypeface(mainActivityInterface.getMyFonts().getLyricFont());
            myView.chordPreview.setTypeface(mainActivityInterface.getMyFonts().getChordFont());

            // Set the presentation preview
            myView.presoLorem.setTypeface(mainActivityInterface.getMyFonts().getPresoFont());
            myView.presoInfoLorem.setTypeface(mainActivityInterface.getMyFonts().getPresoInfoFont());

            // Set the sticky preview
            myView.stickyLorem.setTypeface(mainActivityInterface.getMyFonts().getStickyFont());
        });
        // Run this now and again in about 500ms and 3 seconds (to check loading of the font has happened)
        new Thread(runnable).start();
        new Handler().postDelayed(runnable,500);
        new Handler().postDelayed(runnable,3000);
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
}
