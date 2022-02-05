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

    private final String TAG = "PedalsFragment";
    private SettingsPedalBinding myView;
    private MainActivityInterface mainActivityInterface;

    private ArrayList<String> actionCodes;
    private ArrayList<String> actions;
    private ExposedDropDownArrayAdapter arrayAdapter;

    private boolean longPressCapable = false;
    private long downTime, upTime;
    private String currentMidiCode;
    private int currentListening;
    private int currentPedalCode;
    private int[] defKeyCodes;
    private TextView[] buttonCodes, buttonMidis;
    private RelativeLayout[] buttonHeaders;
    private ExposedDropDown[] shortTexts, longTexts;

    private Handler pageButtonWaiting;
    private Runnable stopListening;

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

        mainActivityInterface.updateToolbar(getString(R.string.pedal));

        // Grab views
        grabViews();

        // Initialise the array items
        mainActivityInterface.getPedalActions().setUpPedalActions(requireContext(), mainActivityInterface);
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

        return myView.getRoot();
    }

    private void midiPedalAllowed() {
        String midiDevice = mainActivityInterface.getMidi().getMidiDeviceName();
        Log.d(TAG,"midiDevice: "+midiDevice);
        Log.d(TAG,"getMidiDevice: "+mainActivityInterface.getMidi().getMidiDevice());
        Log.d(TAG,"getMidiAsPedal: "+mainActivityInterface.getPedalActions().getMidiAsPedal());

        myView.midiAsPedal.setChecked(mainActivityInterface.getPedalActions().getMidiAsPedal());
        myView.midiAsPedal.setOnCheckedChangeListener((compoundButton, b) -> {
            mainActivityInterface.getPreferences().setMyPreferenceBoolean(requireContext(),"midiAsPedal",b);
            mainActivityInterface.getPedalActions().setMidiAsPedal(b);
            midiPedalAllowed();
        });
        if (mainActivityInterface.getMidi() != null && mainActivityInterface.getMidi().getMidiDevice() != null &&
                mainActivityInterface.getPedalActions().getMidiAsPedal()) {
            String message = getString(R.string.midi_pedal) + ": " +
                    mainActivityInterface.getMidi().getMidiDeviceName();
            myView.midiPedal.setText(message);
        } else {
            myView.midiPedal.setText(getString(R.string.pedal_midi_warning));
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
            return getString(R.string.is_not_set);
        } else {
            return KeyEvent.keyCodeToString(i);
        }
    }

    private void setupSliders() {
        myView.scrollDistance.setValue((int)(mainActivityInterface.getGestures().getScrollDistance()*100f));
        myView.scrollDistance.setLabelFormatter(value -> (int)value + "%");
        myView.scrollDistance.addOnChangeListener((slider, value, fromUser) -> myView.scrollDistance.setHint((int)value + "%"));
        myView.scrollDistance.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mainActivityInterface.getPreferences().setMyPreferenceFloat(requireContext(), "scrollDistance", slider.getValue()/100f);
                mainActivityInterface.getGestures().setScrollDistance(slider.getValue()/100f);
            }
        });

    }
    private void setupDropDowns() {
        arrayAdapter = new ExposedDropDownArrayAdapter(requireContext(), R.layout.view_exposed_dropdown_item, actions);
        for (int s = 1; s <= 8; s++) {
            doDropDowns(s, true);
        }
        for (int l = 1; l <= 8; l++) {
            doDropDowns(l, false);
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
        exposedDropDown.setText(getActionFromActionCode(currVal));
        arrayAdapter.keepSelectionPosition(exposedDropDown, actions);
        exposedDropDown.addTextChangedListener(new MyTextWatcher(currVal,which,isShort));
    }

    private String getActionCodeFromAction(String s) {
        return actionCodes.get(actions.indexOf(s));
    }

    private String getActionFromActionCode(String s) {
        Log.d(TAG, "s=" + s);
        int val = actionCodes.indexOf(s);
        Log.d(TAG, "val=" + val);
        if (val > -1 && actions.size() >= val) {
            return actions.get(actionCodes.indexOf(s));
        } else {
            return "";
        }
    }

    private void setupButtons() {
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
        buttonCodes[which].setText(getString(R.string.pedal_waiting));
        buttonMidis[which].setText(getString(R.string.pedal_waiting));
        pageButtonWaiting = new Handler();
        stopListening = () -> {
            buttonCodes[which].setText(charFromInt(currentPedalCode));
            buttonMidis[which].setText(currentMidiCode);
        };
        pageButtonWaiting.postDelayed(stopListening, 8000);
    }

    private void setupSwitches() {
        myView.pedalToggleScrollBeforeSwipeButton.setChecked(mainActivityInterface.getPedalActions().getPedalScrollBeforeMove());
        myView.pedalToggleWarnBeforeSwipeButton.setChecked(mainActivityInterface.getPedalActions().getPedalShowWarningBeforeMove());
        myView.pedalToggleScrollBeforeSwipeButton.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getPedalActions().setPreferences(getContext(), mainActivityInterface, "pedalScrollBeforeMove", isChecked));
        myView.pedalToggleWarnBeforeSwipeButton.setOnCheckedChangeListener((buttonView, isChecked) -> mainActivityInterface.getPedalActions().setPreferences(getContext(), mainActivityInterface, "pedalShowWarningBeforeMove", isChecked));
    }

    private void airTurnModeActions() {
        boolean airTurnMode = mainActivityInterface.getPedalActions().getAirTurnMode();
        myView.airTurnMode.setChecked(airTurnMode);
        if (airTurnMode) {
            myView.airTurnOptions.setVisibility(View.VISIBLE);
        } else {
            myView.airTurnOptions.setVisibility(View.GONE);
        }
        myView.airTurnMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mainActivityInterface.getPedalActions().setPreferences(requireContext(),mainActivityInterface,"airTurnMode",isChecked);
            if (isChecked) {
                myView.airTurnOptions.setVisibility(View.VISIBLE);
            } else {
                myView.airTurnOptions.setVisibility(View.GONE);
            }
        });

        int keyRepeatCount = mainActivityInterface.getPedalActions().getKeyRepeatCount();
        myView.autoRepeatCountSlider.setValue(keyRepeatCount);
        myView.autoRepeatCountSlider.setHint(keyRepeatCount + "");
        myView.autoRepeatCountSlider.addOnSliderTouchListener(new MySliderTouchListener("keyRepeatCount"));
        myView.autoRepeatCountSlider.addOnChangeListener(new MySliderChangeListener("keyRepeatCount"));
        int keyRepeatTime = mainActivityInterface.getPedalActions().getKeyRepeatTime();
        myView.autoRepeatTimeSlider.setValue(keyRepeatTime);
        myView.autoRepeatTimeSlider.setLabelFormatter(value -> ((int)value)+"ms");
        myView.autoRepeatTimeSlider.setHint(keyRepeatTime + "ms");
        myView.autoRepeatTimeSlider.addOnSliderTouchListener(new MySliderTouchListener("keyRepeatTime"));
        myView.autoRepeatTimeSlider.addOnChangeListener(new MySliderChangeListener("keyRepeatTime"));
    }

    // Key listeners called from MainActivity
    // Key down to register button in this fragment.
    // Key up and long press to detect if this is possible for test
    public void keyDownListener(int keyCode) {
        if (currentListening > 0) {
            // Get a text version of the keyCode
            String pedalText = charFromInt(keyCode);

            // Run the common actions for midi and key registrations
            commonEventDown(currentListening, keyCode, pedalText, null);

            // Check and remove any other pedals using this code
            removePreviouslySetKey(keyCode);
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
        longPressCapable = ((upTime - downTime) > 300 && (upTime - downTime) < 1000) || longPressCapable;
    }

    public void commonEventLong() {
        longPressCapable = true;
    }

    public boolean isListening() {
        return currentListening > -1;
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
        }
        if (buttonMidis[currentListening] != null) {
            buttonMidis[currentListening].setText(midiText);
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
                mainActivityInterface.getPedalActions().setMidiCode(requireContext(),mainActivityInterface,x,"");
                buttonMidis[x].setText(R.string.is_not_set);
            }
        }
    }

    private class MyTextWatcher implements TextWatcher {

        String val;
        int which;
        boolean shortPress;

        MyTextWatcher(String val, int which, boolean shortPress) {
            this.val = val;
            this.shortPress = shortPress;
            this.which = which;
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
            mainActivityInterface.getPedalActions().setPedalPreference(requireContext(),mainActivityInterface,which,shortPress,getActionCodeFromAction(val));
        }
    }

    private void setPedalPreference(int which, int pedalCode, String pedalMidi) {
        if (pedalMidi == null) {
            // Normal key press
            mainActivityInterface.getPedalActions().setPedalCode(requireContext(), mainActivityInterface, which, pedalCode);
        } else {
            // Midi press
            mainActivityInterface.getPedalActions().setMidiCode(requireContext(), mainActivityInterface, which, pedalMidi);
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
            // Save the value via the gestures fragment
            mainActivityInterface.getPedalActions().setPreferences(requireContext(),mainActivityInterface,prefName,(int) slider.getValue());
        }

    }

    private class MySliderChangeListener implements Slider.OnChangeListener {

        private final String prefName;

        MySliderChangeListener(String prefName) {
            this.prefName = prefName;
        }

        @Override
        public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
            // Update the helper text
            switch (prefName) {
                case "keyRepeatTime":
                    myView.autoRepeatTimeSlider.setHint((int) value + "ms");
                    break;
                case "keyRepeatCount":
                    myView.autoRepeatCountSlider.setHint((int) value + "");
                    break;
            }
        }
    }
}