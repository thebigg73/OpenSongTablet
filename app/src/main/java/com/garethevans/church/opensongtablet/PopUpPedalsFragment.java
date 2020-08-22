package com.garethevans.church.opensongtablet;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

public class PopUpPedalsFragment extends DialogFragment {

    static PopUpPedalsFragment newInstance() {
        PopUpPedalsFragment frag;
        frag = new PopUpPedalsFragment();
        return frag;
    }

    private SwitchCompat pedalToggleScrollBeforeSwipeButton, autoRepeatLongPress_Switch, pedalToggleWarnBeforeSwipeButton;
    private SeekBar autoRepeatCount_SeekBar;
    private Button pedal1button, pedal2button, pedal3button, pedal4button, pedal5button, pedal6button;
    private TextView pedal1text, pedal2text, pedal3text, pedal4text, pedal5text, pedal6text, autoRepeatCount_TextView;
    private Spinner pedal1choice, pedal2choice, pedal3choice, pedal4choice, pedal5choice, pedal6choice,
            pedallong1choice, pedallong2choice, pedallong3choice, pedallong4choice, pedallong5choice, pedallong6choice;
    private int keyRepeatCount = 20;
    private String keyRepeatCountText = "20";
    private ArrayList<String> availableactions;
    private Preferences preferences;

    private int assignWhich = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_pedals, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(Objects.requireNonNull(getActivity()).getResources().getString(R.string.footpedal));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe,getActivity());
            closeMe.setEnabled(false);
            dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.hide();

        preferences = new Preferences();

        // Initialise the views
        initialiseViews(V);

        // Build the options available in the Spinners
        setAvailableActions();

        // Initialise the buttons, text, listeners and set defaults
        resetButtons();

        // Set AirTurnMode actions
        airTurnModeActions();

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private void initialiseViews(View V) {
        // Initialise the views
        pedal1text   = V.findViewById(R.id.pedal1text);
        pedal2text   = V.findViewById(R.id.pedal2text);
        pedal3text   = V.findViewById(R.id.pedal3text);
        pedal4text   = V.findViewById(R.id.pedal4text);
        pedal5text   = V.findViewById(R.id.pedal5text);
        pedal6text   = V.findViewById(R.id.pedal6text);
        pedal1button = V.findViewById(R.id.pedal1button);
        pedal2button = V.findViewById(R.id.pedal2button);
        pedal3button = V.findViewById(R.id.pedal3button);
        pedal4button = V.findViewById(R.id.pedal4button);
        pedal5button = V.findViewById(R.id.pedal5button);
        pedal6button = V.findViewById(R.id.pedal6button);
        pedal1choice = V.findViewById(R.id.pedal1choice);
        pedal2choice = V.findViewById(R.id.pedal2choice);
        pedal3choice = V.findViewById(R.id.pedal3choice);
        pedal4choice = V.findViewById(R.id.pedal4choice);
        pedal5choice = V.findViewById(R.id.pedal5choice);
        pedal6choice = V.findViewById(R.id.pedal6choice);
        pedallong1choice = V.findViewById(R.id.pedallong1choice);
        pedallong2choice = V.findViewById(R.id.pedallong2choice);
        pedallong3choice = V.findViewById(R.id.pedallong3choice);
        pedallong4choice = V.findViewById(R.id.pedallong4choice);
        pedallong5choice = V.findViewById(R.id.pedallong5choice);
        pedallong6choice = V.findViewById(R.id.pedallong6choice);
        pedalToggleScrollBeforeSwipeButton = V.findViewById(R.id.pedalToggleScrollBeforeSwipeButton);
        pedalToggleWarnBeforeSwipeButton = V.findViewById(R.id.pedalToggleWarnBeforeSwipeButton);
        autoRepeatLongPress_Switch = V.findViewById(R.id.autoRepeatLongPress_Switch);
        autoRepeatCount_SeekBar = V.findViewById(R.id.autoRepeatCount_SeekBar);
        autoRepeatCount_TextView = V.findViewById(R.id.autoRepeatCount_TextView);
    }

    private void airTurnModeActions() {
        autoRepeatLongPress_Switch.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getActivity(),"airTurnMode",isChecked));
        autoRepeatCount_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                keyRepeatCount = progress;
                keyRepeatCountText = "" + progress;
                autoRepeatCount_TextView.setText(keyRepeatCountText);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Save the value
                preferences.setMyPreferenceInt(getActivity(),"keyRepeatCount",keyRepeatCount);
                // If 0 or 1, then no point, so switch off the AirTurn mode
                if (keyRepeatCount<2) {
                    autoRepeatLongPress_Switch.setChecked(false);
                } else if (!autoRepeatLongPress_Switch.isChecked()) {
                    autoRepeatLongPress_Switch.setChecked(true);
                }
            }
        });
    }

    private void setAvailableActions() {
        availableactions = new ArrayList<>();

        //0
        availableactions.add("");

        //1
        availableactions.add(getString(R.string.pageturn_previous));

        //2
        availableactions.add(getString(R.string.pageturn_next));

        //3
        availableactions.add(getString(R.string.pageturn_up));

        //4
        availableactions.add(getString(R.string.pageturn_down));

        //5
        availableactions.add(getString(R.string.padPedalText));

        //6
        availableactions.add(getString(R.string.autoscrollPedalText));

        //7
        availableactions.add(getString(R.string.metronomePedalText));

        //8
        availableactions.add(getString(R.string.padPedalText) + " & " + getString(R.string.autoscrollPedalText));

        //9
        availableactions.add(getString(R.string.padPedalText) + " & " + getString(R.string.metronomePedalText));

        //10
        availableactions.add(getString(R.string.autoscrollPedalText) + " & " + getString(R.string.metronomePedalText));

        //11
        availableactions.add(getString(R.string.padPedalText) + " & " + getString(R.string.autoscrollPedalText) + " & " + getString(R.string.metronomePedalText));

        //12
        availableactions.add(getString(R.string.edit));

        //13
        availableactions.add(getString(R.string.choose_theme));

        //14
        availableactions.add(getString(R.string.autoscale));

        //15
        availableactions.add(getString(R.string.transpose));

        //16
        availableactions.add(getString(R.string.showchords));

        //17
        availableactions.add(getString(R.string.showcapo));

        //18
        availableactions.add(getString(R.string.showlyrics));

        //19
        availableactions.add(getString(R.string.action_search));

        //20
        availableactions.add(getString(R.string.random_song));

        //21
        availableactions.add(getString(R.string.music_score));

        //22
        availableactions.add(getString(R.string.highlight));

        //23
        availableactions.add(getString(R.string.stickynotes));

        //24
        availableactions.add(getString(R.string.inc_autoscroll_speed));

        //25
        availableactions.add(getString(R.string.dec_autoscroll_speed));

        //26
        availableactions.add(getString(R.string.toggle_autoscroll_pause));

        //27
        availableactions.add(getString(R.string.gesture1));

        //28
        availableactions.add(getString(R.string.action_settings));

        //29
        availableactions.add(getString(R.string.currentset));

        //30
        availableactions.add(getString(R.string.gesture4));

        //31
        availableactions.add(getString(R.string.add_song_to_set));
    }

    private String convertSelectionNumberToTextOption(int i) {
        // This is done to allow me to change the order of the available options
        String option;
        switch (i) {
            case 0:
            default:
                // Nothing selected
                option = "";
                break;

            case 1:
                // Previous
                option = "prev";
                break;

            case 2:
                // Next
                option = "next";
                break;

            case 3:
                // Up
                option = "up";
                break;

            case 4:
                // Down
                option = "down";
                break;

            case 5:
                // Pad
                option = "pad";
                break;

            case 6:
                // Autoscroll
                option = "autoscroll";
                break;

            case 7:
                // Metronome
                option = "metronome";
                break;

            case 8:
                // Pad and autoscroll
                option = "pad_autoscroll";
                break;

            case 9:
                // Pad and metronome
                option = "pad_metronome";
                break;

            case 10:
                // Autoscroll and metronome
                option = "autoscroll_metronome";
                break;

            case 11:
                // Pad, Autoscroll and metronome
                option = "pad_autoscroll_metronome";
                break;

            case 12:
                // Edit song
                option = "editsong";
                break;

            case 13:
                // Choose theme
                option = "changetheme";
                break;

            case 14:
                // Choose autoscale
                option = "autoscale";
                break;

            case 15:
                // Transpose
                option = "transpose";
                break;

            case 16:
                // Show chords
                option = "showchords";
                break;

            case 17:
                // Show capo chords
                option = "showcapo";
                break;

            case 18:
                // Show lyrics
                option = "showlyrics";
                break;

            case 19:
                // Search
                option = "fullsearch";
                break;

            case 20:
                // Random song
                option = "randomsong";
                break;

            case 21:
                // Score
                option = "abcnotation";
                break;

            case 22:
                // Highlight
                option = "highlight";
                break;

            case 23:
                // Sticky notes
                option = "sticky";
                break;

            case 24:
                // Increase autoscroll speed
                option = "speedup";
                break;

            case 25:
                // Decrease autoscroll speed
                option = "slowdown";
                break;

            case 26:
                // Pause autoscroll
                option = "pause";
                break;

            case 27:
                // Open/close the song menu
                option = "songmenu";
                break;

            case 28:
                // Open/close the options menu
                option = "optionmenu";
                break;

            case 29:
                // Show the set
                option = "editset";
                break;

            case 30:
                // Refresh song
                option = "refreshsong";
                break;

            case 31:
                // Add song to set
                option = "addsongtoset";
                break;
        }

        return option;
    }

    private int convertSelectionTextToNumberOption(String s) {
        // This is done to allow me to change the order of the available options
        int option;
        switch (s) {
            case "":
            default:
                // Nothing selected
                option = 0;
                break;

            case "prev":
                // Previous
                option = 1;
                break;

            case "next":
                // Next
                option = 2;
                break;

            case "up":
                // Up
                option = 3;
                break;

            case "down":
                // Down
                option = 4;
                break;

            case "pad":
                // Pad
                option = 5;
                break;

            case "autoscroll":
                // Autoscroll
                option = 6;
                break;

            case "metronome":
                // Metronome
                option = 7;
                break;

            case "pad_autoscroll":
                // Pad and autoscroll
                option = 8;
                break;

            case "pad_metronome":
                // Pad and metronome
                option = 9;
                break;

            case "autoscroll_metronome":
                // Autoscroll and metronome
                option = 10;
                break;

            case "pad_autoscroll_metronome":
                // Pad, Autoscroll and metronome
                option = 11;
                break;

            case "editsong":
                // Edit song
                option = 12;
                break;

            case "changetheme":
                // Choose theme
                option = 13;
                break;

            case "autoscale":
                // Choose autoscale
                option = 14;
                break;

            case "transpose":
                // Transpose
                option = 15;
                break;

            case "showchords":
                // Show chords
                option = 16;
                break;

            case "showcapo":
                // Show capo chords
                option = 17;
                break;

            case "showlyrics":
                // Show lyrics
                option = 18;
                break;

            case "fullsearch":
                // Search
                option = 19;
                break;

            case "randomsong":
                // Random song
                option = 20;
                break;

            case "abcnotation":
                // Score
                option = 21;
                break;

            case "highlight":
                // Highlight
                option = 22;
                break;

            case "sticky":
                // Sticky notes
                option = 23;
                break;

            case "speedup":
                // Increase autoscroll speed
                option = 24;
                break;

            case "slowdown":
                // Decrease autoscroll speed
                option = 25;
                break;

            case "pause":
                // Pause autoscroll
                option = 26;
                break;

            case "songmenu":
                // Open/close the song menu
                option = 27;
                break;

            case "optionmenu":
                // Open/close the options menu
                option = 28;
                break;

            case "editset":
                // Show the set
                option = 29;
                break;

            case "refreshsong":
                // Refresh song
                option = 30;
                break;

            case "addsongtoset":
                // Add song to set
                option = 31;
                break;
        }

        return option;
    }

    private String getSavedOptionForPedal(String which) {
        String savedoption = "";

        switch (which) {
            case "1s":
                savedoption = preferences.getMyPreferenceString(getActivity(),"pedal1ShortPressAction","prev");
                break;
            case "2s":
                savedoption = preferences.getMyPreferenceString(getActivity(),"pedal2ShortPressAction","next");
                break;
            case "3s":
                savedoption = preferences.getMyPreferenceString(getActivity(),"pedal3ShortPressAction","prev");
                break;
            case "4s":
                savedoption = preferences.getMyPreferenceString(getActivity(),"pedal4ShortPressAction","next");
                break;
            case "5s":
                savedoption = preferences.getMyPreferenceString(getActivity(),"pedal5ShortPressAction","prev");
                break;
            case "6s":
                savedoption = preferences.getMyPreferenceString(getActivity(),"pedal6ShortPressAction","next");
                break;
            case "1l":
                savedoption = preferences.getMyPreferenceString(getActivity(),"pedal1LongPressAction","songmenu");
                break;
            case "2l":
                savedoption = preferences.getMyPreferenceString(getActivity(),"pedal2LongPressAction","set");
                break;
            case "3l":
                savedoption = preferences.getMyPreferenceString(getActivity(),"pedal3LongPressAction","songmenu");
                break;
            case "4l":
                savedoption = preferences.getMyPreferenceString(getActivity(),"pedal4LongPressAction","set");
                break;
            case "5l":
                savedoption = preferences.getMyPreferenceString(getActivity(),"pedal5LongPressAction","songmenu");
                break;
            case "6l":
                savedoption = preferences.getMyPreferenceString(getActivity(),"pedal6LongPressAction","set");
                break;
        }
        return savedoption;
    }

    private void setSpinner(Spinner s, final String which) {
        int chosen = convertSelectionTextToNumberOption(getSavedOptionForPedal(which));
        ArrayAdapter<String> a = new ArrayAdapter<>(Objects.requireNonNull(getActivity()), R.layout.my_spinner, availableactions);
        s.setAdapter(a);
        s.setSelection(chosen);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
               saveAction(which, i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void setButtons(final Button b, int i, final int which) {
        // This sets the buttons
        b.setEnabled(true);
        String t;
        if (i==-1) {
            t = getString(R.string.currentkeycode) + "=" + getString(R.string.notset);
        } else {
            t = getString(R.string.currentkeycode) + "=" + i;
        }
        t = t + "\n" + getString(R.string.footpedal);
        b.setText(t);

        b.setOnClickListener(v -> {
            resetButtons();
            b.setEnabled(false);
            b.setText(getResources().getString(
                    R.string.pageturn_waiting));
            assignWhich = which;
        });
    }

    private void setGroupText(TextView tv, int i) {
        String text = getString(R.string.pedal) + " " + i;
        tv.setText(text);
    }

    private void saveAction(String w, int i) {
        String option = convertSelectionNumberToTextOption(i);
        switch (w) {
            case "1s":
                preferences.setMyPreferenceString(getActivity(),"pedal1ShortPressAction",option);
                break;
            case "2s":
                preferences.setMyPreferenceString(getActivity(),"pedal2ShortPressAction",option);
                break;
            case "3s":
                preferences.setMyPreferenceString(getActivity(),"pedal3ShortPressAction",option);
                break;
            case "4s":
                preferences.setMyPreferenceString(getActivity(),"pedal4ShortPressAction",option);
                break;
            case "5s":
                preferences.setMyPreferenceString(getActivity(),"pedal5ShortPressAction",option);
                break;
            case "6s":
                preferences.setMyPreferenceString(getActivity(),"pedal6ShortPressAction",option);
                break;
            case "1l":
                preferences.setMyPreferenceString(getActivity(),"pedal1LongPressAction",option);
                break;
            case "2l":
                preferences.setMyPreferenceString(getActivity(),"pedal2LongPressAction",option);
                break;
            case "3l":
                preferences.setMyPreferenceString(getActivity(),"pedal3LongPressAction",option);
                break;
            case "4l":
                preferences.setMyPreferenceString(getActivity(),"pedal4LongPressAction",option);
                break;
            case "5l":
                preferences.setMyPreferenceString(getActivity(),"pedal5LongPressAction",option);
                break;
            case "6l":
                preferences.setMyPreferenceString(getActivity(),"pedal6LongPressAction",option);
                break;
        }
    }

    private void resetButtons() {
        pedalToggleScrollBeforeSwipeButton.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"pedalScrollBeforeMove",true));
        pedalToggleScrollBeforeSwipeButton.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getActivity(),"pedalScrollBeforeMove",isChecked));
        pedalToggleWarnBeforeSwipeButton.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"pedalShowWarningBeforeMove",false));
        pedalToggleWarnBeforeSwipeButton.setOnCheckedChangeListener((buttonView, isChecked) -> preferences.setMyPreferenceBoolean(getActivity(),"pedalShowWarningBeforeMove",isChecked));
        autoRepeatLongPress_Switch.setChecked(preferences.getMyPreferenceBoolean(getActivity(),"airTurnMode",false));
        keyRepeatCount = preferences.getMyPreferenceInt(getActivity(),"keyRepeatCount",20);
        keyRepeatCountText = "" + keyRepeatCount;
        autoRepeatCount_TextView.setText(keyRepeatCountText);
        autoRepeatCount_SeekBar.setProgress(keyRepeatCount);

        setGroupText(pedal1text, 1);
        setGroupText(pedal2text, 2);
        setGroupText(pedal3text, 3);
        setGroupText(pedal4text, 4);
        setGroupText(pedal5text, 5);
        setGroupText(pedal6text, 6);

        setButtons(pedal1button, preferences.getMyPreferenceInt(getActivity(),"pedal1Code",21), 1);
        setButtons(pedal2button, preferences.getMyPreferenceInt(getActivity(),"pedal2Code",22), 2);
        setButtons(pedal3button, preferences.getMyPreferenceInt(getActivity(),"pedal3Code",19), 3);
        setButtons(pedal4button, preferences.getMyPreferenceInt(getActivity(),"pedal4Code",20), 4);
        setButtons(pedal5button, preferences.getMyPreferenceInt(getActivity(),"pedal5Code",92), 5);
        setButtons(pedal6button, preferences.getMyPreferenceInt(getActivity(),"pedal6Code",93), 6);

        setSpinner(pedal1choice, "1s");
        setSpinner(pedal2choice, "2s");
        setSpinner(pedal3choice, "3s");
        setSpinner(pedal4choice, "4s");
        setSpinner(pedal5choice, "5s");
        setSpinner(pedal6choice, "6s");
        setSpinner(pedallong1choice, "1l");
        setSpinner(pedallong2choice, "2l");
        setSpinner(pedallong3choice, "3l");
        setSpinner(pedallong4choice, "4l");
        setSpinner(pedallong5choice, "5l");
        setSpinner(pedallong6choice, "6l");
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().setOnKeyListener((dialog, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                // Reset buttons already using this keycode
                if (preferences.getMyPreferenceInt(getActivity(),"pedal1Code",21) == keyCode) {
                    preferences.setMyPreferenceInt(getActivity(),"pedal1Code",-1);
                } else if (preferences.getMyPreferenceInt(getActivity(),"pedal2Code",22) == keyCode) {
                    preferences.setMyPreferenceInt(getActivity(),"pedal2Code",-1);
                } else if (preferences.getMyPreferenceInt(getActivity(),"pedal3Code",19) == keyCode) {
                    preferences.setMyPreferenceInt(getActivity(),"pedal3Code",-1);
                } else if (preferences.getMyPreferenceInt(getActivity(),"pedal4Code",20) == keyCode) {
                    preferences.setMyPreferenceInt(getActivity(),"pedal4Code",-1);
                } else if (preferences.getMyPreferenceInt(getActivity(),"pedal5Code",92) == keyCode) {
                    preferences.setMyPreferenceInt(getActivity(),"pedal5Code",-1);
                } else if (preferences.getMyPreferenceInt(getActivity(),"pedal6Code",93) == keyCode) {
                    preferences.setMyPreferenceInt(getActivity(),"pedal6Code",-1);
                }

                if (keyCode == KeyEvent.KEYCODE_BACK && assignWhich>-1) {
                    //User has pressed the back key - not allowed!!!!
                    StaticVariables.myToastMessage = getResources().getString(R.string.no);
                    ShowToast.showToast(getActivity());
                } else if (keyCode == KeyEvent.KEYCODE_BACK && assignWhich==-1) {
                    dismiss();
                    return false;
                } else if (assignWhich==1) {
                    preferences.setMyPreferenceInt(getActivity(),"pedal1Code",keyCode);
                } else if (assignWhich==2) {
                    preferences.setMyPreferenceInt(getActivity(),"pedal2Code",keyCode);
                } else if (assignWhich==3) {
                    preferences.setMyPreferenceInt(getActivity(),"pedal3Code",keyCode);
                } else if (assignWhich==4) {
                    preferences.setMyPreferenceInt(getActivity(),"pedal4Code",keyCode);
                } else if (assignWhich==5) {
                    preferences.setMyPreferenceInt(getActivity(),"pedal5Code",keyCode);
                } else if (assignWhich==6) {
                    preferences.setMyPreferenceInt(getActivity(),"pedal6Code",keyCode);
                }
                assignWhich = -1;
                resetButtons();
            }
            return true; // pretend we've processed it
        });

    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

}