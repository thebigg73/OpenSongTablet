package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsPedalBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class PedalsFragment extends Fragment {

    SettingsPedalBinding myView;
    Preferences preferences;
    MainActivityInterface mainActivityInterface;

    private ArrayList<String> actionCodes;
    private ArrayList<String> actions;
    private ExposedDropDownArrayAdapter arrayAdapter;

    private boolean longPressCapable = false;
    private long downTime, upTime;
    private String pedalListening;
    private int keyDownCode;

    Handler pageButtonWaiting;
    Runnable stopListening;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.registerFragment(this,"pedalsFragment");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsPedalBinding.inflate(inflater,container,false);

        // Setup the helper classes
        setupHelpers();

        // Initialise the array items
        setupActions();

        // Set up the drop down menus
        setupDropDowns();

        // Set up the button actions
        setupButtons();

        // Set up the toggle switches
        setupSwitches();

        return myView.getRoot();
    }

    private void setupHelpers() {
        preferences = new Preferences();
    }

    private void setupActions() {
        actions = new ArrayList<>();
        actionCodes = new ArrayList<>();
        addString("","");
        addString("prev",getString(R.string.pageturn_previous));
        addString("next",getString(R.string.pageturn_next));
        addString("up",getString(R.string.pageturn_up));
        addString("down",getString(R.string.pageturn_down));
        addString("pad",getString(R.string.padPedalText));
        addString("autoscroll",getString(R.string.autoscrollPedalText));
        addString("metronome",getString(R.string.metronomePedalText));
        addString("pad_autoscroll",getString(R.string.padPedalText) + " & " + getString(R.string.autoscrollPedalText));
        addString("pad_metronome",getString(R.string.padPedalText) + " & " + getString(R.string.metronomePedalText));
        addString("autoscroll_metronome",getString(R.string.autoscrollPedalText) + " & " + getString(R.string.metronomePedalText));
        addString("pad_autoscroll_metronome",getString(R.string.padPedalText) + " & " + getString(R.string.autoscrollPedalText) + " & " + getString(R.string.metronomePedalText));
        addString("editsong",getString(R.string.edit));
        addString("changetheme",getString(R.string.choose_theme));
        addString("autoscale",getString(R.string.autoscale));
        addString("transpose",getString(R.string.transpose));
        addString("showchords",getString(R.string.showchords));
        addString("showcapo",getString(R.string.showcapo));
        addString("showlyrics",getString(R.string.showlyrics));
        addString("fullsearch",getString(R.string.action_search));
        addString("randomsong",getString(R.string.random_song));
        addString("abcnotation",getString(R.string.music_score));
        addString("highlight",getString(R.string.highlight));
        addString("sticky",getString(R.string.stickynotes));
        addString("speedup",getString(R.string.inc_autoscroll_speed));
        addString("slowdown",getString(R.string.dec_autoscroll_speed));
        addString("pause",getString(R.string.toggle_autoscroll_pause));
        addString("songmenu",getString(R.string.gesture1));
        addString("optionmenu",getString(R.string.action_settings));
        addString("editset",getString(R.string.currentset));
        addString("refreshsong",getString(R.string.gesture4));
        addString("addsongtoset",getString(R.string.add_song_to_set));
    }

    private void addString(String id, String val) {
        actionCodes.add(id);
        actions.add(val);
    }

    private String charFromInt(int i) {
        if (i==-1 || KeyEvent.keyCodeToString(i)==null) {
            return getString(R.string.notset);
        } else {
            return KeyEvent.keyCodeToString(i);
        }
    }


    private void setupDropDowns() {
        arrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),R.layout.exposed_dropdown,actions);
        doDropDowns(myView.shortButton1Text,"pedal1ShortPressAction","prev");
        doDropDowns(myView.shortButton2Text,"pedal2ShortPressAction","next");
        doDropDowns(myView.shortButton3Text,"pedal3ShortPressAction","up");
        doDropDowns(myView.shortButton4Text,"pedal4ShortPressAction","down");
        doDropDowns(myView.shortButton5Text,"pedal5ShortPressAction","prev");
        doDropDowns(myView.shortButton6Text,"pedal6ShortPressAction","next");
        doDropDowns(myView.shortButton7Text,"pedal7ShortPressAction","prev");
        doDropDowns(myView.shortButton8Text,"pedal8ShortPressAction","next");

        doDropDowns(myView.longButton1Text,"pedal1LongPressAction","songmenu");
        doDropDowns(myView.longButton1Text,"pedal2LongPressAction","set");
        doDropDowns(myView.longButton1Text,"pedal3LongPressAction","songmenu");
        doDropDowns(myView.longButton1Text,"pedal4LongPressAction","set");
        doDropDowns(myView.longButton1Text,"pedal5LongPressAction","songmenu");
        doDropDowns(myView.longButton1Text,"pedal6LongPressAction","set");
        doDropDowns(myView.longButton1Text,"pedal7LongPressAction","songmenu");
        doDropDowns(myView.longButton1Text,"pedal8LongPressAction","set");
    }
    private void doDropDowns(AutoCompleteTextView autoCompleteTextView, String pref, String prefdef) {
        autoCompleteTextView.setAdapter(arrayAdapter);
        autoCompleteTextView.setText(getActionFromActionCode(preferences.getMyPreferenceString(getContext(), pref, prefdef)));
        autoCompleteTextView.addTextChangedListener(new MyTextWatcher(pref));
    }

    private String getActionCodeFromAction(String s) {
        return actionCodes.get(actions.indexOf(s));
    }
    private String getActionFromActionCode(String s) {
        return actions.get(actionCodes.indexOf(s));
    }

    private void setupButtons() {
        doButtons(myView.button1,myView.button1Code,"pedal1Code",21);
        doButtons(myView.button2,myView.button2Code,"pedal2Code",22);
        doButtons(myView.button3,myView.button3Code,"pedal3Code",19);
        doButtons(myView.button4,myView.button4Code,"pedal4Code",20);
        doButtons(myView.button5,myView.button5Code,"pedal5Code",92);
        doButtons(myView.button6,myView.button6Code,"pedal6Code",93);
        doButtons(myView.button7,myView.button7Code,"pedal7Code",-1);
        doButtons(myView.button8,myView.button8Code,"pedal8Code",-1);
    }
    private void doButtons(ExtendedFloatingActionButton extendedFloatingActionButton, TextView textView, String pref, int defpref) {
       textView.setText(charFromInt(preferences.getMyPreferenceInt(getContext(), pref, defpref)));
       extendedFloatingActionButton.setOnClickListener(v -> prepareButtonListener(textView,pref,defpref));
    }
    private void prepareButtonListener(TextView textView, String pref, int defpref) {
        pedalListening = pref;
        textView.setText(getString(R.string.pageturn_waiting));
        pageButtonWaiting = new Handler();
        stopListening = () -> {
            textView.setText(charFromInt(defpref));
            if (keyDownCode>-1) {
                checkForKeyUp(pedalListening,keyDownCode);
            }
        };
        pageButtonWaiting.postDelayed(stopListening,8000);
    }

    private void setupSwitches() {
        myView.pedalToggleScrollBeforeSwipeButton.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.getMyPreferenceBoolean(getContext(),"pedalScrollBeforeMove",isChecked));
        myView.pedalToggleScrollBeforeSwipeButton.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.getMyPreferenceBoolean(getContext(),"pedalShowWarningBeforeMove",isChecked));
    }

    // Key listeners called from MainActivity
    public void keyDownListener(int keyCode) {
        // Keep a note of the keyDown
        keyDownCode = keyCode;
        // Reset the keyDown and keyUpTimes
        downTime = System.currentTimeMillis();
        upTime = downTime;
        pageButtonWaiting.removeCallbacks(stopListening);
    }
    public void keyUpListener(int keyCode) {
        // Set the keyUpTime
        upTime = System.currentTimeMillis();
        // Decide if longPressCapable
        longPressCapable = isLongPressCapable();
        // Set the preference
        preferences.setMyPreferenceInt(getContext(),pedalListening,keyCode);
        String keyChar = charFromInt(keyCode);
        switch(pedalListening) {
            case "pedal1Code":
                myView.button1Code.setText(keyChar);
                break;
            case "pedal2Code":
                myView.button2Code.setText(keyChar);
                break;
            case "pedal3Code":
                myView.button3Code.setText(keyChar);
                break;
            case "pedal4Code":
                myView.button4Code.setText(keyChar);
                break;
            case "pedal5Code":
                myView.button5Code.setText(keyChar);
                break;
            case "pedal6Code":
                myView.button6Code.setText(keyChar);
                break;
            case "pedal7Code":
                myView.button7Code.setText(keyChar);
                break;
            case "pedal8Code":
                myView.button8Code.setText(keyChar);
                break;
        }
        pedalListening = null;
        pageButtonWaiting.removeCallbacks(stopListening);
    }
    public void keyLongPressListener(int keyCode) {
        // Register that long press is available
        longPressCapable = true;
        // Register as a short press to deal with it
        keyUpListener(keyCode);
    }
    private boolean isLongPressCapable () {
        // Get the time between keyDown and keyUp.  Standard long press is 500ms.  Look for a time around this
        return ((upTime - downTime)>300 && (upTime - downTime)<1000) || longPressCapable;
    }
    public boolean isListening() {
        return pedalListening!=null;
    }
    private void checkForKeyUp(String pref, int i) {
        // Timed out waiting for onKeyUp or onLongPress, but keyDown was registered
        // Send it as a keyUp
        keyUpListener(i);
    }

    private class MyTextWatcher implements TextWatcher {

        String which;
        String val;

        MyTextWatcher(String which) {
            this.which = which;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            val = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
            preferences.setMyPreferenceString(getContext(),which,getActionCodeFromAction(val));
        }
    }

}
