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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDown;
import com.garethevans.church.opensongtablet.customviews.ExposedDropDownArrayAdapter;
import com.garethevans.church.opensongtablet.databinding.SettingsPedalBinding;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class PedalsFragment extends Fragment {

    @SuppressWarnings("unused")
    private final String TAG = "PedalsFragment";
    private SettingsPedalBinding myView;
    private MainActivityInterface mainActivityInterface;

    private ArrayList<String> actionCodes;
    private ArrayList<String> actions;
    private ExposedDropDownArrayAdapter arrayAdapter;

    private boolean longPressCapable = false;
    private long downTime, upTime;
    private String currentMidiCode, pedal_string="", website_foot_pedal_string="", midi_pedal_string="",
            pedal_midi_warning_string="", is_not_set_string="", pedal_waiting_string="",
            pedal_up_string="", pedal_down_string="", short_press_string="", long_press_string="",
            pedal_waiting_ended_string="", pedal_detected_string="", unknown_string="",
            default_string="", airturn_string="", repeat_string="", up_string="", down_string="";

    private StringBuilder keyEvents = new StringBuilder();
    private int currentListening, currentPedalCode, keyUpCount=0;
    private int[] defKeyCodes;
    private TextView[] buttonCodes, buttonMidis;
    private RelativeLayout[] buttonHeaders;
    private ExposedDropDown[] shortTexts, longTexts;

    private Handler pageButtonWaiting;
    private Runnable stopListening;
    private String webAddress;

    @Override
    public void onResume() {
        super.onResume();
        mainActivityInterface.updateToolbar(pedal_string);
        mainActivityInterface.updateToolbarHelp(webAddress);
        mainActivityInterface.getPedalActions().setTesting(this,true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivityInterface = (MainActivityInterface) context;
        mainActivityInterface.registerFragment(this, "pedalsFragment");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        myView = SettingsPedalBinding.inflate(inflater, container, false);

        prepareStrings();

        webAddress = website_foot_pedal_string;

        // Register this fragment
        mainActivityInterface.registerFragment(this,"PedalsFragment");

        // Grab views
        grabViews();

        // Initialise the array items
        mainActivityInterface.getPedalActions().setUpPedalActions();
        actionCodes = mainActivityInterface.getPedalActions().getActionCodes();
        actions = mainActivityInterface.getPedalActions().getActions();
        defKeyCodes = mainActivityInterface.getPedalActions().defPedalCodes;

        // Decide on midi allowed pedals
        midiPedalAllowed();

        // Set up the drop down menus
        setupDropDowns();

        // Set up the button actions
        setupButtons();

        // Set up the sliders
        setupSliders();

        // Set up the toggle switches
        setupSwitches();

        // Set AirTurnMode actions
        airTurnModeActions();

        // TODO get this working
        // Set RepeatMode actions
        // repeatModeActions();

        // Stop anything from getting focus (otherwise arrows trigger movements
        return myView.getRoot();
    }

    private void prepareStrings() {
        if (getContext()!=null) {
            pedal_string = getString(R.string.pedal);
            website_foot_pedal_string = getString(R.string.website_foot_pedal);
            midi_pedal_string = getString(R.string.midi_pedal);
            pedal_midi_warning_string = getString(R.string.pedal_midi_warning);
            is_not_set_string = getString(R.string.is_not_set);
            pedal_waiting_string = getString(R.string.pedal_waiting);
            short_press_string = getString(R.string.action_short_press);
            long_press_string = getString(R.string.long_press);
            pedal_down_string = getString(R.string.pedal_down);
            pedal_up_string = getString(R.string.pedal_up);
            pedal_waiting_ended_string = getString(R.string.pedal_waiting_ended);
            pedal_detected_string = getString(R.string.pedal_detected);
            unknown_string = getString(R.string.unknown);
            default_string = getString(R.string.use_default);
            airturn_string = getString(R.string.air_turn_long_press);
            repeat_string = getString(R.string.repeat_mode);
            down_string = getString(R.string.pedal_down);
            up_string = getString(R.string.pedal_up);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister this fragment
        mainActivityInterface.registerFragment(null,"PedalsFragment");
        mainActivityInterface.getPedalActions().setTesting(null,false);
    }

    private void midiPedalAllowed() {
        //String midiDevice = mainActivityInterface.getMidi().getMidiDeviceName();
        myView.midiAsPedal.setChecked(mainActivityInterface.getPedalActions().getMidiAsPedal());
        myView.midiAsPedal.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean("midiAsPedal",b);
            mainActivityInterface.getPedalActions().setMidiAsPedal(b);
            midiPedalAllowed();
        });
        if (mainActivityInterface.getMidi() != null && mainActivityInterface.getMidi().getMidiDevice() != null &&
                mainActivityInterface.getPedalActions().getMidiAsPedal()) {
            String message = midi_pedal_string + ": " +
                    mainActivityInterface.getMidi().getMidiDeviceName();
            myView.midiAsPedal.setHint(message);
        } else {
            myView.midiAsPedal.setHint(pedal_midi_warning_string);
        }
    }

    private void grabViews() {
        buttonCodes = new TextView[]{null, myView.button1Code, myView.button2Code, myView.button3Code, myView.button4Code,
                myView.button5Code, myView.button6Code, myView.button7Code, myView.button8Code};

        buttonMidis = new TextView[]{null, myView.button1Midi, myView.button2Midi, myView.button3Midi, myView.button4Midi,
                myView.button5Midi, myView.button6Midi, myView.button7Midi, myView.button8Midi};

        buttonHeaders = new RelativeLayout[]{null, myView.button1Header, myView.button2Header,
                myView.button3Header, myView.button4Header, myView.button5Header,
                myView.button6Header, myView.button7Header, myView.button8Header};

        shortTexts = new ExposedDropDown[]{null, myView.shortButton1Text, myView.shortButton2Text,
                myView.shortButton3Text, myView.shortButton4Text, myView.shortButton5Text,
                myView.shortButton6Text, myView.shortButton7Text, myView.shortButton8Text};

        longTexts = new ExposedDropDown[]{null, myView.longButton1Text, myView.longButton2Text,
                myView.longButton3Text, myView.longButton4Text, myView.longButton5Text,
                myView.longButton6Text, myView.longButton7Text, myView.longButton8Text};
    }

    private String charFromInt(int i) {
        if (i == -1 || KeyEvent.keyCodeToString(i) == null) {
            return is_not_set_string;
        } else {
            return KeyEvent.keyCodeToString(i);
        }
    }

    private void setupSliders() {
        myView.scrollDistance.setLabelFormatter(value -> (int)value + "%");
        myView.scrollDistance.addOnChangeListener((slider, value, fromUser) -> myView.scrollDistance.setHint((int)value + "%"));
        myView.scrollDistance.addOnSliderTouchListener(new MySliderTouchListener("scrollDistance"));
        myView.scrollDistance.setValue((int)(mainActivityInterface.getGestures().getScrollDistance()*100f));
    }

    private void setupDropDowns() {
        if (getContext()!=null) {
            arrayAdapter = new ExposedDropDownArrayAdapter(getContext(), R.layout.view_exposed_dropdown_item, actions);
        }
        for (int s = 1; s <= 8; s++) {
            doDropDowns(s, true);
        }
        for (int l = 1; l <= 8; l++) {
            doDropDowns(l, false);
        }

        // Now check for beatBuddy transitions on the short pedal
        // If found, the long press is adjusted to the exit command
        // The hints are also changed to pedal down/up
        try {
            String actionCode1 = getActionCodeFromAction(myView.shortButton1Text.getText().toString());
            String actionCode2 = getActionCodeFromAction(myView.shortButton2Text.getText().toString());
            String actionCode3 = getActionCodeFromAction(myView.shortButton3Text.getText().toString());
            String actionCode4 = getActionCodeFromAction(myView.shortButton4Text.getText().toString());
            String actionCode5 = getActionCodeFromAction(myView.shortButton5Text.getText().toString());
            String actionCode6 = getActionCodeFromAction(myView.shortButton6Text.getText().toString());
            String actionCode7 = getActionCodeFromAction(myView.shortButton7Text.getText().toString());
            String actionCode8 = getActionCodeFromAction(myView.shortButton8Text.getText().toString());

            checkBeatBuddyTransistions(myView.shortButton1Text, myView.longButton1Text, actionCode1);
            checkBeatBuddyTransistions(myView.shortButton2Text, myView.longButton2Text, actionCode2);
            checkBeatBuddyTransistions(myView.shortButton3Text, myView.longButton3Text, actionCode3);
            checkBeatBuddyTransistions(myView.shortButton4Text, myView.longButton4Text, actionCode4);
            checkBeatBuddyTransistions(myView.shortButton5Text, myView.longButton5Text, actionCode5);
            checkBeatBuddyTransistions(myView.shortButton6Text, myView.longButton6Text, actionCode6);
            checkBeatBuddyTransistions(myView.shortButton7Text, myView.longButton7Text, actionCode7);
            checkBeatBuddyTransistions(myView.shortButton8Text, myView.longButton8Text, actionCode8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDropDowns(int which, boolean isShort) {
        ExposedDropDown exposedDropDown;
        String currVal;
        if (isShort) {
            exposedDropDown = shortTexts[which];
            currVal = mainActivityInterface.getPedalActions().getPedalShortPressAction(which);
        } else {
            exposedDropDown = longTexts[which];
            currVal = mainActivityInterface.getPedalActions().getPedalLongPressAction(which);
        }
        exposedDropDown.setAdapter(arrayAdapter);
        if (getContext()!=null) {
            exposedDropDown.setArray(getContext(), actions);
        }
        exposedDropDown.setText(getActionFromActionCode(currVal));
        exposedDropDown.addTextChangedListener(new MyTextWatcher(currVal,which,isShort));
    }

    private ExposedDropDown getShortExposedDropdownByNum(int which) {
        switch (which) {
            case 1:
            default:
                return myView.shortButton1Text;
            case 2:
                return myView.shortButton2Text;
            case 3:
                return myView.shortButton3Text;
            case 4:
                return myView.shortButton4Text;
            case 5:
                return myView.shortButton5Text;
            case 6:
                return myView.shortButton6Text;
            case 7:
                return myView.shortButton7Text;
            case 8:
                return myView.shortButton8Text;
        }
    }

    private ExposedDropDown getLongExposedDropdownByNum(int which) {
        switch (which) {
            case 1:
            default:
                return myView.longButton1Text;
            case 2:
                return myView.longButton2Text;
            case 3:
                return myView.longButton3Text;
            case 4:
                return myView.longButton4Text;
            case 5:
                return myView.longButton5Text;
            case 6:
                return myView.longButton6Text;
            case 7:
                return myView.longButton7Text;
            case 8:
                return myView.longButton8Text;
        }
    }

    private String getActionCodeFromAction(String s) {
        return actionCodes.get(actions.indexOf(s));
    }

    private String getActionFromActionCode(String s) {
        int val = actionCodes.indexOf(s);
        if (val > -1 && actions.size() >= val) {
            return actions.get(actionCodes.indexOf(s));
        } else {
            return "";
        }
    }

    private void setupButtons() {
        myView.pedalTest.setChecked(false);
        myView.pedalTestLayout.setVisibility(View.GONE);
        myView.pedalTest.setOnCheckedChangeListener((compoundButton, b) -> {
            myView.pedalTestLayout.setVisibility(b ? View.VISIBLE:View.GONE);
            resetTestViews();
        });
        myView.resetButton.setOnClickListener(view -> {
            resetTestViews();
        });
        for (int w = 1; w <= 8; w++) {
            doButtons(w);
        }
    }


    private void doButtons(int which) {
        buttonCodes[which].setText(charFromInt(mainActivityInterface.getPedalActions().getPedalCode(which)));
        buttonMidis[which].setText(mainActivityInterface.getPedalActions().getMidiCode(which));
        buttonHeaders[which].setOnClickListener(v -> prepareButtonListener(which));
    }

    private void prepareButtonListener(int which) {
        currentListening = which;
        currentPedalCode = mainActivityInterface.getPedalActions().getPedalCode(which);
        currentMidiCode = mainActivityInterface.getPedalActions().getMidiCode(which);
        buttonCodes[which].setText(pedal_waiting_string);
        buttonMidis[which].setText(pedal_waiting_string);
        pageButtonWaiting = new Handler();
        stopListening = () -> {
            buttonCodes[which].setText(charFromInt(currentPedalCode));
            buttonMidis[which].setText(currentMidiCode);
            mainActivityInterface.getShowToast().doIt(pedal_waiting_ended_string);
        };
        pageButtonWaiting.postDelayed(stopListening, 8000);
    }

    private void setupSwitches() {
        myView.pedalToggleScrollBeforeSwipeButton.setChecked(mainActivityInterface.getPedalActions().getPedalScrollBeforeMove());
        myView.pedalToggleWarnBeforeSwipeButton.setChecked(mainActivityInterface.getPedalActions().getPedalShowWarningBeforeMove());
        myView.pedalToggleScrollBeforeSwipeButton.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getPedalActions().setPreferences("pedalScrollBeforeMove", isChecked));
        myView.pedalToggleWarnBeforeSwipeButton.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getPedalActions().setPreferences("pedalShowWarningBeforeMove", isChecked));
    }

    private void airTurnModeActions() {
        boolean airTurnMode = mainActivityInterface.getPedalActions().getAirTurnMode();
        myView.airTurnMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPedalActions().setPreferences("airTurnMode",isChecked);
            if (isChecked) {
                myView.repeatMode.setChecked(false);
                myView.airTurnOptions.setVisibility(View.VISIBLE);
            } else {
                myView.airTurnOptions.setVisibility(View.GONE);
            }
        });
        myView.airTurnMode.setChecked(airTurnMode);

        int airTurnLongPressTime = mainActivityInterface.getPedalActions().getAirTurnLongPressTime();
        myView.airTurnLongPressTime.setValue(airTurnLongPressTime);
        myView.airTurnLongPressTime.setHint(airTurnLongPressTime + " ms");
        myView.airTurnLongPressTime.addOnChangeListener((slider, value, fromUser) -> myView.airTurnLongPressTime.setHint((int)value + " ms"));
        myView.airTurnLongPressTime.addOnSliderTouchListener(new MySliderTouchListener("airTurnLongPressTime"));
    }

    private void repeatModeActions() {
        boolean repeatMode = mainActivityInterface.getPedalActions().getRepeatMode();
        myView.repeatMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPedalActions().setPreferences("repeatMode", isChecked);
            if (isChecked) {
                myView.airTurnMode.setChecked(false);
                myView.repeatModeOptions.setVisibility(View.VISIBLE);
            } else {
                myView.repeatModeOptions.setVisibility(View.GONE);
            }
        });
        myView.repeatMode.setChecked(repeatMode);

        int repeatModeTime = mainActivityInterface.getPedalActions().getRepeatModeTime();
        myView.repeatModeTime.setValue(repeatModeTime);
        myView.repeatModeTime.setHint(repeatModeTime + " ms");
        myView.repeatModeTime.addOnChangeListener((slider, value, fromUser) -> myView.repeatModeTime.setHint((int)value + " ms"));
        myView.repeatModeTime.addOnSliderTouchListener(new MySliderTouchListener("repeatModeTime"));
        int repeatModeCount = mainActivityInterface.getPedalActions().getRepeatModeCount();
        myView.repeatModeCount.setValue(repeatModeCount);
        myView.repeatModeCount.setHint(repeatModeCount + "");
        myView.repeatModeCount.addOnChangeListener((slider, value, fromUser) -> myView.repeatModeCount.setHint((int)value + ""));
        myView.repeatModeCount.addOnSliderTouchListener(new MySliderTouchListener("repeatModeCount"));

    }

    // Key listeners called from MainActivity
    // Key down to register button in this fragment.
    // Key up and long press to detect if this is possible for test
    public void keyDownListener(int keyCode) {
        Log.d(TAG,"keyCode:"+keyCode+"  currentListening:"+currentListening);
        downTime = System.currentTimeMillis();
        Log.d(TAG,"downTime:"+downTime);
        upTime = downTime;

        if (currentListening > 0) {
            // Get a text version of the keyCode
            String pedalText = charFromInt(keyCode);

            // Run the common actions for midi and key registrations
            commonEventDown(currentListening, keyCode, pedalText, null);

            // Check and remove any other pedals using this code
            removePreviouslySetKey(keyCode);

            // Reset the listening pedal
            currentListening = 0;
            pageButtonWaiting.removeCallbacks(stopListening);

            // Alert the user a pedal press was detected
            mainActivityInterface.getShowToast().doIt(pedal_detected_string + ": " + pedalText + " ("+keyCode+")");
        }
    }

    public void midiDownListener(String note) {
        if (currentListening > 0) {
            commonEventDown(currentListening, 0, null, note);

            // Check and remove any other pedals using this code
            removePreviouslySetMidi(note);
        }
    }

    private void commonEventDown(int which, int pedalCode, String pedalText, String pedalMidi) {
        // Reset the keyDown and keyUpTimes
        downTime = System.currentTimeMillis();
        Log.d(TAG,"downTime:"+downTime);
        upTime = downTime;

        // Update the on screen value
        updateButtonText(pedalText, pedalMidi);

        // Set the preference
        setPedalPreference(which, pedalCode, pedalMidi);
    }

    public void commonEventUp() {
        // Set the keyUpTime
        upTime = System.currentTimeMillis();
        Log.d(TAG,"upTime:"+upTime);

        // Decide if longPressCapable
        longPressCapable = ((upTime - downTime) > 300 && (upTime - downTime) < 1000) || longPressCapable;
        Log.d(TAG,"longPressCapable:"+ longPressCapable);
        Log.d(TAG,"upTime-dowTime:"+(int)(upTime-downTime));
    }

    public void commonEventLong() {
        longPressCapable = true;
    }

    public void backgroundKeyDown(int keyCode, KeyEvent keyEvent) {
        // Register a keyDown
        downTime = System.currentTimeMillis();
        Log.d(TAG,"backgroundKeyDown - keyCode:"+keyCode+"  keyEvent:"+keyEvent+"  downtime:"+downTime);
        if (myView.pedalTest.getChecked()) {
            updateKeyEvents(keyCode, downTime, down_string);
        }
    }
    public void backgroundKeyUp(int keyCode, KeyEvent keyEvent) {
        // Register a keyUp
        upTime = System.currentTimeMillis();
        Log.d(TAG,"backgroundKeyUp - keyCode:"+keyCode+"  keyEvent:"+keyEvent+"  upTime:"+upTime);
        if (myView.pedalTest.getChecked()) {
            updateKeyEvents(keyCode, upTime, up_string);
        }
        // Check the longpress mode for repeat
        mainActivityInterface.getPedalActions().commonEventUp(keyCode,"");
        // If the keyup happened
    }
    public void backgroundKeyLongPress(int keyCode, KeyEvent keyEvent) {
        // Register a keyLongPress
        upTime = System.currentTimeMillis();
        Log.d(TAG,"backgroundKeyLongPress - keyCode:"+keyCode+"  keyEvent:"+keyEvent+"  upTime:"+upTime);
        if (myView.pedalTest.getChecked()) {
            updateKeyEvents(keyCode, upTime, long_press_string);
        }
        // Because we have an actual long press, update the test view
        myView.longPressInfo.setHint(default_string);
    }

    public boolean isListening() {
        return currentListening > 0;
    }

    private void updateButtonText(String keyText, String midiText) {
        if (midiText == null) {
            midiText = currentMidiCode;
        }
        if (keyText == null) {
            keyText = charFromInt(currentPedalCode);
        }
        if (buttonCodes[currentListening] != null) {
            buttonCodes[currentListening].setText(keyText);
            pageButtonWaiting.removeCallbacks(stopListening);
        }
        if (buttonMidis[currentListening] != null) {
            buttonMidis[currentListening].setText(midiText);
            pageButtonWaiting.removeCallbacks(stopListening);
        }
    }

    private void removePreviouslySetKey(int keyCode) {
        // Check for any other pedals currently set to this value and remove them.
        for (int x = 1; x <= 8; x++) {
            if (currentListening != x && mainActivityInterface.getPedalActions().getPedalCode(x)==keyCode) {
                setPedalPreference(x, defKeyCodes[x], null);
                buttonCodes[x].setText(R.string.is_not_set);
            }
        }
    }

    private void removePreviouslySetMidi(String midiCode) {
        // Check for any other pedals currently set to this value and remove them.
        for (int x = 1; x <= 8; x++) {
            if (currentListening != x && mainActivityInterface.getPedalActions().getMidiCode(x).equals(midiCode)) {
                mainActivityInterface.getPedalActions().setMidiCode(x,"");
                buttonMidis[x].setText(R.string.is_not_set);
            }
        }
    }



    private class MyTextWatcher implements TextWatcher {

        String val;
        int which;
        boolean shortPress;
        ExposedDropDown shortPressDropdown;
        ExposedDropDown longPressDropdown;

        MyTextWatcher(String val, int which, boolean shortPress) {
            this.val = val;
            this.shortPress = shortPress;
            this.which = which;
            shortPressDropdown = getShortExposedDropdownByNum(which);
            longPressDropdown = getLongExposedDropdownByNum(which);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            val = s.toString();
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Save the value via the pedalactions
            String actionCode = getActionCodeFromAction(val);
            mainActivityInterface.getPedalActions().setPedalPreference(which,shortPress,actionCode);
            // If this is a short press and it is a beat buddy transition, set the long press
            // (actually action up) to the transition exit
            if (shortPress) {
                checkBeatBuddyTransistions(shortPressDropdown, longPressDropdown, actionCode);
            }
        }
    }

    // Check if we have BeatBuddy transistions set
    private void checkBeatBuddyTransistions(ExposedDropDown shortPressDropdown,
                                            ExposedDropDown longPressDropdown,
                                            String actionCode) {
        String shortHint;
        String longHint;
        String longTextOverride;
        boolean longEnabled;

        if (actionCode.startsWith("beatbuddytrans")) {
            shortHint = pedal_down_string;
            longHint = pedal_up_string;
            longTextOverride = getActionFromActionCode("beatbuddytransexit");
            longEnabled = false;
        } else if (actionCode.startsWith("beatbuddyxtrans")) {
            shortHint = pedal_down_string;
            longHint = pedal_up_string;
            longTextOverride = getActionFromActionCode("beatbuddyxtransexit");
            longEnabled = false;
        } else {
            shortHint = short_press_string;
            longHint = long_press_string;
            longTextOverride = null;
            longEnabled = true;
        }

        shortPressDropdown.post(() -> {
            Log.d(TAG, "Setting the short hint as: " + shortHint);
            shortPressDropdown.setHint(shortHint);
        });
        longPressDropdown.post(() -> {
            Log.d(TAG, "Setting the long hint as: " + longHint);
            longPressDropdown.setHint(longHint);
            if (longTextOverride != null) {
                longPressDropdown.setText(longTextOverride);
            }
            longPressDropdown.setEnabled(longEnabled);
        });
    }

    private void setPedalPreference(int which, int pedalCode, String pedalMidi) {
        if (pedalMidi == null) {
            // Normal key press
            mainActivityInterface.getPedalActions().setPedalCode(which, pedalCode);
        } else {
            // Midi press
            mainActivityInterface.getPedalActions().setMidiCode(which, pedalMidi);
        }
    }

    private class MySliderTouchListener implements Slider.OnSliderTouchListener {

        private final String prefName;

        MySliderTouchListener(String prefName) {
            this.prefName = prefName;
        }

        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) { }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            if (prefName.equals("scrollDistance")) {
                // Save the value via the gestures fragment
                mainActivityInterface.getPreferences().setMyPreferenceFloat("scrollDistance", slider.getValue()/100f);
                mainActivityInterface.getGestures().setScrollDistance(slider.getValue()/100f);
            } else if (prefName.equals("airTurnLongPressTime") || prefName.equals("repeatModeTime") ||
                    prefName.equals("repeatModeCount")) {
                // Save the value via the pedal fragment
                mainActivityInterface.getPedalActions().setPreferences(prefName, (int)slider.getValue());
            }
        }

    }

    private void updateKeyEvents(int keyCode, long millis, String which) {
        String timeCode = mainActivityInterface.getTimeTools().getTimeStampFromMills(
                mainActivityInterface.getLocale(), millis);
        String eventString = "";
        if (keyCode == -1 || KeyEvent.keyCodeToString(keyCode) == null) {
            eventString = unknown_string;
        } else {
            eventString = KeyEvent.keyCodeToString(keyCode);
        }
        keyEvents.append(timeCode).append(" ").append(eventString).append("(").append(keyCode).append(") : ").append(which).append("\n");
        myView.eventLog.setHint(keyEvents.toString());
    }

    private void resetTestViews() {
        keyEvents = new StringBuilder();
        if (myView!=null) {
            myView.longPressInfo.setHint(unknown_string);
            myView.eventLog.setHint(keyEvents.toString());
        }
    }

    public void setLongPressMode(String detectedMode) {
        if (detectedMode.equals("repeat")) {
            // TODO change to repeat_string if I get it working
            myView.longPressInfo.setHint(unknown_string);
        } else if (detectedMode.equals("airturn")) {
            myView.longPressInfo.setHint(airturn_string);
        }
    }

}