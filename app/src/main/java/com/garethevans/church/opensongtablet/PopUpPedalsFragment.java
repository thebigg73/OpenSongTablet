package com.garethevans.church.opensongtablet;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class PopUpPedalsFragment extends DialogFragment {

    static PopUpPedalsFragment newInstance() {
        PopUpPedalsFragment frag;
        frag = new PopUpPedalsFragment();
        return frag;
    }

    SwitchCompat pedalToggleScrollBeforeSwipeButton;

    Button pedal1button;
    Button pedal2button;
    Button pedal3button;
    Button pedal4button;
    Button pedal5button;
    Button pedal6button;

    TextView pedal1text;
    TextView pedal2text;
    TextView pedal3text;
    TextView pedal4text;
    TextView pedal5text;
    TextView pedal6text;

    Spinner pedal1choice;
    Spinner pedal2choice;
    Spinner pedal3choice;
    Spinner pedal4choice;
    Spinner pedal5choice;
    Spinner pedal6choice;
    Spinner pedallong1choice;
    Spinner pedallong2choice;
    Spinner pedallong3choice;
    Spinner pedallong4choice;
    Spinner pedallong5choice;
    Spinner pedallong6choice;

    ArrayList<String> availableactions;

    int assignWhich = -1;

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_pedals, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.options_options_pedal));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        // Initialise the views
        initialiseViews(V);

        // Build the options available in the Spinners
        setAvailableActions();

        // Initialise the buttons, text, listeners and set defaults
        resetButtons();

        return V;
    }

    public void initialiseViews(View V) {
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
    }

    public void setAvailableActions() {
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
        availableactions.add(getString(R.string.options_song_edit));

        //13
        availableactions.add(getString(R.string.options_options_theme));

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

    public String convertSelectionNumberToTextOption(int i) {
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

    public int convertSelectionTextToNumberOption(String s) {
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

    public String getSavedOptionForPedal(String which) {
        String savedoption = "";

        switch (which) {
            case "1s":
                savedoption = FullscreenActivity.pedal1shortaction;
                break;
            case "2s":
                savedoption = FullscreenActivity.pedal2shortaction;
                break;
            case "3s":
                savedoption = FullscreenActivity.pedal3shortaction;
                break;
            case "4s":
                savedoption = FullscreenActivity.pedal4shortaction;
                break;
            case "5s":
                savedoption = FullscreenActivity.pedal5shortaction;
                break;
            case "6s":
                savedoption = FullscreenActivity.pedal6shortaction;
                break;
            case "1l":
                savedoption = FullscreenActivity.pedal1longaction;
                break;
            case "2l":
                savedoption = FullscreenActivity.pedal2longaction;
                break;
            case "3l":
                savedoption = FullscreenActivity.pedal3longaction;
                break;
            case "4l":
                savedoption = FullscreenActivity.pedal4longaction;
                break;
            case "5l":
                savedoption = FullscreenActivity.pedal5longaction;
                break;
            case "6l":
                savedoption = FullscreenActivity.pedal6longaction;
                break;
        }
        return savedoption;
    }

    public void setSpinner(Spinner s, final String which) {
        int chosen = convertSelectionTextToNumberOption(getSavedOptionForPedal(which));
        ArrayAdapter a = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, availableactions);
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

    public void setButtons(final Button b, int i, final int which) {
        // This sets the buttons
        b.setEnabled(true);
        String t;
        if (i==-1) {
            t = getString(R.string.currentkeycode) + "=" + getString(R.string.notset);
        } else {
            t = getString(R.string.currentkeycode) + "=" + i;
        }
        t = t + "\n" + getString(R.string.options_options_pedal);
        b.setText(t);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtons();
                b.setEnabled(false);
                b.setText(getResources().getString(
                        R.string.pageturn_waiting));
                assignWhich = which;
            }
        });
    }

    public void setGroupText(TextView tv, int i) {
        String text = getString(R.string.pedal) + " " + i;
        tv.setText(text);
    }

    public void saveAction(String w, int i) {
        String option = convertSelectionNumberToTextOption(i);
        switch (w) {
            case "1s":
                FullscreenActivity.pedal1shortaction = option;
                break;
            case "2s":
                FullscreenActivity.pedal2shortaction = option;
                break;
            case "3s":
                FullscreenActivity.pedal3shortaction = option;
                break;
            case "4s":
                FullscreenActivity.pedal4shortaction = option;
                break;
            case "5s":
                FullscreenActivity.pedal5shortaction = option;
                break;
            case "6s":
                FullscreenActivity.pedal6shortaction = option;
                break;
            case "1l":
                FullscreenActivity.pedal1longaction = option;
                break;
            case "2l":
                FullscreenActivity.pedal2longaction = option;
                break;
            case "3l":
                FullscreenActivity.pedal3longaction = option;
                break;
            case "4l":
                FullscreenActivity.pedal4longaction = option;
                break;
            case "5l":
                FullscreenActivity.pedal5longaction = option;
                break;
            case "6l":
                FullscreenActivity.pedal6longaction = option;
                break;
        }
        Preferences.savePreferences();
    }

    public void resetButtons() {
        boolean checked = FullscreenActivity.toggleScrollBeforeSwipe.equals("Y");
        pedalToggleScrollBeforeSwipeButton.setChecked(checked);
        pedalToggleScrollBeforeSwipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) {
                    FullscreenActivity.toggleScrollBeforeSwipe = "N";
                } else {
                    FullscreenActivity.toggleScrollBeforeSwipe = "Y";
                }
                Preferences.savePreferences();
            }
        });

        setGroupText(pedal1text, 1);
        setGroupText(pedal2text, 2);
        setGroupText(pedal3text, 3);
        setGroupText(pedal4text, 4);
        setGroupText(pedal5text, 5);
        setGroupText(pedal6text, 6);

        setButtons(pedal1button, FullscreenActivity.pedal1, 1);
        setButtons(pedal2button, FullscreenActivity.pedal2, 2);
        setButtons(pedal3button, FullscreenActivity.pedal3, 3);
        setButtons(pedal4button, FullscreenActivity.pedal4, 4);
        setButtons(pedal5button, FullscreenActivity.pedal5, 5);
        setButtons(pedal6button, FullscreenActivity.pedal6, 6);

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
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    Log.d("d", "up press=" + keyCode);
                    // Reset buttons already using this keycode
                    if (FullscreenActivity.pedal1 == keyCode) {
                        FullscreenActivity.pedal1 = -1;
                    } else if (FullscreenActivity.pedal2 == keyCode) {
                        FullscreenActivity.pedal2 = -1;
                    } else if (FullscreenActivity.pedal3 == keyCode) {
                        FullscreenActivity.pedal3 = -1;
                    } else if (FullscreenActivity.pedal4 == keyCode) {
                        FullscreenActivity.pedal4 = -1;
                    } else if (FullscreenActivity.pedal5 == keyCode) {
                        FullscreenActivity.pedal5 = -1;
                    } else if (FullscreenActivity.pedal6 == keyCode) {
                        FullscreenActivity.pedal6 = -1;
                    }

                    if (keyCode == KeyEvent.KEYCODE_BACK && assignWhich>-1) {
                        //User has pressed the back key - not allowed!!!!
                        FullscreenActivity.myToastMessage = getResources().getString(R.string.no);
                        ShowToast.showToast(getActivity());
                    } else if (keyCode == KeyEvent.KEYCODE_BACK && assignWhich==-1) {
                        dismiss();
                        return false;
                    } else if (assignWhich==1) {
                        FullscreenActivity.pedal1 = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich==2) {
                        FullscreenActivity.pedal2 = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich==3) {
                        FullscreenActivity.pedal3 = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich==4) {
                        FullscreenActivity.pedal4 = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich==5) {
                        FullscreenActivity.pedal5 = keyCode;
                        Preferences.savePreferences();
                    } else if (assignWhich==6) {
                        FullscreenActivity.pedal6 = keyCode;
                        Preferences.savePreferences();
                    }
                    assignWhich = -1;
                    resetButtons();
                }
                return true; // pretend we've processed it
            }
        });

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}