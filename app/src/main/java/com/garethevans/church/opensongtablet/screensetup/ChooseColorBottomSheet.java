package com.garethevans.church.opensongtablet.screensetup;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.databinding.BottomSheetChooseColorBinding;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.Slider;

public class ChooseColorBottomSheet extends BottomSheetDialogFragment {

    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private BottomSheetChooseColorBinding myView;

    private String newColorHex;
    private String alphaHex;
    private String redHex;
    private String greenHex;
    private String blueHex;
    private String themePrefix;
    private final String whichColor;
    private int newColorInt;
    private boolean sliding = false, typing = false;
    private final Fragment callingFragment;
    private final String fragName;

    public ChooseColorBottomSheet(Fragment callingFragment, String fragName, String whichColor) {
        this.callingFragment = callingFragment;
        this.fragName = fragName;
        this.whichColor = whichColor;
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        displayInterface = (DisplayInterface) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog1).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        return dialog;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = BottomSheetChooseColorBinding.inflate(inflater,container,false);

        myView.dialogHeading.setText(getName());
        myView.dialogHeading.setClose(this);

        // Set up colour
        setupOriginalColor();

        // Set the sliders to the correct positions
        setSliderValues();

        // Set up the listeners
        setListeners();

        return myView.getRoot();
    }

    private void setSliderValues() {
        myView.alphaSlider.setValue(getIntFromHex(alphaHex));
        myView.alphaSlider.setHint(alphaHex);
        myView.redSlider.setValue(getIntFromHex(redHex));
        myView.redSlider.setHint(redHex);
        myView.greenSlider.setValue(getIntFromHex(greenHex));
        myView.greenSlider.setHint(greenHex);
        myView.blueSlider.setValue(getIntFromHex(blueHex));
        myView.blueSlider.setHint(blueHex);
    }

    private void setListeners() {
        myView.alphaSlider.addOnSliderTouchListener(new MySliderTouchListener());
        myView.alphaSlider.addOnChangeListener(new MySliderChangeListener());
        myView.redSlider.addOnSliderTouchListener(new MySliderTouchListener());
        myView.redSlider.addOnChangeListener(new MySliderChangeListener());
        myView.greenSlider.addOnSliderTouchListener(new MySliderTouchListener());
        myView.greenSlider.addOnChangeListener(new MySliderChangeListener());
        myView.blueSlider.addOnSliderTouchListener(new MySliderTouchListener());
        myView.blueSlider.addOnChangeListener(new MySliderChangeListener());

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
                    if (text.length()>8) {
                        // Trim off the end
                        text = text.substring(0,8);
                        text = "#" + text;
                        myView.hexColor.setText(text);
                        myView.hexColor.setSelection(1);
                    }

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
        themePrefix = mainActivityInterface.getPreferences().getMyPreferenceString(getContext(), "appTheme", "dark");

        // Load the chosen colours up
        mainActivityInterface.getMyThemeColors().getDefaultColors(getContext(),mainActivityInterface);
        int oldColorInt;
        try {
            if (whichColor.equals("backgroundColor")) {
                oldColorInt = mainActivityInterface.getPresenterSettings().getBackgroundColor();
            } else {
                oldColorInt = mainActivityInterface.getMyThemeColors().getValue(whichColor);
            }
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

    private class MySliderTouchListener implements Slider.OnSliderTouchListener {

        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
            sliding = true;
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            sliding = false;
        }
    }

    private class MySliderChangeListener implements Slider.OnChangeListener {

        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            if (slider==myView.alphaSlider.getSlider()) {
                alphaHex = getHexFromSlider(Math.round(value));
                myView.alphaSlider.setHint(alphaHex);
            } else if (slider==myView.redSlider.getSlider()) {
                redHex = getHexFromSlider(Math.round(value));
                myView.redSlider.setHint(redHex);
            } else if (slider==myView.greenSlider.getSlider()) {
                greenHex = getHexFromSlider(Math.round(value));
                myView.greenSlider.setHint(greenHex);
            } else if (slider==myView.blueSlider.getSlider()) {
                blueHex = getHexFromSlider(Math.round(value));
                myView.blueSlider.setHint(blueHex);
            }
            updateColors();
        }
    }
    private void doSave() {
        // Set the preference
        if (whichColor.equals("backgroundColor")) {
            // Presenter settings
            mainActivityInterface.getPreferences().setMyPreferenceInt(getContext(),whichColor,newColorInt);
            mainActivityInterface.getPresenterSettings().setBackgroundColor(newColorInt);
        } else {
            mainActivityInterface.getPreferences().setMyPreferenceInt(getContext(), themePrefix + "_" + whichColor, newColorInt);
        }

        // If we changed the page button color...
        if (whichColor.equals("pageButtonsColor")) {
            mainActivityInterface.getMyThemeColors().setPageButtonsColor(newColorInt);
            mainActivityInterface.getMyThemeColors().splitPageButtonsColorAndAlpha(mainActivityInterface);
        } else if (whichColor.equals("extraInfoTextColor")) {
            mainActivityInterface.getMyThemeColors().setExtraInfoTextColor(newColorInt);
            mainActivityInterface.getMyThemeColors().splitPageButtonsColorAndAlpha(mainActivityInterface);
        }

        // Update the theme color on the fragment behind
        mainActivityInterface.updateFragment(fragName,callingFragment,null);

        // These changes should call an update to any secondary displays as well
        if (whichColor.equals("presoShadowColor")) {
            mainActivityInterface.getMyThemeColors().setPresoShadowColor(newColorInt);
            displayInterface.updateDisplay("setInfoStyles");
        }

        // Navigate back
        dismiss();
        //mainActivityInterface.popTheBackStack(R.id.themeSetupFragment,true);
        //mainActivityInterface.navigateToFragment(null,R.id.themeSetupFragment);
    }

    private String getName() {
        String title="";
        switch (whichColor) {
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
            case "backgroundColor":
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myView = null;
    }
}

