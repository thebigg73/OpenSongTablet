package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.DisplayColorSettingsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;

public class ChooseColorFragment extends Fragment {

    private final String TAG = "ChooseColorFrament";
    private MainActivityInterface mainActivityInterface;
    private DisplayColorSettingsBinding myView;
    private Preferences preferences;
    private ThemeColors themeColors;

    private String newColorHex;
    private String alphaHex;
    private String redHex;
    private String greenHex;
    private String blueHex;
    private String themePrefix;
    private int newColorInt;
    private boolean sliding = false, typing = false;

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = DisplayColorSettingsBinding.inflate(inflater,container,false);

        mainActivityInterface.updateToolbar(null,getName());
        // Set up the helper classes
        setUpHelpers();

        // Set up colour
        setupOriginalColor(mainActivityInterface.getWhattodo());

        // Set the sliders to the correct positions
        setSliderValues();

        // Set up the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setUpHelpers() {
        preferences = mainActivityInterface.getPreferences();
        themeColors = mainActivityInterface.getMyThemeColors();
    }

    private void setSliderValues() {
        myView.alphaSlider.setProgress(getIntFromHex(alphaHex));
        myView.redSlider.setProgress(getIntFromHex(redHex));
        myView.greenSlider.setProgress(getIntFromHex(greenHex));
        myView.blueSlider.setProgress(getIntFromHex(blueHex));
    }

    private void setListeners() {
        myView.alphaSlider.setOnSeekBarChangeListener(new MySeekBarListener());
        myView.redSlider.setOnSeekBarChangeListener(new MySeekBarListener());
        myView.greenSlider.setOnSeekBarChangeListener(new MySeekBarListener());
        myView.blueSlider.setOnSeekBarChangeListener(new MySeekBarListener());
        myView.hexColor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                typing = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Only do this is we are actually editing the text, not the sliders
                if (!sliding) {
                    int cursorPos = myView.hexColor.getSelectionStart();
                    String text = s.toString();
                    // Get rid of # for now
                    text = text.replace("#", "");
                    // Make sure there are only hex characters
                    text = text.replaceAll("[^A-Fa-f0-9]", "0");
                    if (text.length()==8) {
                        newColorInt = getColorFromHex(text);
                        // Fix it back to properHex with 8 characters
                        text = String.format("%08X", (newColorInt));
                        newColorHex = text;
                        text = "#" + text;
                        sliding = true;  // To stop permanent loops!
                        myView.hexColor.setText(text);
                        myView.hexColor.setSelection(cursorPos);
                        sliding = false;
                        setNewColors(newColorInt);
                        getHexPairs(newColorHex);
                        setSliderValues();
                    }
                }
                typing = false;
            }
        });
        myView.saveColor.setOnClickListener(v -> doSave(mainActivityInterface.getWhattodo()));
    }

    private void setupOriginalColor(String which) {
        themePrefix = preferences.getMyPreferenceString(getContext(), "appTheme", "dark");

        Log.d(TAG,"themePrefix="+themePrefix);
        Log.d(TAG,themeColors.getThemeName());
        // Load the chosen colours up
        themeColors.getDefaultColors(getContext(),preferences);
        int oldColorInt;
        try {
            oldColorInt = themeColors.getValue(which);
        } catch (Exception e) {
            e.printStackTrace();
            oldColorInt = -1;
        }

        Log.d("d", "oldColorInt="+oldColorInt);
        String oldColorHex = String.format("%08X", (oldColorInt));
        newColorHex = String.format("%08X",(oldColorInt));
        newColorInt = oldColorInt;

        setNewColors(getColorFromHex(newColorHex));

        myView.oldColor.setBackgroundColor(oldColorInt);

        getHexPairs(oldColorHex);

        String text = "#"+newColorHex;
        myView.hexColor.setText(text);
        myView.newColor.setBackgroundColor(newColorInt);
    }

    private void getHexPairs(String hex) {
        // Remove #
        hex = hex.replace("#","");
        // Make sure it is 8 hex characters only
        hex = fixHexCode(hex);

        alphaHex = hex.substring(0,2);
        redHex   = hex.substring(2,4);
        greenHex = hex.substring(4,6);
        blueHex  = hex.substring(6,8);
    }

    private void setNewColors(int colorInt) {
        myView.newColor.setBackgroundColor(colorInt);
    }

    private void updateColors() {
        newColorHex = alphaHex + redHex + greenHex + blueHex;
        String text = "#" + newColorHex;
        newColorInt = getColorFromHex(text);
        if (!typing) {
            // Don't update the text view if we are typing there!
            myView.hexColor.setText(text);
        }
        myView.newColor.setBackgroundColor(newColorInt);
    }

    private String getHexFromSlider(int intValue) {
        // Returns a 2 character hex code for int=0-255;
        return String.format("%02X", (intValue));
    }

    private int getIntFromHex(String hexValue) {
        // Gets an integer value from a hex pair
        return Integer.parseInt(hexValue,16);
    }

    private int getColorFromHex(String hex) {
        // Remove the #
        hex = hex.replace("#","");

        // Make sure the hex code is 8 char hex code
        hex = fixHexCode(hex);

        // Put the # back
        hex = "#" + hex;

        try {
            return Color.parseColor(hex);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private String fixHexCode(String hex) {
        hex = hex.replaceAll("[^A-Fa-f0-9]", "F");
        // Make sure it is 8 characters of hex code
        hex = String.format("%" + 8 + "s", hex);
        // Replace any missing characters with F
        hex = hex.replace(" ","F");
        return hex;
    }

    private class MySeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (seekBar==myView.alphaSlider) {
                alphaHex = getHexFromSlider(progress);
            } else if (seekBar==myView.redSlider) {
                redHex = getHexFromSlider(progress);
            } else if (seekBar==myView.greenSlider) {
                greenHex = getHexFromSlider(progress);
            } else if (seekBar==myView.blueSlider) {
                blueHex = getHexFromSlider(progress);
            }
            updateColors();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            sliding = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            sliding = false;
        }
    }

    private void doSave(String which) {
        // Set the preference
        preferences.setMyPreferenceInt(getContext(),themePrefix+"_"+which,newColorInt);
        // Navigate back
        mainActivityInterface.popTheBackStack(R.id.themeSetupFragment,true);
        mainActivityInterface.navigateToFragment(null,R.id.themeSetupFragment);
    }

    private String getName() {
        String title="";
        // Get the name of the property from the whatToDo
        Log.d("ChooseColor","what="+mainActivityInterface.getWhattodo());
        switch (mainActivityInterface.getWhattodo()) {
            case "lyricsTextColor":
                title = getString(R.string.lyrics_color);
                break;
            case "lyricsChordsColor":
                title = getString(R.string.chord_color);
                break;
            case "lyricsCapoColor":
                title = getString(R.string.capo_color);
                break;
            case "lyricsBackgroundColor":
            case "stickyBackgroundColor":
            case "extraInfoBgColor":
                title = getString(R.string.background);
                break;
            case "lyricsVerseColor":
                title = getString(R.string.verse_background);
                break;
            case "lyricsChorusColor":
                title = getString(R.string.chorus_background);
                break;
            case "lyricsPreChorusColor":
                title = getString(R.string.prechorus_background);
                break;
            case "lyricsBridgeColor":
                title = getString(R.string.bridge_background);
                break;
            case "lyricsTagColor":
                title = getString(R.string.tag_background);
                break;
            case "lyricsCustomColor":
                title = getString(R.string.custom_background);
                break;
            case "lyricsCommentColor":
                title = getString(R.string.comment_background);
                break;
            case "presoInfoFontColor":
                title = getString(R.string.info_text);
                break;
            case "presoFontColor":
            case "extraInfoTextColor":
            case "stickyTextColor":
                title = getString(R.string.text);
                break;
            case "metronomeColor":
                title = getString(R.string.metronome);
                break;
            case "pageButtonsColor":
                title = getString(R.string.page_buttons);
                break;
            case "presoAlertColor":
                title = getString(R.string.alert);
                break;
            case "presoShadowColor":
                title = getString(R.string.block_text_shadow);
                break;
            case "highlightChordColor":
                title = getString(R.string.chords);
                break;
            case "highlightHeadingColor":
                title = getString(R.string.title);
                break;
        }
        return title;
    }
}
