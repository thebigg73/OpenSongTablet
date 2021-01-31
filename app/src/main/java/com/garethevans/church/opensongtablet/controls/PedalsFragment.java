package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.appdata.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsPedalBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.midi.Midi;
import com.garethevans.church.opensongtablet.preferences.Preferences;

import java.util.ArrayList;

public class PedalsFragment extends Fragment {

    private SettingsPedalBinding myView;
    private Preferences preferences;
    private PedalActions pedalActions;
    private Midi midi;
    private MainActivityInterface mainActivityInterface;

    private ArrayList<String> actionCodes;
    private ArrayList<String> actions;
    private ExposedDropDownArrayAdapter arrayAdapter;

    private boolean longPressCapable = false;
    private long downTime, upTime;
    private String currentMidiCode;
    private int currentListening, currentPedalCode;
    private int[] defKeyCodes;
    private String[] defMidiCodes;
    private String[] shortActions;
    private String[] longActions;
    private TextView[] buttonCodes, buttonMidis;
    private RelativeLayout[] buttonHeaders;
    private AutoCompleteTextView[] shortTexts, longTexts;

    private Handler pageButtonWaiting;
    private Runnable stopListening;

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

        mainActivityInterface.updateToolbar(null,getString(R.string.settings) + " / " + getString(R.string.controls) + " / " + getString(R.string.pedal));

        // Setup the helper classes
        setupHelpers();

        // Grab views
        grabViews();

        // Initialise the array items
        actionCodes = pedalActions.getActionCodes();
        actions = pedalActions.getActions();
        defKeyCodes = pedalActions.defPedalCodes;
        defMidiCodes = pedalActions.defPedalMidis;
        shortActions = pedalActions.defShortActions;
        longActions = pedalActions.defLongActions;

        // Decide on midi allowed pedals
        midiPedalAllowed();

        // Set up the drop down menus
        setupDropDowns();

        // Set up the button actions
        setupButtons();

        // Set up the toggle switches
        setupSwitches();

        return myView.getRoot();
    }

    private void setupHelpers() {
        preferences = mainActivityInterface.getPreferences();
        pedalActions = mainActivityInterface.getPedalActions();
        midi = mainActivityInterface.getMidi(mainActivityInterface);
    }

    private void midiPedalAllowed() {
        if (midi!=null && midi.getMidiDevice()!=null && preferences.getMyPreferenceBoolean(getContext(),"midiAsPedal",false)) {
            String message = getString(R.string.midi_pedal) + ": " + midi.getMidiDeviceName();
            myView.midiPedal.setText(message);
        } else {
            myView.midiPedal.setText(getString(R.string.pedal_midi_warning));
        }
    }
    private void grabViews() {
        buttonCodes = new TextView[] {null,myView.button1Code,myView.button2Code,myView.button3Code,myView.button4Code,
                myView.button5Code,myView.button6Code,myView.button7Code,myView.button8Code};

        buttonMidis = new TextView[] {null,myView.button1Midi,myView.button2Midi,myView.button3Midi,myView.button4Midi,
                myView.button5Midi,myView.button6Midi,myView.button7Midi,myView.button8Midi};

        buttonHeaders = new RelativeLayout[] {null, myView.button1Header, myView.button2Header,
                myView.button3Header, myView.button4Header, myView.button5Header,
                myView.button6Header, myView.button7Header, myView.button8Header};

        shortTexts = new AutoCompleteTextView[]{null, myView.shortButton1Text, myView.shortButton2Text,
                myView.shortButton3Text, myView.shortButton4Text, myView.shortButton5Text,
                myView.shortButton6Text, myView.shortButton7Text, myView.shortButton8Text};

        longTexts = new AutoCompleteTextView[]{null, myView.longButton1Text, myView.longButton2Text,
                myView.longButton3Text, myView.longButton4Text, myView.longButton5Text,
                myView.longButton6Text, myView.longButton7Text, myView.longButton8Text};
    }

    private String charFromInt(int i) {
        if (i==-1 || KeyEvent.keyCodeToString(i)==null) {
            return getString(R.string.not_set);
        } else {
            return KeyEvent.keyCodeToString(i);
        }
    }

    private void setupDropDowns() {
        arrayAdapter = new ExposedDropDownArrayAdapter(requireContext(),R.layout.exposed_dropdown,actions);
        for (int s=1; s<=8; s++) {
            doDropDowns(s,true);
        }
        for (int l=1; l<=8; l++) {
            doDropDowns(l,false);
        }
    }
    private void doDropDowns(int which,boolean isShort) {
        AutoCompleteTextView tv;
        String pref;
        String defpref;
        if (isShort) {
            tv = shortTexts[which];
            pref = "pedal"+which+"ShortPressAction";
            defpref = shortActions[which];
        } else {
            tv = longTexts[which];
            pref = "pedal" + which + "LongPressAction";
            defpref = longActions[which];
        }
        tv.setAdapter(arrayAdapter);
        tv.setText(getActionFromActionCode(preferences.getMyPreferenceString(getContext(),
                    pref, defpref)));
        tv.addTextChangedListener(new MyTextWatcher(pref));
    }

    private String getActionCodeFromAction(String s) {
        return actionCodes.get(actions.indexOf(s));
    }
    private String getActionFromActionCode(String s) {
        Log.d("d","s="+s);
        int val = actionCodes.indexOf(s);
        Log.d("d","val="+val);
        if (val>-1 && actions.size()>=val) {
            return actions.get(actionCodes.indexOf(s));
        } else {
            return "";
        }
    }

    private void setupButtons() {
        for (int w=1; w<=8; w++) {
            doButtons(w);
        }
    }
    private void doButtons(int which) {
        buttonCodes[which].setText(charFromInt(preferences.getMyPreferenceInt(getContext(), "pedal"+which+"Code",defKeyCodes[which])));
        buttonMidis[which].setText(preferences.getMyPreferenceString(getContext(), "pedal"+which+"Midi",defMidiCodes[which]));
        buttonHeaders[which].setOnClickListener(v -> prepareButtonListener(which));
    }

    private void prepareButtonListener(int which) {
        currentListening = which;
        String codePref = "pedal"+which+"Code";
        String midiPref = "midi"+which+"Code";
        currentPedalCode = preferences.getMyPreferenceInt(getContext(),codePref,defKeyCodes[which]);
        currentMidiCode = preferences.getMyPreferenceString(getContext(),midiPref,defMidiCodes[which]);
        buttonCodes[which].setText(getString(R.string.pedal_waiting));
        buttonMidis[which].setText(getString(R.string.pedal_waiting));
        pageButtonWaiting = new Handler();
        stopListening = () -> {
            buttonCodes[which].setText(charFromInt(currentPedalCode));
            buttonMidis[which].setText(currentMidiCode);
        };
        pageButtonWaiting.postDelayed(stopListening,8000);
    }

    private void setupSwitches() {
        myView.pedalToggleScrollBeforeSwipeButton.setChecked(preferences.getMyPreferenceBoolean(getContext(),"pedalScrollBeforeMove",true));
        myView.pedalToggleWarnBeforeSwipeButton.setChecked(preferences.getMyPreferenceBoolean(getContext(),"pedalShowWarningBeforeMove",false));
        myView.pedalToggleScrollBeforeSwipeButton.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.getMyPreferenceBoolean(getContext(),"pedalScrollBeforeMove",isChecked));
        myView.pedalToggleWarnBeforeSwipeButton.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.getMyPreferenceBoolean(getContext(),"pedalShowWarningBeforeMove",isChecked));
    }

    // Key listeners called from MainActivity
    // Key down to register button in this fragment.
    // Key up and long press to detect if this is possible for test
    public void keyDownListener(int keyCode) {
        if (currentListening > 0) {
            // Get a text version of the keyCode
            String pedalText = charFromInt(keyCode);

            // Run the common actions for midi and key registrations
            commonEventDown(currentListening,keyCode,pedalText, null);

            // Check and remove any other pedals using this code
            removePreviouslySetKey(keyCode);
        }
    }
    public void midiDownListener(String note) {
        if (currentListening>0) {
            commonEventDown(currentListening,0,null, note);

            // Check and remove any other pedals using this code
            removePreviouslySetMidi(note);
        }
    }
    private void commonEventDown(int which, int pedalCode, String pedalText, String pedalMidi) {
        // Reset the keyDown and keyUpTimes
        downTime = System.currentTimeMillis();
        upTime = downTime;

        // Reset the listening pedal
        currentListening = 0;
        pageButtonWaiting.removeCallbacks(stopListening);

        // Update the on screen value
        updateButtonText(pedalText, pedalMidi);

        // Set the preference
        setPedalPreference(which, pedalCode, pedalMidi);
    }

    public void commonEventUp() {
        // Set the keyUpTime
        upTime = System.currentTimeMillis();

        // Decide if longPressCapable
        longPressCapable = ((upTime - downTime)>300 && (upTime - downTime)<1000) || longPressCapable;
    }
    public void commonEventLong() {
        longPressCapable = true;
    }


    public boolean isListening() {
        return currentListening>-1;
    }

    private void updateButtonText(String keyText, String midiText) {
        if (midiText==null) {
            midiText = currentMidiCode;
        }
        if (keyText==null) {
            keyText = charFromInt(currentPedalCode);
        }
        buttonCodes[currentListening].setText(keyText);
        buttonMidis[currentListening].setText(midiText);
    }

    private void removePreviouslySetKey(int keyCode) {
        // Check for any other pedals currently set to this value and remove them.
        for (int x=1; x<=8; x++) {
            if (currentListening!=x && preferences.getMyPreferenceInt(getContext(),"pedal"+x+"Code",defKeyCodes[x])==keyCode) {
                setPedalPreference(x,defKeyCodes[x],null);
                buttonCodes[x].setText(R.string.not_set);
            }
        }
    }
    private void removePreviouslySetMidi(String midiCode) {
        // Check for any other pedals currently set to this value and remove them.
        for (int x=1; x<=8; x++) {
            if (currentListening!=x && preferences.getMyPreferenceString(getContext(),"pedal"+x+"Midi",defMidiCodes[x]).equals(midiCode)) {
                preferences.setMyPreferenceString(getContext(),"pedal"+x+"Midi","");
                buttonMidis[x].setText(R.string.not_set);
            }
        }
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

    private void setPedalPreference(int which, int pedalCode, String pedalMidi) {
        if (pedalMidi==null) {
            // Normal key press
            preferences.setMyPreferenceInt(getContext(),"pedal"+which+"Code",pedalCode);
        } else {
            // Midi press
            preferences.setMyPreferenceString(getContext(), "pedal"+which+"Midi", pedalMidi);
        }
    }
}