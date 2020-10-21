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

import com.garethevans.church.opensongtablet.databinding.DisplayColorSettingsBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

public class ChooseColorFragment extends Fragment {

    MainActivityInterface mainActivityInterface;
    DisplayColorSettingsBinding myView;
    Preferences preferences;
    ThemeColors themeColors;

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

        // Set up the helper classes
        setUpHelpers();

        // Set up colour
        setupOriginalColor();

        // Set the sliders to the correct positions
        setSliderValues();

        // Set up the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setUpHelpers() {
        preferences = new Preferences();
        themeColors = new ThemeColors();
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
        myView.saveColor.setOnClickListener(v -> doSave());
    }

    private void setupOriginalColor() {
        themePrefix = preferences.getMyPreferenceString(getContext(), "appTheme", "dark");

        // Load the chosen colours up
        themeColors.getDefaultColors(getContext(),preferences);
        int oldColorInt;
        try {
            oldColorInt = themeColors.getValue(StaticVariables.whattodo);
        } catch (Exception e) {
            oldColorInt = -1;
        }

        String oldColorHex = String.format("%08X", (oldColorInt));
        newColorHex = String.format("%08X",(oldColorInt));
        newColorInt = oldColorInt;

        Log.d("d","newColorHex="+newColorHex);
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
            Log.d("d","hex="+hex);
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

    private void doSave() {
        // Set the preference
        preferences.setMyPreferenceInt(getContext(),themePrefix+"_"+StaticVariables.whattodo,newColorInt);
        // Navigate back
        requireActivity().getSupportFragmentManager().popBackStackImmediate();
    }
}
